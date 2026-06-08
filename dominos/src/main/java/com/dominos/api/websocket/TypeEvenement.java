package com.dominos.api.websocket;

/**
 * Types d'événements envoyés via WebSocket aux clients.
 *
 * Chaque événement correspond à une action dans la partie.
 * Le client JavaScript lit le champ "type" pour savoir
 * quoi afficher ou faire.
 */
public enum TypeEvenement {

    /** Le matchmaking a réuni 3 joueurs — la partie commence. */
    PARTIE_CREEE,

    /** Un joueur a posé un domino sur le plateau. */
    COUP_JOUE,

    /** C'est au tour du joueur suivant. */
    TOUR_SUIVANT,

    /** Un joueur n'avait aucun domino jouable et a passé. */
    JOUEUR_PASSE,

    /** Une manche est terminée (victoire ou blocage). */
    MANCHE_TERMINEE,

    /** La partie entière est terminée — un joueur a atteint 120 points. */
    PARTIE_TERMINEE,

    /** Erreur survenue pendant le traitement d'un coup. */
    ERREUR
}
