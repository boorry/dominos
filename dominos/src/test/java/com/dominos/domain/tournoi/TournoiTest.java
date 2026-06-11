package com.dominos.domain.tournoi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tournoi")
class TournoiTest {

    private Tournoi tournoi;

    @BeforeEach
    void setUp() {
        tournoi = new Tournoi("Grand Tournoi");
    }

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("créé avec statut INSCRIPTION")
    void statutInscriptionParDefaut() {
        assertEquals(StatutTournoi.INSCRIPTION, tournoi.getStatut());
        assertEquals(0, tournoi.getNbJoueursInscrits());
    }

    @Test
    @DisplayName("nom vide → exception")
    void nomVide() {
        assertThrows(IllegalArgumentException.class, () -> new Tournoi(""));
    }

    // -------------------------------------------------------------------------
    // Inscriptions
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Inscriptions")
    class Inscriptions {

        @Test
        @DisplayName("inscrire 27 joueurs → tournoi complet")
        void inscrire27Joueurs() {
            inscrireTousLesJoueurs(tournoi);
            assertTrue(tournoi.estComplet());
            assertEquals(27, tournoi.getNbJoueursInscrits());
        }

        @Test
        @DisplayName("joueur déjà inscrit → exception")
        void dejaInscrit() {
            tournoi.inscrire("id-1");
            assertThrows(IllegalArgumentException.class,
                () -> tournoi.inscrire("id-1"));
        }

        @Test
        @DisplayName("28e joueur → exception")
        void tropDeJoueurs() {
            inscrireTousLesJoueurs(tournoi);
            assertThrows(IllegalStateException.class,
                () -> tournoi.inscrire("id-28"));
        }

        @Test
        @DisplayName("inscription après démarrage → exception")
        void inscriptionApresDeMarrage() {
            inscrireTousLesJoueurs(tournoi);
            tournoi.demarrer();
            assertThrows(IllegalStateException.class,
                () -> tournoi.inscrire("id-nouveau"));
        }
    }

    // -------------------------------------------------------------------------
    // Démarrage
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Démarrage")
    class Demarrage {

        @Test
        @DisplayName("démarrer avec 27 joueurs → EN_COURS, phase GROUPES")
        void demarrerAvec27Joueurs() {
            inscrireTousLesJoueurs(tournoi);
            tournoi.demarrer();
            assertEquals(StatutTournoi.EN_COURS,   tournoi.getStatut());
            assertEquals(PhaseTournoi.GROUPES,     tournoi.getPhaseActuelle());
        }

        @Test
        @DisplayName("démarrer crée 9 matchs de groupes")
        void creer9Matchs() {
            inscrireTousLesJoueurs(tournoi);
            tournoi.demarrer();
            assertEquals(9, tournoi.getMatchsPhaseActuelle().size());
        }

        @Test
        @DisplayName("chaque match a exactement 3 joueurs")
        void chaque3Joueurs() {
            inscrireTousLesJoueurs(tournoi);
            tournoi.demarrer();
            tournoi.getMatchsPhaseActuelle()
                .forEach(m -> assertEquals(3, m.getJoueursIds().size()));
        }

        @Test
        @DisplayName("démarrer sans 27 joueurs → exception")
        void demarrerSans27() {
            tournoi.inscrire("id-1");
            assertThrows(IllegalStateException.class, tournoi::demarrer);
        }

        @Test
        @DisplayName("les 27 joueurs sont répartis sans doublon")
        void sansDoublonDansGroupes() {
            inscrireTousLesJoueurs(tournoi);
            tournoi.demarrer();
            long total = tournoi.getMatchsPhaseActuelle().stream()
                .flatMap(m -> m.getJoueursIds().stream())
                .distinct().count();
            assertEquals(27, total);
        }
    }

    // -------------------------------------------------------------------------
    // Avancement du bracket
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Avancement bracket")
    class AvancementBracket {

        @BeforeEach
        void demarrer() {
            inscrireTousLesJoueurs(tournoi);
            tournoi.demarrer();
        }

        @Test
        @DisplayName("terminer les 9 matchs de groupes → passage aux quarts")
        void passerAuxQuarts() {
            terminerTousLesMatchs(tournoi, PhaseTournoi.GROUPES);
            assertEquals(PhaseTournoi.QUARTS, tournoi.getPhaseActuelle());
        }

        @Test
        @DisplayName("après groupes → 3 matchs de quarts créés")
        void troisMatchsQuarts() {
            terminerTousLesMatchs(tournoi, PhaseTournoi.GROUPES);
            assertEquals(3, tournoi.getMatchsPhaseActuelle().size());
        }

        @Test
        @DisplayName("après quarts → 1 match de demi-finale")
        void unMatchDemi() {
            terminerTousLesMatchs(tournoi, PhaseTournoi.GROUPES);
            terminerTousLesMatchs(tournoi, PhaseTournoi.QUARTS);
            assertEquals(PhaseTournoi.DEMI_FINALES, tournoi.getPhaseActuelle());
            assertEquals(1, tournoi.getMatchsPhaseActuelle().size());
        }

        @Test
        @DisplayName("après demi → 1 match de finale")
        void unMatchFinale() {
            terminerTousLesMatchs(tournoi, PhaseTournoi.GROUPES);
            terminerTousLesMatchs(tournoi, PhaseTournoi.QUARTS);
            terminerTousLesMatchs(tournoi, PhaseTournoi.DEMI_FINALES);
            assertEquals(PhaseTournoi.FINALE, tournoi.getPhaseActuelle());
        }

        @Test
        @DisplayName("après finale → tournoi TERMINE avec un champion")
        void tournoiTermine() {
            terminerTousLesMatchs(tournoi, PhaseTournoi.GROUPES);
            terminerTousLesMatchs(tournoi, PhaseTournoi.QUARTS);
            terminerTousLesMatchs(tournoi, PhaseTournoi.DEMI_FINALES);
            terminerTousLesMatchs(tournoi, PhaseTournoi.FINALE);

            assertEquals(StatutTournoi.TERMINE, tournoi.getStatut());
            assertNotNull(tournoi.getChampionId());
        }
    }

    // -------------------------------------------------------------------------
    // Utilitaires
    // -------------------------------------------------------------------------

    private void inscrireTousLesJoueurs(Tournoi t) {
        IntStream.rangeClosed(1, 27)
            .forEach(i -> t.inscrire("joueur-id-" + i));
    }

    private void terminerTousLesMatchs(Tournoi t, PhaseTournoi phase) {
        t.getMatchs().stream()
            .filter(m -> m.getPhase() == phase && !m.estTermine())
            .forEach(m -> {
                if (m.estEnAttente()) {
                    t.demarrerMatch(m.getId(), "partie-" + m.getId());
                }
                String gagnant = m.getJoueursIds().get(0);
                t.terminerMatch(m.getId(), gagnant);
            });
    }
}
