package com.dominos.domain.utilisateur;

/**
 * Représente l'état d'un utilisateur dans la plateforme.
 *
 * LIBRE      → connecté, disponible, peut rejoindre une partie
 * EN_ATTENTE → a cliqué "Jouer", attend que 2 autres joueurs soient trouvés
 * EN_PARTIE  → actuellement dans une partie en cours
 *
 * Transitions valides :
 *   LIBRE → EN_ATTENTE  (rejoindre la file matchmaking)
 *   EN_ATTENTE → LIBRE  (quitter la file)
 *   EN_ATTENTE → EN_PARTIE  (matchmaking complet, partie créée)
 *   EN_PARTIE → LIBRE   (partie terminée)
 */
public enum StatutUtilisateur {
    LIBRE,
    EN_ATTENTE,
    EN_PARTIE
}
