package com.dominos.moteur;

import com.dominos.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MoteurJeu")
class MoteurJeuTest {

    private MoteurJeu moteur;
    private Partie    partie;
    private Joueur    alice, bob, carl;

    @BeforeEach
    void setUp() {
        moteur = new MoteurJeu();
        alice  = new Joueur("id-alice", "Alice");
        bob    = new Joueur("id-bob",   "Bob");
        carl   = new Joueur("id-carl",  "Carl");
        partie = new Partie(List.of(alice, bob, carl));
    }

    @Nested
    @DisplayName("initialiserManche()")
    class InitialiserManche {

        @Test
        @DisplayName("distribue 7 dominos à chaque joueur")
        void distribue7Dominos() {
            moteur.initialiserManche(partie);
            assertEquals(7, partie.getMain(alice).taille());
            assertEquals(7, partie.getMain(bob).taille());
            assertEquals(7, partie.getMain(carl).taille());
        }

        @Test
        @DisplayName("plateau vide après initialisation")
        void plateauVide() {
            moteur.initialiserManche(partie);
            assertTrue(partie.getPlateau().estVide());
        }

        @Test
        @DisplayName("21 dominos distribués sans doublons")
        void pasDedoublons() {
            moteur.initialiserManche(partie);
            List<Domino> tous = new java.util.ArrayList<>();
            for (Joueur j : partie.getJoueurs()) {
                tous.addAll(partie.getMain(j).getDominos());
            }
            assertEquals(21, tous.size());
            assertEquals(21, tous.stream().distinct().count());
        }
    }

    @Nested
    @DisplayName("jouerCoup()")
    class JouerCoup {

        @BeforeEach
        void init() {
            moteur.initialiserManche(partie);
        }

        @Test
        @DisplayName("poser un domino valide → aJoue=true")
        void poserDominoValide() {
            List<Domino> jouables = moteur.getDominosJouables(partie);
            assertFalse(jouables.isEmpty());
            ResultatCoup r = moteur.jouerCoup(partie, jouables.get(0));
            assertTrue(r.aJoue());
        }

        @Test
        @DisplayName("poser retire le domino de la main")
        void retirerDeMain() {
            Joueur joueur     = partie.getJoueurCourant();
            int tailleAvant   = partie.getMain(joueur).taille();
            moteur.jouerCoup(partie, moteur.getDominosJouables(partie).get(0));
            assertEquals(tailleAvant - 1, partie.getMain(joueur).taille());
        }

        @Test
        @DisplayName("scénario victoire — main vide")
        void victoire() {
            MainJoueur mainAlice = partie.getMain(alice);
            mainAlice.vider();
            mainAlice.ajouter(new Domino(3, 5));
            partie.setJoueurCourantIndex(0);

            ResultatCoup r = moteur.jouerCoup(partie, new Domino(3, 5));
            assertTrue(r.estTermine() || r.etat() == EtatPartie.VICTOIRE);
            assertEquals(alice, r.gagnant());
        }
    }

    @Nested
    @DisplayName("passerTour()")
    class PasserTour {

        @BeforeEach
        void init() {
            moteur.initialiserManche(partie);
        }

        @Test
        @DisplayName("une passe → EN_COURS")
        void unePasse() {
            ResultatCoup r = moteur.passerTour(partie);
            assertEquals(EtatPartie.EN_COURS, r.etat());
        }

        @Test
        @DisplayName("3 passes consécutives → BLOCAGE ou TERMINEE")
        void troisPasses() {
            moteur.passerTour(partie);
            moteur.passerTour(partie);
            ResultatCoup r = moteur.passerTour(partie);
            assertTrue(r.etat() == EtatPartie.BLOCAGE
                    || r.etat() == EtatPartie.TERMINEE);
        }
    }
}
