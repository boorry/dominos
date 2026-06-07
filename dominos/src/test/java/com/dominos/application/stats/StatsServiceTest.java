package com.dominos.application.stats;

import com.dominos.application.utilisateur.UtilisateurExceptions;
import com.dominos.api.dto.stats.StatsReponse;
import com.dominos.domain.utilisateur.StatutUtilisateur;
import com.dominos.domain.utilisateur.Utilisateur;
import com.dominos.infrastructure.utilisateur.UtilisateurRepositoryMemoire;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StatsService")
class StatsServiceTest {

    private StatsService             service;
    private UtilisateurRepositoryMemoire utilisateurRepo;
    private StatsRepositoryFake      statsRepo;
    private Utilisateur              alice;

    @BeforeEach
    void setUp() {
        utilisateurRepo = new UtilisateurRepositoryMemoire();
        statsRepo       = new StatsRepositoryFake();
        service         = new StatsService(utilisateurRepo, statsRepo);

        alice = new Utilisateur(
            "id-alice", "alice_42", "alice@example.com",
            "sel$hash", StatutUtilisateur.LIBRE, LocalDateTime.now()
        );
        utilisateurRepo.sauvegarder(alice);
    }

    // -------------------------------------------------------------------------
    // Cas nominaux
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getStats()")
    class GetStats {

        @Test
        @DisplayName("joueur sans partie → stats vides à 0")
        void sansPartie() {
            statsRepo.setResultat(StatsResultat.vide());

            StatsReponse stats = service.getStats("id-alice");

            assertEquals("id-alice",  stats.utilisateurId());
            assertEquals("alice_42",  stats.pseudo());
            assertEquals(0, stats.partiesJouees());
            assertEquals(0, stats.partiesGagnees());
            assertEquals(0, stats.scoreTotal());
            assertEquals(0.0, stats.scoreMoyen());
        }

        @Test
        @DisplayName("joueur avec parties → stats correctes")
        void avecParties() {
            statsRepo.setResultat(new StatsResultat(5, 3, 245, 87));

            StatsReponse stats = service.getStats("id-alice");

            assertEquals(5,    stats.partiesJouees());
            assertEquals(3,    stats.partiesGagnees());
            assertEquals(2,    stats.partiesPerdues());
            assertEquals(245,  stats.scoreTotal());
            assertEquals(49.0, stats.scoreMoyen());
            assertEquals(87,   stats.meilleurScore());
        }

        @Test
        @DisplayName("scoreMoyen arrondi à 1 décimale")
        void scoreMoyenArrondi() {
            // 10 / 3 = 3.3333... → doit retourner 3.3
            statsRepo.setResultat(new StatsResultat(3, 1, 10, 5));

            StatsReponse stats = service.getStats("id-alice");
            assertEquals(3.3, stats.scoreMoyen());
        }

        @Test
        @DisplayName("id inconnu → UtilisateurIntrouvableException")
        void idInconnu() {
            assertThrows(UtilisateurExceptions.UtilisateurIntrouvableException.class,
                () -> service.getStats("id-inconnu"));
        }
    }

    // -------------------------------------------------------------------------
    // Fake repository pour les tests
    // -------------------------------------------------------------------------

    /**
     * Implémentation test de StatsRepository.
     * Retourne un résultat configurable sans base de données.
     */
    static class StatsRepositoryFake implements StatsRepository {
        private StatsResultat resultat = StatsResultat.vide();

        public void setResultat(StatsResultat r) { this.resultat = r; }

        @Override
        public StatsResultat calculerStats(String utilisateurId) {
            return resultat;
        }
    }
}
