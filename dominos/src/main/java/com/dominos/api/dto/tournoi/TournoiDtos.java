package com.dominos.api.dto.tournoi;

import com.dominos.domain.tournoi.MatchTournoi;
import com.dominos.domain.tournoi.PhaseTournoi;
import com.dominos.domain.tournoi.StatutTournoi;
import com.dominos.domain.tournoi.Tournoi;

import java.util.List;

/**
 * DTOs liés aux tournois.
 */
public final class TournoiDtos {

    private TournoiDtos() {}

    // -------------------------------------------------------------------------
    // Requêtes
    // -------------------------------------------------------------------------

    public record CreerTournoiRequete(String nom) {
        public void valider() {
            if (nom == null || nom.isBlank())
                throw new IllegalArgumentException("Le nom du tournoi est requis");
        }
    }

    public record InscrireRequete(String utilisateurId) {
        public void valider() {
            if (utilisateurId == null || utilisateurId.isBlank())
                throw new IllegalArgumentException("L'identifiant utilisateur est requis");
        }
    }

    public record ResultatMatchRequete(String matchId, String gagnantId) {
        public void valider() {
            if (matchId == null || matchId.isBlank())
                throw new IllegalArgumentException("L'identifiant du match est requis");
            if (gagnantId == null || gagnantId.isBlank())
                throw new IllegalArgumentException("L'identifiant du gagnant est requis");
        }
    }

    // -------------------------------------------------------------------------
    // Réponses
    // -------------------------------------------------------------------------

    /**
     * Résumé d'un tournoi.
     *
     * {
     *   "id": "uuid-tournoi",
     *   "nom": "Tournoi #1",
     *   "statut": "EN_COURS",
     *   "phaseActuelle": "GROUPES",
     *   "nbJoueursInscrits": 27,
     *   "nbJoueursRequis": 27,
     *   "championId": null
     * }
     */
    public record TournoiReponse(
        String       id,
        String       nom,
        StatutTournoi statut,
        PhaseTournoi  phaseActuelle,
        int          nbJoueursInscrits,
        int          nbJoueursRequis,
        String       championId,
        String       dateCreation
    ) {
        public static TournoiReponse depuis(Tournoi t) {
            return new TournoiReponse(
                t.getId(),
                t.getNom(),
                t.getStatut(),
                t.getPhaseActuelle(),
                t.getNbJoueursInscrits(),
                Tournoi.NB_JOUEURS_REQUIS,
                t.getChampionId(),
                t.getDateCreation().toString()
            );
        }
    }

    /**
     * Bracket complet du tournoi.
     */
    public record BracketReponse(
        String            tournoiId,
        String            nom,
        PhaseTournoi      phaseActuelle,
        List<MatchReponse> matchsPhaseActuelle,
        List<MatchReponse> tousLesMatchs
    ) {
        public static BracketReponse depuis(Tournoi t) {
            List<MatchReponse> matchsActuels = t.getMatchsPhaseActuelle()
                .stream().map(MatchReponse::depuis).toList();
            List<MatchReponse> tousMatchs = t.getMatchs()
                .stream().map(MatchReponse::depuis).toList();
            return new BracketReponse(
                t.getId(), t.getNom(), t.getPhaseActuelle(),
                matchsActuels, tousMatchs);
        }
    }

    /**
     * Résumé d'un match.
     */
    public record MatchReponse(
        String                  id,
        PhaseTournoi            phase,
        int                     numeroGroupe,
        List<String>            joueursIds,
        String                  partieId,
        String                  gagnantId,
        MatchTournoi.StatutMatch statut
    ) {
        public static MatchReponse depuis(MatchTournoi m) {
            return new MatchReponse(
                m.getId(), m.getPhase(), m.getNumeroGroupe(),
                m.getJoueursIds(), m.getPartieId(),
                m.getGagnantId(), m.getStatut());
        }
    }
}
