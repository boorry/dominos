package com.dominos.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Domino")
class DominoTest {

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("crée un domino avec les valeurs données")
        void creerDomino() {
            Domino d = new Domino(3, 5);
            assertEquals(3, d.getGauche());
            assertEquals(5, d.getDroite());
        }

        @Test
        @DisplayName("accepte les valeurs limites 0 et 6")
        void valeurLimites() {
            assertDoesNotThrow(() -> new Domino(0, 6));
            assertDoesNotThrow(() -> new Domino(0, 0));
            assertDoesNotThrow(() -> new Domino(6, 6));
        }

        @ParameterizedTest(name = "gauche={0}, droite={1} → invalide")
        @CsvSource({ "-1,3", "7,3", "3,-1", "3,7" })
        @DisplayName("rejette les valeurs hors de [0..6]")
        void rejeterValeursInvalides(int gauche, int droite) {
            assertThrows(IllegalArgumentException.class, () -> new Domino(gauche, droite));
        }
    }

    @Nested
    @DisplayName("retourner()")
    class Retourner {

        @Test
        @DisplayName("retourne une nouvelle instance avec valeurs inversées")
        void retournerCreesNouvelleInstance() {
            Domino original = new Domino(3, 5);
            Domino retourne = original.retourner();
            assertEquals(5, retourne.getGauche());
            assertEquals(3, retourne.getDroite());
        }

        @Test
        @DisplayName("l'original n'est pas muté")
        void originalInchange() {
            Domino original = new Domino(3, 5);
            original.retourner();
            assertEquals(3, original.getGauche());
            assertEquals(5, original.getDroite());
        }
    }

    @Nested
    @DisplayName("estJouable()")
    class EstJouable {

        @Test
        @DisplayName("jouable si gauche correspond à droite de la table")
        void jouableGauche() {
            assertTrue(new Domino(5, 3).estJouable(0, 5));
        }

        @Test
        @DisplayName("jouable si droite correspond à gauche de la table")
        void jouableDroite() {
            assertTrue(new Domino(3, 5).estJouable(5, 0));
        }

        @Test
        @DisplayName("pas jouable si aucune face ne correspond")
        void nonJouable() {
            assertFalse(new Domino(1, 2).estJouable(4, 5));
        }
    }

    @Nested
    @DisplayName("estDouble()")
    class EstDouble {

        @Test
        @DisplayName("vrai pour [6|6]")
        void double6() {
            assertTrue(new Domino(6, 6).estDouble());
            assertTrue(new Domino(6, 6).estDouble(6));
        }

        @Test
        @DisplayName("faux pour [3|5]")
        void pasDouble() {
            assertFalse(new Domino(3, 5).estDouble());
        }
    }

    @Nested
    @DisplayName("equals / hashCode")
    class EqualsHashCode {

        @Test
        @DisplayName("[3|5] == [5|3] (symétrie)")
        void symetrique() {
            assertEquals(new Domino(3, 5), new Domino(5, 3));
        }

        @Test
        @DisplayName("[3|5] != [3|4]")
        void different() {
            assertNotEquals(new Domino(3, 5), new Domino(3, 4));
        }

        @Test
        @DisplayName("hashCode cohérent avec equals")
        void hashCodeCoherent() {
            assertEquals(new Domino(3, 5).hashCode(), new Domino(5, 3).hashCode());
        }
    }

    @Test
    @DisplayName("toString retourne [gauche|droite]")
    void toStringFormat() {
        assertEquals("[3|5]", new Domino(3, 5).toString());
    }
}
