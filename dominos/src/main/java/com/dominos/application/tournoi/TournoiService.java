package com.dominos.application.tournoi;

import com.dominos.application.partie.PartieApplicationService;
import com.dominos.application.utilisateur.UtilisateurExceptions;
import com.dominos.application.utilisateur.UtilisateurRepository;
import com.dominos.domain.partie.PartieEnAttente;
import com.dominos.domain.tournoi.MatchTournoi;
import com.dominos.domain.tournoi.PhaseTournoi;
import com.dominos.domain.tournoi.StatutTournoi;
import com.dominos.domain.tournoi.Tournoi;
import com.dominos.domain.utilisateur.Utilisateur;

import java.util.List;

/**
 * Service applicatif pour les tournois.
 *
 * Responsabilités :
 *  - Créer un tournoi
 *  - Gérer les inscriptions
 *  - Démarrer un tournoi et créer les matchs de groupes
 *  - Enregistrer le résultat d'un match et avancer le bracket
 *  - Créer automatiquement les parties pour chaque match
 */
public class TournoiService {

    private final TournoiRepository       tournoiRepository;
    private final UtilisateurRepository   utilisateurRepository;
    private final PartieApplicationService partieService;

    public TournoiService(TournoiRepository tournoiRepository,
                          UtilisateurRepository utilisateurRepository,
                          PartieApplicationService partieService) {
        this.tournoiRepository     = tournoiRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.partieService         = partieService;
    }

    // -------------------------------------------------------------------------
    // Créer un tournoi
    // -------------------------------------------------------------------------

    /**
     * Crée un nouveau tournoi ouvert aux inscriptions.
     */
    public Tournoi creer(String nom) {
        Tournoi tournoi = new Tournoi(nom);
        return tournoiRepository.sauvegarder(tournoi);
    }

    // -------------------------------------------------------------------------
    // Inscription
    // -------------------------------------------------------------------------

    /**
     * Inscrit un joueur au tournoi.
     *
     * @throws TournoiExceptions.TournoiIntrouvableException si tournoi inconnu
     * @throws TournoiExceptions.JoueurDejaInscritException  si déjà inscrit
     * @throws TournoiExceptions.TournoiCompletException     si complet
     */
    public Tournoi inscrire(String tournoiId, String utilisateurId) {
        Tournoi tournoi = getTournoiOuException(tournoiId);

        Utilisateur utilisateur = utilisateurRepository
            .trouverParId(utilisateurId)
            .orElseThrow(() ->
                new UtilisateurExceptions.UtilisateurIntrouvableException(utilisateurId));

        try {
            tournoi.inscrire(utilisateurId);
        } catch (IllegalArgumentException e) {
            throw new TournoiExceptions.JoueurDejaInscritException(
                utilisateur.getPseudo());
        } catch (IllegalStateException e) {
            if (e.getMessage().contains("complet")) {
                throw new TournoiExceptions.TournoiCompletException();
            }
            throw e;
        }

        return tournoiRepository.sauvegarder(tournoi);
    }

    // -------------------------------------------------------------------------
    // Démarrage
    // -------------------------------------------------------------------------

    /**
     * Démarre le tournoi.
     * Crée les 9 matchs de groupes et les parties associées.
     *
     * @throws TournoiExceptions.TournoiNonDemarrableException si conditions non remplies
     */
    public Tournoi demarrer(String tournoiId) {
        Tournoi tournoi = getTournoiOuException(tournoiId);

        try {
            tournoi.demarrer();
        } catch (IllegalStateException e) {
            throw new TournoiExceptions.TournoiNonDemarrableException(e.getMessage());
        }

        tournoiRepository.sauvegarder(tournoi);

        // Créer les parties pour chaque match de groupes
        creerPartiesPourMatchs(tournoi, tournoi.getMatchsEnAttente());

        return tournoiRepository.sauvegarder(tournoi);
    }

    // -------------------------------------------------------------------------
    // Résultat d'un match
    // -------------------------------------------------------------------------

    /**
     * Enregistre le résultat d'un match de tournoi.
     * Si tous les matchs de la phase sont terminés, crée automatiquement
     * les matchs de la phase suivante.
     *
     * @param tournoiId identifiant du tournoi
     * @param matchId   identifiant du match
     * @param gagnantId identifiant du joueur gagnant
     */
    public Tournoi enregistrerResultat(String tournoiId,
                                        String matchId,
                                        String gagnantId) {
        Tournoi tournoi = getTournoiOuException(tournoiId);

        boolean nouvellephase = tournoi.terminerMatch(matchId, gagnantId);
        tournoiRepository.sauvegarder(tournoi);

        // Si on passe à une nouvelle phase, créer les nouvelles parties
        if (nouvellephase && tournoi.getStatut() == StatutTournoi.EN_COURS) {
            creerPartiesPourMatchs(tournoi, tournoi.getMatchsEnAttente());
            tournoiRepository.sauvegarder(tournoi);
        }

        return tournoi;
    }

    // -------------------------------------------------------------------------
    // Consultation
    // -------------------------------------------------------------------------

    public Tournoi consulter(String tournoiId) {
        return getTournoiOuException(tournoiId);
    }

    public List<Tournoi> listerTous() {
        return tournoiRepository.trouverTous();
    }

    public List<Tournoi> listerEnInscription() {
        return tournoiRepository.trouverEnInscription();
    }

    // -------------------------------------------------------------------------
    // Privé
    // -------------------------------------------------------------------------

    private Tournoi getTournoiOuException(String tournoiId) {
        return tournoiRepository.trouverParId(tournoiId)
            .orElseThrow(() ->
                new TournoiExceptions.TournoiIntrouvableException(tournoiId));
    }

    /**
     * Crée une PartieEnAttente pour chaque match en attente
     * et lie la partie créée au match.
     */
    private void creerPartiesPourMatchs(Tournoi tournoi,
                                         List<MatchTournoi> matchsEnAttente) {
        for (MatchTournoi match : matchsEnAttente) {
            // Récupérer les objets Utilisateur pour créer la PartieEnAttente
            List<Utilisateur> joueurs = match.getJoueursIds().stream()
                .map(id -> utilisateurRepository.trouverParId(id)
                    .orElseThrow(() ->
                        new UtilisateurExceptions.UtilisateurIntrouvableException(id)))
                .toList();

            PartieEnAttente partieEnAttente = new PartieEnAttente(joueurs);
            String partieId = partieService.creerPartie(partieEnAttente);
            tournoi.demarrerMatch(match.getId(), partieId);
        }
    }
}
