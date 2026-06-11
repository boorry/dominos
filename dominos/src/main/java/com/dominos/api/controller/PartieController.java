package com.dominos.api.controller;

import com.dominos.api.dto.partie.PartieDtos;
import com.dominos.application.partie.PartieApplicationService;
import com.dominos.application.partie.PartieExceptions;
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
 *   GET  /api/parties/{id}              → état de la partie
 *   POST /api/parties/{id}/jouer        → jouer un domino
 *   POST /api/parties/{id}/jouer/cote   → confirmer le côté (si CHOIX_REQUIS)
 *   POST /api/parties/{id}/passer       → passer son tour
 */
@RestController
@RequestMapping("/api/parties")
public class PartieController {

    private final PartieApplicationService partieService;

    public PartieController(PartieApplicationService partieService) {
        this.partieService = partieService;
    }

    // -------------------------------------------------------------------------
    // GET /api/parties/{id}
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<PartieDtos.PartieReponse> getPartie(
            @PathVariable String id) {

        Partie partie = partieService.getPartie(id)
            .orElseThrow(() ->
                new PartieExceptions.PartieIntrouvableException(id));

        List<Domino> jouables = partieService.getDominosJouables(id);
        return ResponseEntity.ok(
            PartieDtos.PartieReponse.depuis(id, partie, jouables));
    }

    // -------------------------------------------------------------------------
    // POST /api/parties/{id}/jouer
    // -------------------------------------------------------------------------

    /**
     * Le joueur pose un domino.
     *
     * Réponse normale :
     * { "aJoue": true, "etat": "EN_COURS", "message": "..." }
     *
     * Réponse si ambiguïté (deux côtés possibles) :
     * {
     *   "aJoue": false,
     *   "etat": "CHOIX_REQUIS",
     *   "message": "Le domino [1|3] peut être posé des deux côtés...",
     *   "cotesPossibles": ["GAUCHE", "DROITE"]
     * }
     * → Le client doit appeler POST /jouer/cote
     */
    @PostMapping("/{id}/jouer")
    public ResponseEntity<PartieDtos.CoupReponse> jouer(
            @PathVariable String id,
            @RequestBody PartieDtos.JouerCoupRequete requete) {

        requete.valider();
        ResultatCoup r = partieService.jouerCoup(
            id, requete.utilisateurId(), requete.domino());
        return ResponseEntity.ok(PartieDtos.CoupReponse.depuis(r));
    }

    // -------------------------------------------------------------------------
    // POST /api/parties/{id}/jouer/cote — NOUVEAU
    // -------------------------------------------------------------------------

    /**
     * Le joueur confirme le côté de pose après un CHOIX_REQUIS.
     *
     * Requête :
     * {
     *   "utilisateurId": "uuid-alice",
     *   "domino": "[1|3]",
     *   "cote": "GAUCHE"
     * }
     *
     * Réponses : 200 OK, 400 Bad Request (côté invalide), 403 Forbidden
     */
    @PostMapping("/{id}/jouer/cote")
    public ResponseEntity<PartieDtos.CoupReponse> jouerAvecCote(
            @PathVariable String id,
            @RequestBody PartieDtos.JouerAvecCoteRequete requete) {

        requete.valider();
        ResultatCoup r = partieService.jouerCoupAvecCote(
            id, requete.utilisateurId(), requete.domino(), requete.cote());
        return ResponseEntity.ok(PartieDtos.CoupReponse.depuis(r));
    }

    // -------------------------------------------------------------------------
    // POST /api/parties/{id}/passer
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/passer")
    public ResponseEntity<PartieDtos.CoupReponse> passer(
            @PathVariable String id,
            @RequestBody PartieDtos.PasserTourRequete requete) {

        requete.valider();
        ResultatCoup r = partieService.passerTour(id, requete.utilisateurId());
        return ResponseEntity.ok(PartieDtos.CoupReponse.depuis(r));
    }
}
