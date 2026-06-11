package com.dominos.application.partie;

import com.dominos.domain.Domino;
import com.dominos.domain.DominoParser;
import com.dominos.domain.Joueur;
import com.dominos.domain.Partie;
import com.dominos.domain.Plateau;
import com.dominos.domain.partie.PartieEnAttente;
import com.dominos.infrastructure.partie.JoueurPartieEntity;
import com.dominos.infrastructure.partie.PartieEntity;
import com.dominos.infrastructure.partie.PartieJpaRepository;
import com.dominos.moteur.EtatPartie;
import com.dominos.moteur.MoteurJeu;
import com.dominos.moteur.ResultatCoup;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service applicatif pour la gestion des parties.
 *
 * Mis à jour avec jouerCoupAvecCote() pour gérer le choix
 * de côté quand un domino peut être posé des deux côtés.
 */
@Service
public class PartieApplicationService {

    private final Map<String, Partie> partiesActives = new ConcurrentHashMap<>();

    private final MoteurJeu            moteur;
    private final PartieJpaRepository  partieRepo;
    private final NotificationService  notificationService;

    public PartieApplicationService(MoteurJeu moteur,
                                    PartieJpaRepository partieRepo,
                                    NotificationService notificationService) {
        this.moteur             = moteur;
        this.partieRepo         = partieRepo;
        this.notificationService = notificationService;
    }

    // -------------------------------------------------------------------------
    // Création
    // -------------------------------------------------------------------------

    public String creerPartie(PartieEnAttente partieEnAttente) {
        String partieId = partieEnAttente.getId();

        List<Joueur> joueurs = partieEnAttente.getJoueurs().stream()
            .map(u -> new Joueur(u.getId(), u.getPseudo()))
            .toList();

        Partie partie = new Partie(joueurs);
        moteur.initialiserManche(partie);
        partiesActives.put(partieId, partie);

        PartieEntity entity = new PartieEntity(partieId, EtatPartie.EN_COURS, 1);
        for (Joueur j : joueurs) {
            entity.getJoueurs().add(
                new JoueurPartieEntity(entity, j.getId(), j.getPseudo()));
        }
        partieRepo.save(entity);

        notificationService.notifierPartieCreee(partieId,
            joueurs.stream().map(Joueur::getPseudo).toList());

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

    /**
     * Le joueur joue un domino.
     *
     * Si le résultat est CHOIX_REQUIS, le domino n'est pas encore posé.
     * Le joueur doit rappeler jouerCoupAvecCote() avec son choix.
     */
    public ResultatCoup jouerCoup(String partieId,
                                   String utilisateurId,
                                   String dominoStr) {
        Partie partie = getPartieOuException(partieId);
        validerJoueurCourant(partie, utilisateurId);

        Domino domino  = DominoParser.parse(dominoStr);
        ResultatCoup r = moteur.jouerCoup(partie, domino);

        // Si choix requis, on ne notifie pas encore — on attend le choix
        if (!r.choixRequis()) {
            notifierEtPersister(partieId, partie, r);
        }

        return r;
    }

    /**
     * Le joueur confirme le côté de pose après un CHOIX_REQUIS.
     *
     * @param coteStr "GAUCHE" ou "DROITE"
     */
    public ResultatCoup jouerCoupAvecCote(String partieId,
                                           String utilisateurId,
                                           String dominoStr,
                                           String coteStr) {
        Partie partie = getPartieOuException(partieId);
        validerJoueurCourant(partie, utilisateurId);

        Domino       domino = DominoParser.parse(dominoStr);
        Plateau.Cote cote   = parseCote(coteStr);

        ResultatCoup r = moteur.jouerCoupAvecCote(partie, domino, cote);
        notifierEtPersister(partieId, partie, r);
        return r;
    }

    // -------------------------------------------------------------------------
    // Passer son tour
    // -------------------------------------------------------------------------

    public ResultatCoup passerTour(String partieId, String utilisateurId) {
        Partie partie = getPartieOuException(partieId);
        validerJoueurCourant(partie, utilisateurId);

        ResultatCoup r = moteur.passerTour(partie);
        notifierEtPersister(partieId, partie, r);
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

    private Plateau.Cote parseCote(String coteStr) {
        try {
            return Plateau.Cote.valueOf(coteStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Côté invalide : '" + coteStr + "' — attendu GAUCHE ou DROITE");
        }
    }

    private Map<String, Integer> construireScores(Partie partie) {
        return partie.getScores().entrySet().stream()
            .collect(Collectors.toMap(
                e -> e.getKey().getPseudo(),
                Map.Entry::getValue));
    }

    private void notifierEtPersister(String partieId, Partie partie, ResultatCoup r) {
        Map<String, Integer> scores = construireScores(partie);
        List<String> plateauStr = partie.getPlateau().getDominos()
            .stream().map(Domino::toString).toList();

        if (r.etat() == EtatPartie.EN_COURS) {
            if (r.aJoue()) {
                notificationService.notifierCoupJoue(
                    partieId, r.joueurActif().getPseudo(), r.message(),
                    plateauStr, partie.getJoueurCourant().getPseudo(),
                    scores, partie.getMancheCourante());
            } else {
                notificationService.notifierJoueurPasse(
                    partieId, r.joueurActif().getPseudo(),
                    partie.getJoueurCourant().getPseudo(),
                    partie.getMancheCourante());
            }
        } else if (r.etat() == EtatPartie.VICTOIRE
                || r.etat() == EtatPartie.BLOCAGE) {
            notificationService.notifierMancheTerminee(
                partieId,
                r.gagnant() != null ? r.gagnant().getPseudo() : null,
                scores, partie.getMancheCourante(), r.message());
            mettreAJourPersistance(partieId, partie, r);
        } else if (r.etat() == EtatPartie.TERMINEE) {
            notificationService.notifierPartieTerminee(
                partieId,
                r.gagnant() != null ? r.gagnant().getPseudo() : null,
                scores, r.message());
            mettreAJourPersistance(partieId, partie, r);
            partiesActives.remove(partieId);
        }
    }

    private void mettreAJourPersistance(String partieId, Partie partie, ResultatCoup r) {
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
