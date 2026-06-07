package com.dominos.application.partie;

import com.dominos.domain.Joueur;
import com.dominos.domain.Partie;
import com.dominos.domain.Domino;
import com.dominos.domain.DominoParser;
import com.dominos.domain.partie.PartieEnAttente;
import com.dominos.infrastructure.partie.PartieEntity;
import com.dominos.infrastructure.partie.PartieJpaRepository;
import com.dominos.infrastructure.partie.JoueurPartieEntity;
import com.dominos.moteur.EtatPartie;
import com.dominos.moteur.MoteurJeu;
import com.dominos.moteur.ResultatCoup;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service applicatif pour la gestion des parties.
 *
 * Responsabilités :
 *  - Créer une Partie depuis une PartieEnAttente (issue du matchmaking)
 *  - Stocker les parties actives en mémoire (Map id → Partie)
 *  - Persister l'état en base via PartieJpaRepository
 *  - Déléguer les règles au MoteurJeu
 *
 * Pourquoi stocker en mémoire ET en base ?
 *   La Partie (domain) contient l'état complet du jeu en cours
 *   (mains des joueurs, plateau, compteur de passes...).
 *   Sérialiser tout ça en base à chaque coup serait coûteux.
 *   On garde la Partie en mémoire pendant la partie,
 *   et on persiste les événements importants (début, fin, scores).
 *
 *   À l'Étape 6 (WebSocket), on pourra aussi notifier les joueurs
 *   depuis ce service.
 */
@Service
public class PartieApplicationService {

    // Parties actives en mémoire : partieId → Partie
    private final Map<String, Partie>   partiesActives = new ConcurrentHashMap<>();
    // Correspondance partieId → liste des utilisateurIds
    private final Map<String, List<String>> partieJoueurs = new ConcurrentHashMap<>();

    private final MoteurJeu            moteur;
    private final PartieJpaRepository  partieRepo;

    public PartieApplicationService(MoteurJeu moteur,
                                    PartieJpaRepository partieRepo) {
        this.moteur    = moteur;
        this.partieRepo = partieRepo;
    }

    // -------------------------------------------------------------------------
    // Création d'une partie
    // -------------------------------------------------------------------------

    /**
     * Crée et initialise une partie depuis le résultat du matchmaking.
     * Appelé automatiquement quand 3 joueurs sont réunis.
     *
     * @return l'identifiant de la partie créée
     */
    public String creerPartie(PartieEnAttente partieEnAttente) {
        String partieId = partieEnAttente.getId();

        // Créer les joueurs domaine depuis les utilisateurs
        List<Joueur> joueurs = partieEnAttente.getJoueurs().stream()
            .map(u -> new Joueur(u.getId(), u.getPseudo()))
            .toList();

        // Créer la Partie domaine
        Partie partie = new Partie(joueurs);

        // Initialiser la première manche
        moteur.initialiserManche(partie);

        // Stocker en mémoire
        partiesActives.put(partieId, partie);
        partieJoueurs.put(partieId, joueurs.stream()
            .map(Joueur::getId).toList());

        // Persister en base
        PartieEntity entity = new PartieEntity(
            partieId, EtatPartie.EN_COURS, 1);

        for (Joueur j : joueurs) {
            entity.getJoueurs().add(
                new JoueurPartieEntity(entity, j.getId(), j.getPseudo()));
        }
        partieRepo.save(entity);

        return partieId;
    }

    // -------------------------------------------------------------------------
    // Consultation
    // -------------------------------------------------------------------------

    /**
     * Retourne la partie active par son id.
     */
    public Optional<Partie> getPartie(String partieId) {
        return Optional.ofNullable(partiesActives.get(partieId));
    }

    /**
     * Retourne les dominos jouables pour le joueur courant.
     */
    public List<Domino> getDominosJouables(String partieId) {
        Partie partie = getPartieOuException(partieId);
        return moteur.getDominosJouables(partie);
    }

    // -------------------------------------------------------------------------
    // Actions de jeu
    // -------------------------------------------------------------------------

    /**
     * Le joueur joue un domino.
     *
     * @param partieId      identifiant de la partie
     * @param utilisateurId identifiant du joueur qui joue
     * @param dominoStr     domino au format "[x|y]" ou "x,y"
     */
    public ResultatCoup jouerCoup(String partieId,
                                  String utilisateurId,
                                  String dominoStr) {
        Partie partie  = getPartieOuException(partieId);
        validerJoueurCourant(partie, utilisateurId);

        Domino domino  = DominoParser.parse(dominoStr);
        ResultatCoup r = moteur.jouerCoup(partie, domino);

        mettreAJourPersistance(partieId, partie, r);
        return r;
    }

    /**
     * Le joueur passe son tour.
     */
    public ResultatCoup passerTour(String partieId, String utilisateurId) {
        Partie partie  = getPartieOuException(partieId);
        validerJoueurCourant(partie, utilisateurId);

        ResultatCoup r = moteur.passerTour(partie);

        mettreAJourPersistance(partieId, partie, r);
        return r;
    }

    // -------------------------------------------------------------------------
    // Privé
    // -------------------------------------------------------------------------

    private Partie getPartieOuException(String partieId) {
        return Optional.ofNullable(partiesActives.get(partieId))
            .orElseThrow(() -> new PartieExceptions.PartieIntrouvableException(partieId));
    }

    private void validerJoueurCourant(Partie partie, String utilisateurId) {
        String idCourant = partie.getJoueurCourant().getId();
        if (!idCourant.equals(utilisateurId)) {
            throw new PartieExceptions.PasTonTourException(
                partie.getJoueurCourant().getPseudo());
        }
    }

    private void mettreAJourPersistance(String partieId,
                                         Partie partie,
                                         ResultatCoup r) {
        if (r.estTermine()) {
            // Mettre à jour l'état en base
            partieRepo.findById(partieId).ifPresent(entity -> {
                entity.setEtat(r.etat());
                entity.setMancheCourante(partie.getMancheCourante());

                // Mettre à jour les scores
                for (JoueurPartieEntity jp : entity.getJoueurs()) {
                    partie.getJoueurs().stream()
                        .filter(j -> j.getId().equals(jp.getUtilisateurId()))
                        .findFirst()
                        .ifPresent(j -> jp.setScore(partie.getScore(j)));
                }
                partieRepo.save(entity);
            });

            // Retirer de la mémoire si partie terminée
            if (r.etat() == EtatPartie.TERMINEE) {
                partiesActives.remove(partieId);
            }
        }
    }
}
