package com.dominos.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MainJoueur")
class MainJoueurTest {

    private MainJoueur main;

    @BeforeEach
    void setUp() {
        main = new MainJoueur();
    }

    @Nested
    @DisplayName("Gestion des dominos")
    class GestionDominos {

        @Test
        @DisplayName("vide à la création")
        void videALaCreation() {
            assertTrue(main.estVide());
            assertEquals(0, main.taille());
        }

        @Test
        @DisplayName("ajouter un domino l'inclut dans la main")
        void ajouterDomino() {
            main.ajouter(new Domino(3, 5));
            assertFalse(main.estVide());
            assertEquals(1, main.taille());
        }

        @Test
        @DisplayName("retirer un domino présent le supprime")
        void retirerDominoPresent() {
            Domino d = new Domino(3, 5);
            main.ajouter(d);
            main.retirer(d);
            assertTrue(main.estVide());
        }

        @Test
        @DisplayName("retirer un domino absent lève IllegalStateException")
        void retirerDominoAbsent() {
            assertThrows(IllegalStateException.class,
                () -> main.retirer(new Domino(3, 5)));
        }

        @Test
        @DisplayName("getDominos() retourne une vue non modifiable")
        void vueNonModifiable() {
            main.ajouter(new Domino(1, 2));
            List<Domino> vue = main.getDominos();
            assertThrows(UnsupportedOperationException.class,
                () -> vue.add(new Domino(3, 4)));
        }

        @Test
        @DisplayName("vider() supprime tous les dominos")
        void vider() {
            main.ajouter(new Domino(1, 2));
            main.ajouter(new Domino(3, 4));
            main.vider();
            assertTrue(main.estVide());
        }
    }

    @Nested
    @DisplayName("getDominosJouables()")
    class DominosJouables {

        @Test
        @DisplayName("plateau vide → tous les dominos jouables")
        void plateauVide() {
            main.ajouter(new Domino(3, 5));
            main.ajouter(new Domino(1, 2));
            assertEquals(2, main.getDominosJouables(-1, -1).size());
        }

        @Test
        @DisplayName("filtre les dominos compatibles")
        void filtreCompatibles() {
            main.ajouter(new Domino(5, 3));
            main.ajouter(new Domino(1, 2));
            main.ajouter(new Domino(6, 4));
            List<Domino> jouables = main.getDominosJouables(4, 5);
            assertEquals(2, jouables.size());
        }

        @Test
        @DisplayName("sans effet de bord — appels répétés donnent le même résultat")
        void sansEffetDeBord() {
            main.ajouter(new Domino(5, 3));
            main.ajouter(new Domino(1, 2));
            List<Domino> appel1 = main.getDominosJouables(4, 5);
            List<Domino> appel2 = main.getDominosJouables(4, 5);
            assertEquals(appel1.size(), appel2.size());
        }
    }

    @Nested
    @DisplayName("calculerScore()")
    class CalculerScore {

        @Test
        @DisplayName("score = somme des valeurs totales")
        void scoreCorrect() {
            main.ajouter(new Domino(3, 5)); // 8
            main.ajouter(new Domino(1, 2)); // 3
            assertEquals(11, main.calculerScore());
        }

        @Test
        @DisplayName("score = 0 si main vide")
        void scoreMainVide() {
            assertEquals(0, main.calculerScore());
        }
    }
}
