package com.dominos.api.controller;

import com.dominos.application.utilisateur.UtilisateurExceptions;
import com.dominos.application.utilisateur.UtilisateurService;
import com.dominos.domain.utilisateur.StatutUtilisateur;
import com.dominos.domain.utilisateur.Utilisateur;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests du UtilisateurController.
 *
 * @WebMvcTest :
 *   - Démarre uniquement la couche web (controllers, filtres)
 *   - Ne démarre PAS la base de données ni les services réels
 *   - Très rapide — idéal pour tester les endpoints HTTP
 *
 * @MockBean :
 *   - Remplace le vrai UtilisateurService par un mock (Mockito)
 *   - On contrôle exactement ce que le service retourne
 *   - On teste uniquement le comportement HTTP du controller
 */
@WebMvcTest(UtilisateurController.class)
@DisplayName("UtilisateurController")
class UtilisateurControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UtilisateurService utilisateurService;

    private Utilisateur utilisateurAlice() {
        return new Utilisateur(
            "id-alice", "alice_42", "alice@example.com",
            "sel$hash", StatutUtilisateur.LIBRE, LocalDateTime.now()
        );
    }

    // -------------------------------------------------------------------------
    // POST /api/users — Inscription
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/users")
    class Inscription {

        @Test
        @DisplayName("inscription réussie → 201 avec le profil")
        void inscriptionReussie() throws Exception {
            when(utilisateurService.inscrire("alice_42", "alice@example.com", "motdepasse123"))
                .thenReturn(utilisateurAlice());

            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of(
                        "pseudo",     "alice_42",
                        "email",      "alice@example.com",
                        "motDePasse", "motdepasse123"
                    ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pseudo").value("alice_42"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.statut").value("LIBRE"));
        }

        @Test
        @DisplayName("pseudo déjà pris → 409 Conflict")
        void pseudoDejaPris() throws Exception {
            when(utilisateurService.inscrire(anyString(), anyString(), anyString()))
                .thenThrow(new UtilisateurExceptions.PseudoDejaPrisException("alice_42"));

            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of(
                        "pseudo",     "alice_42",
                        "email",      "alice@example.com",
                        "motDePasse", "motdepasse123"
                    ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        @DisplayName("pseudo manquant → 400 Bad Request")
        void pseudoManquant() throws Exception {
            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of(
                        "email",      "alice@example.com",
                        "motDePasse", "motdepasse123"
                    ))))
                .andExpect(status().isBadRequest());
        }
    }

    // -------------------------------------------------------------------------
    // POST /api/auth/login — Connexion
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/auth/login")
    class Connexion {

        @Test
        @DisplayName("connexion réussie → 200 avec token")
        void connexionReussie() throws Exception {
            when(utilisateurService.authentifier("alice_42", "motdepasse123"))
                .thenReturn(utilisateurAlice());

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of(
                        "pseudo",     "alice_42",
                        "motDePasse", "motdepasse123"
                    ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("id-alice"))
                .andExpect(jsonPath("$.utilisateur.pseudo").value("alice_42"));
        }

        @Test
        @DisplayName("mauvais mot de passe → 401 Unauthorized")
        void mauvaisMotDePasse() throws Exception {
            when(utilisateurService.authentifier(anyString(), anyString()))
                .thenThrow(new UtilisateurExceptions.AuthentificationException());

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of(
                        "pseudo",     "alice_42",
                        "motDePasse", "mauvais"
                    ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        }
    }
}
