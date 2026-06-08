package com.dominos.application.partie;

import com.dominos.domain.Domino;
import com.dominos.domain.Joueur;
import com.dominos.domain.Partie;
import com.dominos.domain.DominoParser;
import com.dominos.domain.partie.PartieEnAttente;
import com.dominos.infrastructure.partie.JoueurPartieEntity;
import com.dominos.infrastructure.partie.PartieEntity;
import com.dominos.infrastructure.partie.PartieJpaRepository;
import com.dominos.moteur.EtatPartie;
import com.dominos.moteur.MoteurJeu;
import com.dominos.moteur.ResultatCoup;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service applicatif pour la gestion des parties.
 *
 * Version mise à jour avec notifications WebSocket.
 * Après chaque action (jouerCoup, passerTour, creerPartie),
 * un événement est envoyé à tous les joueurs via NotificationService.
 */
@Service
public class PartieApplicationService {

    private final Map<String, Partie> partiesActives = new ConcurrentHashMap<>();

    private final MoteurJeu           moteur;
    private final PartieJpaRepository partieRepo;
    private final NotificationService notificationService;

    public PartieApplicationService(MoteurJeu moteur,
                                    PartieJpaRepository partieRepo,
                                    NotificationService notificationService) {
        this.moteur              = moteur;
        this.partieRepo          = partieRepo;
        this.notificationService = notificationService;
    }

    // -------------------------------------------------------------------------
    // Création d'une partie
    // -------------------------------------------------------------------------

    public String creerPartie(PartieEnAttente partieEnAttente) {
        String partieId = partieEnAttente.getId();

        List<Joueur> joueurs = partieEnAttente.getJoueurs().stream()
            .map(u -> new Joueur(u.getId(), u.getPseudo()))
            .toList();

        Partie partie = new Partie(joueurs);
        moteur.initialiserManche(partie);
        partiesActives.put(partieId, partie);

        // Persister
        PartieEntity entity = new PartieEntity(partieId, EtatPartie.EN_COURS, 1);
        for (Joueur j : joueurs) {
            entity.getJoueurs().add(
                new JoueurPartieEntity(entity, j.getId(), j.getPseudo()));
        }
        partieRepo.save(entity);

        // Notifier les joueurs via WebSocket
        List<String> pseudos = joueurs.stream()
            .map(Joueur::getPseudo).toList();
        notificationService.notifierPartieCreee(partieId, pseudos);

        return partieId;
    }

    // -------------------------------------------------------------------------
    // Consultation
    // -------------------------------------------------------------------------

    public Optional<Partie> getPartie(String partieId) {
        return Optional.ofNullable(partiesActives.get(partieId));
    }

    public List<Domino> getDominosJouables(String partieId) {
        return moteur.getDominosJouables(getPartieOuException(partieId));
    }

    // -------------------------------------------------------------------------
    // Jouer un coup
    // -------------------------------------------------------------------------

    public ResultatCoup jouerCoup(String partieId,
                                   String utilisateurId,
                                   String dominoStr) {
        Partie partie = getPartieOuException(partieId);
        validerJoueurCourant(partie, utilisateurId);

        Domino domino  = DominoParser.parse(dominoStr);
        ResultatCoup r = moteur.jouerCoup(partie, domino);

        mettreAJourPersistance(partieId, partie, r);

        // Construire le plateau pour la notification
        List<String> plateauStr = partie.getPlateau().getDominos()
            .stream().map(Domino::toString).toList();

        Map<String, Integer> scores = construireScores(partie);

        if (r.etat() == EtatPartie.EN_COURS) {
            // Notifier coup joué + joueur suivant
            notificationService.notifierCoupJoue(
                partieId,
                partie.getJoueurCourant() != null
                    ? r.joueurActif().getPseudo()
                    : utilisateurId,
                r.message(),
                plateauStr,
                partie.getJoueurCourant().getPseudo(),
                scores,
                partie.getMancheCourante()
            );

        } else if (r.etat() == EtatPartie.VICTOIRE
                || r.etat() == EtatPartie.BLOCAGE) {
            // Notifier fin de manche
            notificationService.notifierMancheTerminee(
                partieId,
                r.gagnant() != null ? r.gagnant().getPseudo() : null,
                scores,
                partie.getMancheCourante(),
                r.message()
            );

        } else if (r.etat() == EtatPartie.TERMINEE) {
            // Notifier fin de partie
            notificationService.notifierPartieTerminee(
                partieId,
                r.gagnant() != null ? r.gagnant().getPseudo() : null,
                scores,
                r.message()
            );
            partiesActives.remove(partieId);
        }

        return r;
    }

    // -------------------------------------------------------------------------
    // Passer un tour
    // -------------------------------------------------------------------------

    public ResultatCoup passerTour(String partieId, String utilisateurId) {
        Partie partie = getPartieOuException(partieId);
        validerJoueurCourant(partie, utilisateurId);

        ResultatCoup r = moteur.passerTour(partie);
        mettreAJourPersistance(partieId, partie, r);

        Map<String, Integer> scores = construireScores(partie);

        if (r.etat() == EtatPartie.EN_COURS) {
            notificationService.notifierJoueurPasse(
                partieId,
                r.joueurActif().getPseudo(),
                partie.getJoueurCourant().getPseudo(),
                partie.getMancheCourante()
            );

        } else if (r.etat() == EtatPartie.BLOCAGE
                || r.etat() == EtatPartie.VICTOIRE) {
            notificationService.notifierMancheTerminee(
                partieId,
                r.gagnant() != null ? r.gagnant().getPseudo() : null,
                scores,
                partie.getMancheCourante(),
                r.message()
            );

        } else if (r.etat() == EtatPartie.TERMINEE) {
            notificationService.notifierPartieTerminee(
                partieId,
                r.gagnant() != null ? r.gagnant().getPseudo() : null,
                scores,
                r.message()
            );
            partiesActives.remove(partieId);
        }

        return r;
    }

    // -------------------------------------------------------------------------
    // Privé
    // -------------------------------------------------------------------------

    private Partie getPartieOuException(String partieId) {
        return Optional.ofNullable(partiesActives.get(partieId))
            .orElseThrow(() ->
                new PartieExceptions.PartieIntrouvableException(partieId));
    }

    private void validerJoueurCourant(Partie partie, String utilisateurId) {
        if (!partie.getJoueurCourant().getId().equals(utilisateurId)) {
            throw new PartieExceptions.PasTonTourException(
                partie.getJoueurCourant().getPseudo());
        }
    }

    private Map<String, Integer> construireScores(Partie partie) {
        return partie.getScores().entrySet().stream()
            .collect(Collectors.toMap(
                e -> e.getKey().getPseudo(),
                Map.Entry::getValue
            ));
    }

    private void mettreAJourPersistance(String partieId,
                                         Partie partie,
                                         ResultatCoup r) {
        if (r.estTermine()) {
            partieRepo.findById(partieId).ifPresent(entity -> {
                entity.setEtat(r.etat());
                entity.setMancheCourante(partie.getMancheCourante());
                for (JoueurPartieEntity jp : entity.getJoueurs()) {
                    partie.getJoueurs().stream()
                        .filter(j -> j.getId().equals(jp.getUtilisateurId()))
                        .findFirst()
                        .ifPresent(j -> jp.setScore(partie.getScore(j)));
                }
                partieRepo.save(entity);
            });
        }
    }
}
