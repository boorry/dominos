package com.dominos.api.dto.matchmaking;

import com.dominos.application.matchmaking.ResultatMatchmaking;
import com.dominos.domain.utilisateur.Utilisateur;

import java.util.List;

/**
 * DTOs liés au matchmaking.
 */
public final class MatchmakingDtos {

    private MatchmakingDtos() {}

    // -------------------------------------------------------------------------
    // Requêtes
    // -------------------------------------------------------------------------

    /**
     * POST /api/matchmaking/join et DELETE /api/matchmaking/leave
     *
     * Exemple JSON reçu :
     * {
     *   "utilisateurId": "uuid-alice"
     * }
     *
     * Note : à l'Étape 6 avec JWT, l'id sera extrait du token
     * et ce DTO ne sera plus nécessaire.
     */
    public record MatchmakingRequete(String utilisateurId) {
        public void valider() {
            if (utilisateurId == null || utilisateurId.isBlank())
                throw new IllegalArgumentException("L'identifiant utilisateur est requis");
        }
    }

    // -------------------------------------------------------------------------
    // Réponses
    // -------------------------------------------------------------------------

    /**
     * Réponse après avoir rejoint la file.
     *
     * Cas 1 — En attente :
     * {
     *   "statut": "EN_ATTENTE",
     *   "joueursEnAttente": 2,
     *   "partieId": null,
     *   "joueurs": null,
     *   "message": "En attente (2/3 joueurs)"
     * }
     *
     * Cas 2 — Partie trouvée :
     * {
     *   "statut": "PARTIE_TROUVEE",
     *   "joueursEnAttente": 0,
     *   "partieId": "uuid-partie",
     *   "joueurs": ["alice_42", "bob_99", "carl_7"],
     *   "message": "Partie trouvée ! Bonne chance."
     * }
     */
    public record MatchmakingReponse(
        String statut,
        int joueursEnAttente,
        String partieId,
        List<String> joueurs,
        String message
    ) {
        public static MatchmakingReponse depuis(ResultatMatchmaking resultat) {
            if (resultat.estEnAttente()) {
                return new MatchmakingReponse(
                    "EN_ATTENTE",
                    resultat.getJoueursEnAttente(),
                    null,
                    null,
                    "En attente (" + resultat.getJoueursEnAttente() + "/3 joueurs)"
                );
            }

            // Partie trouvée
            var partie = resultat.getPartie().orElseThrow();
            List<String> pseudos = partie.getJoueurs()
                .stream()
                .map(Utilisateur::getPseudo)
                .toList();

            return new MatchmakingReponse(
                "PARTIE_TROUVEE",
                0,
                partie.getId(),
                pseudos,
                "Partie trouvée ! Bonne chance."
            );
        }
    }
}
