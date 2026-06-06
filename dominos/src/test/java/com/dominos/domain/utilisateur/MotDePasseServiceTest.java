package com.dominos.domain.utilisateur;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MotDePasseService")
class MotDePasseServiceTest {

    private MotDePasseService service;

    @BeforeEach
    void setUp() {
        service = new MotDePasseService();
    }

    @Nested
    @DisplayName("hacher()")
    class Hacher {

        @Test
        @DisplayName("le hash ne contient pas le mot de passe en clair")
        void hashNePasContenirMotDePasse() {
            String mdp  = "monMotDePasse123";
            String hash = service.hacher(mdp);
            assertFalse(hash.contains(mdp));
        }

        @Test
        @DisplayName("deux hachages du même mot de passe sont différents (sel aléatoire)")
        void selAleatoire() {
            String hash1 = service.hacher("motdepasse123");
            String hash2 = service.hacher("motdepasse123");
            assertNotEquals(hash1, hash2);
        }

        @Test
        @DisplayName("mot de passe trop court → exception")
        void motDePasseTropCourt() {
            assertThrows(IllegalArgumentException.class,
                () -> service.hacher("court"));
        }
    }

    @Nested
    @DisplayName("verifier()")
    class Verifier {

        @Test
        @DisplayName("bon mot de passe → vrai")
        void bonMotDePasse() {
            String mdp  = "monMotDePasse123";
            String hash = service.hacher(mdp);
            assertTrue(service.verifier(mdp, hash));
        }

        @Test
        @DisplayName("mauvais mot de passe → faux")
        void mauvaisMotDePasse() {
            String hash = service.hacher("monMotDePasse123");
            assertFalse(service.verifier("mauvaisMotDePasse", hash));
        }

        @Test
        @DisplayName("null → faux sans exception")
        void motDePasseNull() {
            String hash = service.hacher("monMotDePasse123");
            assertFalse(service.verifier(null, hash));
        }
    }
}
