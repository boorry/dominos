package com.dominos.application.stats;

/**
 * Résultat brut des statistiques calculées depuis la base.
 *
 * Objet intermédiaire entre StatsRepository et StatsService.
 * Le service le convertit en StatsReponse (DTO HTTP).
 */
public record StatsResultat(
    int partiesJouees,
    int partiesGagnees,
    int scoreTotal,
    int meilleurScore
) {
    public int partiesPerdues() {
        return partiesJouees - partiesGagnees;
    }

    public double scoreMoyen() {
        if (partiesJouees == 0) return 0.0;
        return Math.round((double) scoreTotal / partiesJouees * 10.0) / 10.0;
    }

    public static StatsResultat vide() {
        return new StatsResultat(0, 0, 0, 0);
    }
}
