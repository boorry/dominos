package com.dominos.api.controller;

import com.dominos.api.dto.stats.StatsReponse;
import com.dominos.api.dto.utilisateur.UtilisateurDtos;
import com.dominos.application.stats.StatsService;
import com.dominos.application.utilisateur.UtilisateurService;
import com.dominos.domain.utilisateur.Utilisateur;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST — gestion des comptes utilisateurs et statistiques.
 *
 * Endpoints :
 *   POST /api/users            → inscription
 *   POST /api/auth/login       → connexion
 *   GET  /api/users/{id}       → consulter un profil
 *   GET  /api/users/{id}/stats → statistiques d'un joueur
 */
@RestController
@RequestMapping("/api")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;
    private final StatsService       statsService;

    public UtilisateurController(UtilisateurService utilisateurService,
                                  StatsService statsService) {
        this.utilisateurService = utilisateurService;
        this.statsService       = statsService;
    }

    // -------------------------------------------------------------------------
    // POST /api/users — Inscription
    // -------------------------------------------------------------------------

    /**
     * Requête :
     *   POST /api/users
     *   { "pseudo": "alice_42", "email": "...", "motDePasse": "..." }
     *
     * Réponses : 201 Created, 409 Conflict, 400 Bad Request
     */
    @PostMapping("/users")
    public ResponseEntity<UtilisateurDtos.UtilisateurReponse> inscrire(
            @RequestBody UtilisateurDtos.InscriptionRequete requete) {

        requete.valider();

        Utilisateur utilisateur = utilisateurService.inscrire(
            requete.pseudo(),
            requete.email(),
            requete.motDePasse()
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(UtilisateurDtos.UtilisateurReponse.depuis(utilisateur));
    }

    // -------------------------------------------------------------------------
    // POST /api/auth/login — Connexion
    // -------------------------------------------------------------------------

    /**
     * Requête :
     *   POST /api/auth/login
     *   { "pseudo": "alice_42", "motDePasse": "..." }
     *
     * Réponses : 200 OK, 401 Unauthorized
     */
    @PostMapping("/auth/login")
    public ResponseEntity<UtilisateurDtos.ConnexionReponse> connecter(
            @RequestBody UtilisateurDtos.ConnexionRequete requete) {

        requete.valider();

        Utilisateur utilisateur = utilisateurService.authentifier(
            requete.pseudo(),
            requete.motDePasse()
        );

        return ResponseEntity.ok(
            UtilisateurDtos.ConnexionReponse.depuis(utilisateur));
    }

    // -------------------------------------------------------------------------
    // GET /api/users/{id} — Consulter un profil
    // -------------------------------------------------------------------------

    /**
     * Requête : GET /api/users/uuid-alice
     * Réponses : 200 OK, 404 Not Found
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UtilisateurDtos.UtilisateurReponse> consulter(
            @PathVariable String id) {

        Utilisateur utilisateur = utilisateurService.consulter(id);
        return ResponseEntity.ok(
            UtilisateurDtos.UtilisateurReponse.depuis(utilisateur));
    }

    // -------------------------------------------------------------------------
    // GET /api/users/{id}/stats — Statistiques
    // -------------------------------------------------------------------------

    /**
     * Retourne les statistiques complètes d'un joueur.
     *
     * Requête : GET /api/users/uuid-alice/stats
     *
     * Réponse 200 OK :
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
     *
     * Réponse 200 OK si aucune partie jouée :
     * {
     *   "utilisateurId": "uuid-alice",
     *   "pseudo": "alice_42",
     *   "partiesJouees": 0,
     *   ...tous à 0
     * }
     *
     * Réponse 404 Not Found → joueur inconnu
     */
    @GetMapping("/users/{id}/stats")
    public ResponseEntity<StatsReponse> getStats(@PathVariable String id) {
        return ResponseEntity.ok(statsService.getStats(id));
    }
}
