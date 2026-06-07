package com.dominos.application.stats;

/**
 * Port de lecture des statistiques d'un joueur.
 *
 * Lit depuis la table joueur_partie.
 * Implémenté par StatsRepositoryJpa.
 */
public interface StatsRepository {

    /**
     * Calcule les statistiques complètes d'un joueur.
     *
     * @param utilisateurId identifiant du joueur
     * @return ses statistiques (jamais null — vide si aucune partie)
     */
    StatsResultat calculerStats(String utilisateurId);
}
