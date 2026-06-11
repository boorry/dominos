package com.dominos.domain.tournoi;

/**
 * Cycle de vie d'un tournoi.
 *
 * INSCRIPTION  → le tournoi est ouvert aux inscriptions
 * EN_COURS     → le tournoi a démarré, les parties se jouent
 * TERMINE      → le tournoi est terminé, un champion est désigné
 * ANNULE       → le tournoi a été annulé (pas assez de joueurs, etc.)
 *
 * Transitions valides :
 *   INSCRIPTION → EN_COURS  (demarrer)
 *   INSCRIPTION → ANNULE    (annuler)
 *   EN_COURS    → TERMINE   (fin automatique)
 *   EN_COURS    → ANNULE    (annulation forcée)
 */
public enum StatutTournoi {
    INSCRIPTION,
    EN_COURS,
    TERMINE,
    ANNULE
}
