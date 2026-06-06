package com.dominos.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Plateau")
class PlateauTest {

    private Plateau plateau;

    @BeforeEach
    void setUp() {
        plateau = new Plateau();
    }

    @Test
    @DisplayName("vide à la création")
    void videALaCreation() {
        assertTrue(plateau.estVide());
        assertTrue(plateau.getExtrémiteGauche().isEmpty());
        assertTrue(plateau.getExtremiteDroite().isEmpty());
    }

    @Nested
    @DisplayName("poserPremier()")
    class PoserPremier {

        @Test
        @DisplayName("pose le premier domino sur un plateau vide")
        void poserSurVide() {
            plateau.poserPremier(new Domino(3, 5));
            assertFalse(plateau.estVide());
            assertEquals(3, plateau.getExtrémiteGauche().orElseThrow());
            assertEquals(5, plateau.getExtremiteDroite().orElseThrow());
        }

        @Test
        @DisplayName("lève IllegalStateException si plateau non vide")
        void leveExceptionSiNonVide() {
            plateau.poserPremier(new Domino(3, 5));
            assertThrows(IllegalStateException.class,
                () -> plateau.poserPremier(new Domino(1, 2)));
        }
    }

    @Nested
    @DisplayName("poser()")
    class Poser {

        @BeforeEach
        void init() {
            plateau.poserPremier(new Domino(3, 5));
        }

        @Test
        @DisplayName("pose à droite si gauche du domino == droite du plateau")
        void poserADroite() {
            Plateau.ResultatPose r = plateau.poser(new Domino(5, 2));
            assertEquals(Plateau.Cote.DROITE, r.cote());
            assertFalse(r.retourné());
            assertEquals(2, plateau.getExtremiteDroite().orElseThrow());
        }

        @Test
        @DisplayName("pose à gauche si droite du domino == gauche du plateau")
        void poserAGauche() {
            Plateau.ResultatPose r = plateau.poser(new Domino(1, 3));
            assertEquals(Plateau.Cote.GAUCHE, r.cote());
            assertEquals(1, plateau.getExtrémiteGauche().orElseThrow());
        }

        @Test
        @DisplayName("retourne le domino si nécessaire")
        void retournerSiBesoin() {
            Plateau.ResultatPose r = plateau.poser(new Domino(3, 1));
            assertTrue(r.retourné());
            assertEquals(1, plateau.getExtrémiteGauche().orElseThrow());
        }

        @Test
        @DisplayName("lève IllegalArgumentException si domino non jouable")
        void nonJouable() {
            assertThrows(IllegalArgumentException.class,
                () -> plateau.poser(new Domino(1, 2)));
        }
    }

    @Test
    @DisplayName("vider() réinitialise le plateau")
    void vider() {
        plateau.poserPremier(new Domino(3, 5));
        plateau.vider();
        assertTrue(plateau.estVide());
    }
}
