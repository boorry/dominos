package com.dominos.domain.tournoi;

/**
 * Phases d'un tournoi de dominos à 27 joueurs.
 *
 * GROUPES       → 9 groupes de 3 joueurs (9 parties simultanées)
 *                 Le gagnant de chaque groupe se qualifie.
 *
 * QUARTS        → 3 groupes de 3 gagnants de la phase de groupes
 *                 (3 parties, 3 qualifiés)
 *
 * DEMI_FINALES  → 1 partie avec les 3 gagnants des quarts
 *                 (1 partie, 1 qualifié + les 2 finalistes)
 *
 * FINALE        → La partie finale entre les 3 meilleurs joueurs
 *
 * Note : toutes les phases restent à 3 joueurs par partie.
 */
public enum PhaseTournoi {
    GROUPES,
    QUARTS,
    DEMI_FINALES,
    FINALE;

    /**
     * Retourne la phase suivante.
     * @throws IllegalStateException si on est déjà en finale
     */
    public PhaseTournoi suivante() {
        return switch (this) {
            case GROUPES      -> QUARTS;
            case QUARTS       -> DEMI_FINALES;
            case DEMI_FINALES -> FINALE;
            case FINALE       -> throw new IllegalStateException(
                "La finale est la dernière phase — pas de phase suivante");
        };
    }

    /**
     * Nombre de matchs pour cette phase (avec 27 joueurs).
     */
    public int nombreDeMatchs() {
        return switch (this) {
            case GROUPES      -> 9;
            case QUARTS       -> 3;
            case DEMI_FINALES -> 1;
            case FINALE       -> 1;
        };
    }

    /**
     * Nombre de joueurs qualifiés depuis cette phase.
     */
    public int nombreDeQualifies() {
        return switch (this) {
            case GROUPES      -> 9;  // 1 gagnant par groupe
            case QUARTS       -> 3;  // 1 gagnant par match
            case DEMI_FINALES -> 3;  // les 3 joueurs de la demi vont en finale
            case FINALE       -> 1;  // le champion
        };
    }
}
