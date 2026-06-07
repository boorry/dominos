package com.dominos.api.dto.stats;

/**
 * DTO des statistiques d'un joueur.
 *
 * Retourné par GET /api/users/{id}/stats
 *
 * Exemple JSON :
 * {
 *   "utilisateurId": "uuid-alice",
 *   "pseudo": "alice_42",
 *   "partiesJouees": 5,
 *   "partiesGagnees": 3,
 *   "partiesPerdues": 2,
 *   "scoreTotal": 245,
 *   "scoreMoyen": 49.0,
 *   "meilleurScore": 87
 * }
 */
public record StatsReponse(
    String  utilisateurId,
    String  pseudo,
    int     partiesJouees,
    int     partiesGagnees,
    int     partiesPerdues,
    int     scoreTotal,
    double  scoreMoyen,
    int     meilleurScore
) {
    /**
     * Retourné quand le joueur n'a encore joué aucune partie.
     */
    public static StatsReponse vide(String utilisateurId, String pseudo) {
        return new StatsReponse(utilisateurId, pseudo, 0, 0, 0, 0, 0.0, 0);
    }
}
