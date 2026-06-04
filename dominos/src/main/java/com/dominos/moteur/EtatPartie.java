package com.dominos.moteur;

/**
 * États possibles d'une partie à un instant donné.
 */
public enum EtatPartie {
    /** La partie n'a pas encore commencé. */
    EN_ATTENTE,
    /** Une manche est en cours. */
    EN_COURS,
    /** Un joueur a vidé sa main — victoire normale. */
    VICTOIRE,
    /** Tous les joueurs ont passé consécutivement — blocage. */
    BLOCAGE,
    /** La partie entière est terminée (score max atteint). */
    TERMINEE
}
