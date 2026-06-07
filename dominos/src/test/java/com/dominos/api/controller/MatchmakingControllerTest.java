package com.dominos.api.controller;

import com.dominos.application.matchmaking.MatchmakingService;
import com.dominos.application.matchmaking.ResultatMatchmaking;
import com.dominos.application.partie.PartieApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MatchmakingController.class)
@DisplayName("MatchmakingController")
class MatchmakingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MatchmakingService matchmakingService;

    @MockBean
    private PartieApplicationService partieService;

    // -------------------------------------------------------------------------
    // POST /api/matchmaking/join
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/matchmaking/join")
    class RejoindreFile {

        @Test
        @DisplayName("joueur rejoint → EN_ATTENTE 1/3")
        void joueurEnAttente() throws Exception {
            when(matchmakingService.rejoindreFile("id-alice"))
                .thenReturn(ResultatMatchmaking.enAttente(1));

            mockMvc.perform(post("/api/matchmaking/join")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        Map.of("utilisateurId", "id-alice"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE"))
                .andExpect(jsonPath("$.joueursEnAttente").value(1));
        }

        @Test
        @DisplayName("utilisateurId manquant → 400")
        void utilisateurIdManquant() throws Exception {
            mockMvc.perform(post("/api/matchmaking/join")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isBadRequest());
        }
    }

    // -------------------------------------------------------------------------
    // DELETE /api/matchmaking/leave
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /api/matchmaking/leave → 200 OK")
    void quitterFile() throws Exception {
        mockMvc.perform(delete("/api/matchmaking/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    Map.of("utilisateurId", "id-alice"))))
            .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // GET /api/matchmaking/status
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/matchmaking/status → nombre en attente")
    void statut() throws Exception {
        when(matchmakingService.getNombreEnAttente()).thenReturn(2);

        mockMvc.perform(get("/api/matchmaking/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.joueursEnAttente").value(2));
    }
}
