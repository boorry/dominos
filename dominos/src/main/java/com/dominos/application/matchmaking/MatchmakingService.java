package com.dominos.application.matchmaking;

import com.dominos.application.utilisateur.UtilisateurExceptions;
import com.dominos.application.utilisateur.UtilisateurRepository;
import com.dominos.domain.partie.PartieEnAttente;
import com.dominos.domain.utilisateur.StatutUtilisateur;
import com.dominos.domain.utilisateur.Utilisateur;

import java.util.List;

/**
 * Service de matchmaking — orchestre la file d'attente et la création de parties.
 *
 * Responsabilités :
 *  - Ajouter un joueur à la file (rejoindreFile)
 *  - Quitter la file (quitterFile)
 *  - Détecter automatiquement quand 3 joueurs sont réunis
 *  - Créer une PartieEnAttente et mettre à jour les statuts
 *
 * Ce service ne connaît ni HTTP, ni WebSocket, ni console.
 * Il dépend de deux interfaces :
 *  - UtilisateurRepository  : pour lire/mettre à jour les utilisateurs
 *  - MatchmakingRepository  : pour gérer la file d'attente
 */
public class MatchmakingService {

    private static final int JOUEURS_PAR_PARTIE = 3;

    private final UtilisateurRepository  utilisateurRepository;
    private final MatchmakingRepository  matchmakingRepository;

    public MatchmakingService(UtilisateurRepository utilisateurRepository,
                              MatchmakingRepository matchmakingRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.matchmakingRepository = matchmakingRepository;
    }

    // -------------------------------------------------------------------------
    // Rejoindre la file
    // -------------------------------------------------------------------------

    /**
     * Un joueur demande à jouer.
     *
     * Étapes :
     *  1. Vérifier que le joueur existe
     *  2. Vérifier qu'il n'est pas déjà EN_PARTIE
     *  3. Vérifier qu'il n'est pas déjà EN_ATTENTE
     *  4. Passer son statut à EN_ATTENTE
     *  5. L'ajouter à la file
     *  6. Vérifier si 3 joueurs sont réunis → créer la partie si oui
     *
     * @param utilisateurId identifiant du joueur
     * @return ResultatMatchmaking — EN_ATTENTE ou PARTIE_TROUVEE
     */
    public ResultatMatchmaking rejoindreFile(String utilisateurId) {
        Utilisateur joueur = utilisateurRepository.trouverParId(utilisateurId)
            .orElseThrow(() ->
                new UtilisateurExceptions.UtilisateurIntrouvableException(utilisateurId));

        // Vérifications métier
        if (joueur.estEnPartie()) {
            throw new MatchmakingExceptions.JoueurDejaEnPartieException(joueur.getPseudo());
        }
        if (joueur.estEnAttente() || matchmakingRepository.estEnAttente(utilisateurId)) {
            throw new MatchmakingExceptions.JoueurDejaEnAttenteException(joueur.getPseudo());
        }

        // Mettre à jour le statut
        joueur.changerStatut(StatutUtilisateur.EN_ATTENTE);
        utilisateurRepository.sauvegarder(joueur);

        // Ajouter à la file
        matchmakingRepository.ajouterEnAttente(joueur);

        // Vérifier si une partie peut être créée
        return verifierEtCreerPartie();
    }

    // -------------------------------------------------------------------------
    // Quitter la file
    // -------------------------------------------------------------------------

    /**
     * Un joueur annule sa demande et quitte la file d'attente.
     *
     * @param utilisateurId identifiant du joueur
     */
    public void quitterFile(String utilisateurId) {
        Utilisateur joueur = utilisateurRepository.trouverParId(utilisateurId)
            .orElseThrow(() ->
                new UtilisateurExceptions.UtilisateurIntrouvableException(utilisateurId));

        if (!matchmakingRepository.estEnAttente(utilisateurId)) {
            throw new MatchmakingExceptions.JoueurNonEnAttenteException(joueur.getPseudo());
        }

        // Retirer de la file
        matchmakingRepository.retirerDeAttente(joueur);

        // Remettre le statut à LIBRE
        joueur.changerStatut(StatutUtilisateur.LIBRE);
        utilisateurRepository.sauvegarder(joueur);
    }

    // -------------------------------------------------------------------------
    // Consultation
    // -------------------------------------------------------------------------

    /**
     * Nombre de joueurs actuellement en attente.
     */
    public int getNombreEnAttente() {
        return matchmakingRepository.nombreEnAttente();
    }

    // -------------------------------------------------------------------------
    // Privé — création de partie
    // -------------------------------------------------------------------------

    /**
     * Vérifie si 3 joueurs sont disponibles.
     * Si oui : crée la PartieEnAttente, retire les joueurs de la file,
     * et passe leurs statuts à EN_PARTIE.
     */
    private ResultatMatchmaking verifierEtCreerPartie() {
        int enAttente = matchmakingRepository.nombreEnAttente();

        if (enAttente < JOUEURS_PAR_PARTIE) {
            return ResultatMatchmaking.enAttente(enAttente);
        }

        // Prendre les 3 premiers dans l'ordre d'arrivée
        List<Utilisateur> joueurs =
            matchmakingRepository.getPremiersEnAttente(JOUEURS_PAR_PARTIE);

        // Retirer les 3 joueurs de la file et les passer EN_PARTIE
        for (Utilisateur joueur : joueurs) {
            matchmakingRepository.retirerDeAttente(joueur);
            joueur.changerStatut(StatutUtilisateur.EN_PARTIE);
            utilisateurRepository.sauvegarder(joueur);
        }

        PartieEnAttente partie = new PartieEnAttente(joueurs);
        return ResultatMatchmaking.partieTrouvee(partie);
    }
}
