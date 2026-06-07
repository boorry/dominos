package com.dominos.api.controller;

import com.dominos.api.dto.partie.PartieDtos;
import com.dominos.application.partie.PartieApplicationService;
import com.dominos.domain.Domino;
import com.dominos.domain.Partie;
import com.dominos.moteur.ResultatCoup;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST — déroulement d'une partie.
 *
 * Endpoints :
 *   GET  /api/parties/{id}         → état complet de la partie
 *   POST /api/parties/{id}/jouer   → jouer un domino
 *   POST /api/parties/{id}/passer  → passer son tour
 */
@RestController
@RequestMapping("/api/parties")
public class PartieController {

    private final PartieApplicationService partieService;

    public PartieController(PartieApplicationService partieService) {
        this.partieService = partieService;
    }

    // -------------------------------------------------------------------------
    // GET /api/parties/{id} — État de la partie
    // -------------------------------------------------------------------------

    /**
     * Retourne l'état complet de la partie.
     *
     * Requête :
     *   GET /api/parties/uuid-partie
     *
     * Réponse :
     *   200 OK
     *   {
     *     "id": "uuid-partie",
     *     "etat": "EN_COURS",
     *     "manche": 1,
     *     "plateau": ["[3|5]", "[5|2]"],
     *     "joueurCourant": "Alice",
     *     "scores": { "Alice": 0, "Bob": 0, "Carl": 0 },
     *     "mainJoueurCourant": ["[1|2]", "[6|3]"],
     *     "dominosJouables": ["[1|2]"]
     *   }
     *
     *   404 Not Found → partie inexistante ou terminée
     */
    @GetMapping("/{id}")
    public ResponseEntity<PartieDtos.PartieReponse> getPartie(
            @PathVariable String id) {

        Partie partie = partieService.getPartie(id)
            .orElseThrow(() ->
                new com.dominos.application.partie.PartieExceptions
                    .PartieIntrouvableException(id));

        List<Domino> jouables = partieService.getDominosJouables(id);

        return ResponseEntity.ok(
            PartieDtos.PartieReponse.depuis(id, partie, jouables));
    }

    // -------------------------------------------------------------------------
    // POST /api/parties/{id}/jouer — Jouer un domino
    // -------------------------------------------------------------------------

    /**
     * Le joueur courant pose un domino.
     *
     * Requête :
     *   POST /api/parties/uuid-partie/jouer
     *   Content-Type: application/json
     *   {
     *     "utilisateurId": "uuid-alice",
     *     "domino": "[3|5]"
     *   }
     *
     * Réponses :
     *   200 OK — coup joué :
     *   {
     *     "aJoue": true,
     *     "etat": "EN_COURS",
     *     "message": "Alice pose [3|5] à droite.",
     *     "gagnant": null
     *   }
     *
     *   200 OK — victoire :
     *   {
     *     "aJoue": true,
     *     "etat": "VICTOIRE",
     *     "message": "Alice gagne la manche 1 et marque 27 points !",
     *     "gagnant": "Alice"
     *   }
     *
     *   400 Bad Request → domino non jouable ou format invalide
     *   403 Forbidden   → pas le tour de ce joueur
     *   404 Not Found   → partie inexistante
     */
    @PostMapping("/{id}/jouer")
    public ResponseEntity<PartieDtos.CoupReponse> jouer(
            @PathVariable String id,
            @RequestBody PartieDtos.JouerCoupRequete requete) {

        requete.valider();

        ResultatCoup resultat = partieService.jouerCoup(
            id,
            requete.utilisateurId(),
            requete.domino()
        );

        return ResponseEntity.ok(PartieDtos.CoupReponse.depuis(resultat));
    }

    // -------------------------------------------------------------------------
    // POST /api/parties/{id}/passer — Passer son tour
    // -------------------------------------------------------------------------

    /**
     * Le joueur courant passe son tour.
     *
     * Requête :
     *   POST /api/parties/uuid-partie/passer
     *   Content-Type: application/json
     *   { "utilisateurId": "uuid-alice" }
     *
     * Réponses :
     *   200 OK — tour passé :
     *   {
     *     "aJoue": false,
     *     "etat": "EN_COURS",
     *     "message": "Alice passe son tour.",
     *     "gagnant": null
     *   }
     *
     *   200 OK — blocage :
     *   {
     *     "aJoue": false,
     *     "etat": "BLOCAGE",
     *     "message": "Jeu bloqué ! Alice gagne la manche.",
     *     "gagnant": "Alice"
     *   }
     *
     *   403 Forbidden → pas le tour de ce joueur
     *   404 Not Found → partie inexistante
     */
    @PostMapping("/{id}/passer")
    public ResponseEntity<PartieDtos.CoupReponse> passer(
            @PathVariable String id,
            @RequestBody PartieDtos.PasserTourRequete requete) {

        requete.valider();

        ResultatCoup resultat = partieService.passerTour(
            id,
            requete.utilisateurId()
        );

        return ResponseEntity.ok(PartieDtos.CoupReponse.depuis(resultat));
    }
}
