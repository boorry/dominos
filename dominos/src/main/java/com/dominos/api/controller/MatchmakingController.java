package com.dominos.api.controller;

import com.dominos.api.dto.matchmaking.MatchmakingDtos;
import com.dominos.application.matchmaking.MatchmakingService;
import com.dominos.application.matchmaking.ResultatMatchmaking;
import com.dominos.application.partie.PartieApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST — matchmaking.
 *
 * Endpoints :
 *   POST   /api/matchmaking/join  → rejoindre la file
 *   DELETE /api/matchmaking/leave → quitter la file
 *   GET    /api/matchmaking/status → nombre de joueurs en attente
 */
@RestController
@RequestMapping("/api/matchmaking")
public class MatchmakingController {

    private final MatchmakingService       matchmakingService;
    private final PartieApplicationService partieService;

    public MatchmakingController(MatchmakingService matchmakingService,
                                  PartieApplicationService partieService) {
        this.matchmakingService = matchmakingService;
        this.partieService      = partieService;
    }

    // -------------------------------------------------------------------------
    // POST /api/matchmaking/join — Rejoindre la file
    // -------------------------------------------------------------------------

    /**
     * Un joueur rejoint la file d'attente.
     *
     * Requête :
     *   POST /api/matchmaking/join
     *   Content-Type: application/json
     *   { "utilisateurId": "uuid-alice" }
     *
     * Réponses :
     *   200 OK — en attente :
     *   {
     *     "statut": "EN_ATTENTE",
     *     "joueursEnAttente": 2,
     *     "partieId": null,
     *     "message": "En attente (2/3 joueurs)"
     *   }
     *
     *   200 OK — partie trouvée :
     *   {
     *     "statut": "PARTIE_TROUVEE",
     *     "joueursEnAttente": 0,
     *     "partieId": "uuid-partie",
     *     "joueurs": ["alice_42", "bob_99", "carl_7"],
     *     "message": "Partie trouvée ! Bonne chance."
     *   }
     *
     *   409 Conflict → joueur déjà en attente ou en partie
     */
    @PostMapping("/join")
    public ResponseEntity<MatchmakingDtos.MatchmakingReponse> rejoindre(
            @RequestBody MatchmakingDtos.MatchmakingRequete requete) {

        requete.valider();

        ResultatMatchmaking resultat =
            matchmakingService.rejoindreFile(requete.utilisateurId());

        // Si une partie est trouvée, on la crée et initialise automatiquement
        if (resultat.estPartieTrouvee()) {
            resultat.getPartie().ifPresent(partieEnAttente ->
                partieService.creerPartie(partieEnAttente));
        }

        return ResponseEntity.ok(
            MatchmakingDtos.MatchmakingReponse.depuis(resultat));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/matchmaking/leave — Quitter la file
    // -------------------------------------------------------------------------

    /**
     * Un joueur quitte la file d'attente.
     *
     * Requête :
     *   DELETE /api/matchmaking/leave
     *   Content-Type: application/json
     *   { "utilisateurId": "uuid-alice" }
     *
     * Réponses :
     *   200 OK          → file quittée avec succès
     *   400 Bad Request → joueur pas dans la file
     */
    @DeleteMapping("/leave")
    public ResponseEntity<Void> quitter(
            @RequestBody MatchmakingDtos.MatchmakingRequete requete) {

        requete.valider();
        matchmakingService.quitterFile(requete.utilisateurId());
        return ResponseEntity.ok().build();
    }

    // -------------------------------------------------------------------------
    // GET /api/matchmaking/status — Statut de la file
    // -------------------------------------------------------------------------

    /**
     * Retourne le nombre de joueurs actuellement en attente.
     *
     * Requête :
     *   GET /api/matchmaking/status
     *
     * Réponse :
     *   200 OK
     *   { "joueursEnAttente": 2 }
     */
    @GetMapping("/status")
    public ResponseEntity<java.util.Map<String, Integer>> statut() {
        return ResponseEntity.ok(
            java.util.Map.of("joueursEnAttente",
                matchmakingService.getNombreEnAttente()));
    }
}
