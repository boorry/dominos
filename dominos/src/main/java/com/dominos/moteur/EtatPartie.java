package com.dominos.moteur;

/**
 * États possibles d'une partie à un instant donné.
 *
 * CHOIX_REQUIS — nouvel état :
 *   Le joueur a soumis un domino jouable des deux côtés.
 *   Il doit maintenant choisir le côté (GAUCHE ou DROITE)
 *   via jouerCoupAvecCote() avant que le coup soit validé.
 */
public enum EtatPartie {
    EN_ATTENTE,
    EN_COURS,
    /** Le joueur doit choisir le côté de pose. */
    CHOIX_REQUIS,
    VICTOIRE,
    BLOCAGE,
    TERMINEE
}
