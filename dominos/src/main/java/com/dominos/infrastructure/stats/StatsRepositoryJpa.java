package com.dominos.infrastructure.stats;

import com.dominos.application.stats.StatsRepository;
import com.dominos.application.stats.StatsResultat;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 * Implémentation JPA des statistiques.
 *
 * Utilise EntityManager directement pour des requêtes JPQL
 * d'agrégation (COUNT, SUM, MAX) sur la table joueur_partie.
 *
 * Pourquoi EntityManager plutôt que JpaRepository ?
 *   Les requêtes d'agrégation complexes (SUM, MAX, COUNT avec
 *   conditions multiples) sont plus lisibles en JPQL qu'en
 *   méthodes Spring Data. EntityManager nous donne plus de contrôle.
 *
 * JPQL (Java Persistence Query Language) :
 *   Similaire à SQL mais opère sur les entités Java, pas les tables.
 *   "FROM JoueurPartieEntity" au lieu de "FROM joueur_partie"
 */
@Component
public class StatsRepositoryJpa implements StatsRepository {

    private final EntityManager em;

    public StatsRepositoryJpa(EntityManager em) {
        this.em = em;
    }

    @Override
    public StatsResultat calculerStats(String utilisateurId) {

        // --- Nombre de parties jouées ---
        Long partiesJouees = em.createQuery(
                "SELECT COUNT(jp) FROM JoueurPartieEntity jp " +
                "WHERE jp.utilisateurId = :id", Long.class)
            .setParameter("id", utilisateurId)
            .getSingleResult();

        if (partiesJouees == 0) {
            return StatsResultat.vide();
        }

        // --- Nombre de parties gagnées (position = 1) ---
        Long partiesGagnees = em.createQuery(
                "SELECT COUNT(jp) FROM JoueurPartieEntity jp " +
                "WHERE jp.utilisateurId = :id AND jp.position = 1", Long.class)
            .setParameter("id", utilisateurId)
            .getSingleResult();

        // --- Score total ---
        Long scoreTotal = em.createQuery(
                "SELECT COALESCE(SUM(jp.score), 0) FROM JoueurPartieEntity jp " +
                "WHERE jp.utilisateurId = :id", Long.class)
            .setParameter("id", utilisateurId)
            .getSingleResult();

        // --- Meilleur score ---
        Integer meilleurScore = em.createQuery(
                "SELECT COALESCE(MAX(jp.score), 0) FROM JoueurPartieEntity jp " +
                "WHERE jp.utilisateurId = :id", Integer.class)
            .setParameter("id", utilisateurId)
            .getSingleResult();

        return new StatsResultat(
            partiesJouees.intValue(),
            partiesGagnees.intValue(),
            scoreTotal.intValue(),
            meilleurScore != null ? meilleurScore : 0
        );
    }
}
