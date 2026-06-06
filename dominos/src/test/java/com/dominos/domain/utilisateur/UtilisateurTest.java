package com.dominos.domain.utilisateur;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Utilisateur")
class UtilisateurTest {

    private static final String PSEUDO = "alice_42";
    private static final String EMAIL  = "alice@example.com";
    private static final String HASH   = "sel$hashvalide123";

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("statut LIBRE par défaut")
        void statutLibreParDefaut() {
            Utilisateur u = new Utilisateur(PSEUDO, EMAIL, HASH);
            assertEquals(StatutUtilisateur.LIBRE, u.getStatut());
        }

        @Test
        @DisplayName("id UUID généré automatiquement")
        void idGenere() {
            Utilisateur u = new Utilisateur(PSEUDO, EMAIL, HASH);
            assertNotNull(u.getId());
            assertFalse(u.getId().isBlank());
        }

        @Test
        @DisplayName("email normalisé en minuscules")
        void emailNormalise() {
            Utilisateur u = new Utilisateur(PSEUDO, "Alice@Example.COM", HASH);
            assertEquals("alice@example.com", u.getEmail());
        }
    }

    @Nested
    @DisplayName("Validation pseudo")
    class ValidationPseudo {

        @Test
        @DisplayName("pseudo vide → exception")
        void pseudoVide() {
            assertThrows(IllegalArgumentException.class,
                () -> new Utilisateur("", EMAIL, HASH));
        }

        @Test
        @DisplayName("pseudo trop court → exception")
        void pseudoTropCourt() {
            assertThrows(IllegalArgumentException.class,
                () -> new Utilisateur("ab", EMAIL, HASH));
        }

        @Test
        @DisplayName("pseudo trop long → exception")
        void pseudoTropLong() {
            assertThrows(IllegalArgumentException.class,
                () -> new Utilisateur("a".repeat(21), EMAIL, HASH));
        }

        @ParameterizedTest(name = "pseudo invalide : ''{0}''")
        @ValueSource(strings = { "alice!", "alice 42", "alice-42" })
        @DisplayName("caractères spéciaux non autorisés → exception")
        void pseudoCaracteresInvalides(String pseudo) {
            assertThrows(IllegalArgumentException.class,
                () -> new Utilisateur(pseudo, EMAIL, HASH));
        }
    }

    @Nested
    @DisplayName("Transitions de statut")
    class TransitionsStatut {

        @Test
        @DisplayName("LIBRE → EN_ATTENTE : valide")
        void libreVersEnAttente() {
            Utilisateur u = new Utilisateur(PSEUDO, EMAIL, HASH);
            u.changerStatut(StatutUtilisateur.EN_ATTENTE);
            assertEquals(StatutUtilisateur.EN_ATTENTE, u.getStatut());
        }

        @Test
        @DisplayName("EN_ATTENTE → EN_PARTIE : valide")
        void enAttenteVersEnPartie() {
            Utilisateur u = new Utilisateur(PSEUDO, EMAIL, HASH);
            u.changerStatut(StatutUtilisateur.EN_ATTENTE);
            u.changerStatut(StatutUtilisateur.EN_PARTIE);
            assertEquals(StatutUtilisateur.EN_PARTIE, u.getStatut());
        }

        @Test
        @DisplayName("EN_PARTIE → LIBRE : valide")
        void enPartieVersLibre() {
            Utilisateur u = new Utilisateur(PSEUDO, EMAIL, HASH);
            u.changerStatut(StatutUtilisateur.EN_ATTENTE);
            u.changerStatut(StatutUtilisateur.EN_PARTIE);
            u.changerStatut(StatutUtilisateur.LIBRE);
            assertEquals(StatutUtilisateur.LIBRE, u.getStatut());
        }

        @Test
        @DisplayName("LIBRE → EN_PARTIE : invalide")
        void libreVersEnPartieInvalide() {
            Utilisateur u = new Utilisateur(PSEUDO, EMAIL, HASH);
            assertThrows(IllegalStateException.class,
                () -> u.changerStatut(StatutUtilisateur.EN_PARTIE));
        }
    }
}
