package com.dominos.api.controller;

import com.dominos.api.dto.tournoi.TournoiDtos;
import com.dominos.application.tournoi.TournoiService;
import com.dominos.domain.tournoi.Tournoi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST — tournois.
 *
 * Endpoints :
 *   POST   /api/tournois                        → créer un tournoi
 *   GET    /api/tournois                        → lister tous les tournois
 *   GET    /api/tournois/inscription            → tournois ouverts
 *   GET    /api/tournois/{id}                   → détails d'un tournoi
 *   POST   /api/tournois/{id}/inscrire          → inscrire un joueur
 *   POST   /api/tournois/{id}/demarrer          → démarrer le tournoi
 *   GET    /api/tournois/{id}/bracket           → bracket complet
 *   POST   /api/tournois/{id}/resultat          → enregistrer résultat d'un match
 */
@RestController
@RequestMapping("/api/tournois")
public class TournoiController {

    private final TournoiService tournoiService;

    public TournoiController(TournoiService tournoiService) {
        this.tournoiService = tournoiService;
    }

    // -------------------------------------------------------------------------
    // POST /api/tournois — Créer un tournoi
    // -------------------------------------------------------------------------

    /**
     * Requête :
     *   POST /api/tournois
     *   { "nom": "Grand Tournoi Printemps 2024" }
     *
     * Réponse 201 Created :
     *   { "id": "uuid", "nom": "...", "statut": "INSCRIPTION", ... }
     */
    @PostMapping
    public ResponseEntity<TournoiDtos.TournoiReponse> creer(
            @RequestBody TournoiDtos.CreerTournoiRequete requete) {

        requete.valider();
        Tournoi tournoi = tournoiService.creer(requete.nom());
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(TournoiDtos.TournoiReponse.depuis(tournoi));
    }

    // -------------------------------------------------------------------------
    // GET /api/tournois — Lister tous les tournois
    // -------------------------------------------------------------------------

    @GetMapping
    public ResponseEntity<List<TournoiDtos.TournoiReponse>> listerTous() {
        List<TournoiDtos.TournoiReponse> tournois = tournoiService.listerTous()
            .stream().map(TournoiDtos.TournoiReponse::depuis).toList();
        return ResponseEntity.ok(tournois);
    }

    // -------------------------------------------------------------------------
    // GET /api/tournois/inscription — Tournois ouverts
    // -------------------------------------------------------------------------

    @GetMapping("/inscription")
    public ResponseEntity<List<TournoiDtos.TournoiReponse>> listerEnInscription() {
        List<TournoiDtos.TournoiReponse> tournois = tournoiService.listerEnInscription()
            .stream().map(TournoiDtos.TournoiReponse::depuis).toList();
        return ResponseEntity.ok(tournois);
    }

    // -------------------------------------------------------------------------
    // GET /api/tournois/{id} — Détails d'un tournoi
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<TournoiDtos.TournoiReponse> consulter(
            @PathVariable String id) {

        Tournoi tournoi = tournoiService.consulter(id);
        return ResponseEntity.ok(TournoiDtos.TournoiReponse.depuis(tournoi));
    }

    // -------------------------------------------------------------------------
    // POST /api/tournois/{id}/inscrire — Inscrire un joueur
    // -------------------------------------------------------------------------

    /**
     * Requête :
     *   POST /api/tournois/{id}/inscrire
     *   { "utilisateurId": "uuid-alice" }
     *
     * Réponses : 200 OK, 409 Conflict (déjà inscrit / complet), 404 Not Found
     */
    @PostMapping("/{id}/inscrire")
    public ResponseEntity<TournoiDtos.TournoiReponse> inscrire(
            @PathVariable String id,
            @RequestBody TournoiDtos.InscrireRequete requete) {

        requete.valider();
        Tournoi tournoi = tournoiService.inscrire(id, requete.utilisateurId());
        return ResponseEntity.ok(TournoiDtos.TournoiReponse.depuis(tournoi));
    }

    // -------------------------------------------------------------------------
    // POST /api/tournois/{id}/demarrer — Démarrer le tournoi
    // -------------------------------------------------------------------------

    /**
     * Requête : POST /api/tournois/{id}/demarrer
     *
     * Réponses : 200 OK, 400 Bad Request (pas 27 joueurs), 404 Not Found
     */
    @PostMapping("/{id}/demarrer")
    public ResponseEntity<TournoiDtos.TournoiReponse> demarrer(
            @PathVariable String id) {

        Tournoi tournoi = tournoiService.demarrer(id);
        return ResponseEntity.ok(TournoiDtos.TournoiReponse.depuis(tournoi));
    }

    // -------------------------------------------------------------------------
    // GET /api/tournois/{id}/bracket — Bracket complet
    // -------------------------------------------------------------------------

    /**
     * Retourne le bracket complet avec tous les matchs.
     *
     * Requête : GET /api/tournois/{id}/bracket
     */
    @GetMapping("/{id}/bracket")
    public ResponseEntity<TournoiDtos.BracketReponse> getBracket(
            @PathVariable String id) {

        Tournoi tournoi = tournoiService.consulter(id);
        return ResponseEntity.ok(TournoiDtos.BracketReponse.depuis(tournoi));
    }

    // -------------------------------------------------------------------------
    // POST /api/tournois/{id}/resultat — Enregistrer résultat d'un match
    // -------------------------------------------------------------------------

    /**
     * Enregistre le gagnant d'un match et avance le bracket.
     *
     * Requête :
     *   POST /api/tournois/{id}/resultat
     *   { "matchId": "uuid-match", "gagnantId": "uuid-alice" }
     *
     * Réponses : 200 OK, 400 Bad Request, 404 Not Found
     */
    @PostMapping("/{id}/resultat")
    public ResponseEntity<TournoiDtos.TournoiReponse> enregistrerResultat(
            @PathVariable String id,
            @RequestBody TournoiDtos.ResultatMatchRequete requete) {

        requete.valider();
        Tournoi tournoi = tournoiService.enregistrerResultat(
            id, requete.matchId(), requete.gagnantId());
        return ResponseEntity.ok(TournoiDtos.TournoiReponse.depuis(tournoi));
    }
}
