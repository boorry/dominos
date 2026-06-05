package com.dominos.application.matchmaking;

import com.dominos.domain.utilisateur.Utilisateur;

import java.util.List;
import java.util.Optional;

/**
 * Port de persistance pour la file d'attente du matchmaking.
 *
 * Responsabilités :
 *  - Gérer la liste des joueurs EN_ATTENTE
 *  - Garantir qu'un joueur n'est pas deux fois dans la file
 *
 * Implémenté par MatchmakingRepositoryMemoire à cette étape.
 * À l'Étape 4, remplacé par une implémentation JPA/Redis.
 */
public interface MatchmakingRepository {

    /**
     * Ajoute un joueur à la file d'attente.
     */
    void ajouterEnAttente(Utilisateur utilisateur);

    /**
     * Retire un joueur de la file d'attente.
     */
    void retirerDeAttente(Utilisateur utilisateur);

    /**
     * Retourne les N premiers joueurs en attente dans l'ordre d'arrivée.
     * Retourne une liste vide si moins de N joueurs sont disponibles.
     */
    List<Utilisateur> getPremiersEnAttente(int nombre);

    /**
     * Vrai si le joueur est déjà dans la file.
     */
    boolean estEnAttente(String utilisateurId);

    /**
     * Nombre de joueurs actuellement en attente.
     */
    int nombreEnAttente();
}
