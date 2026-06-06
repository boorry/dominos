package com.dominos.application.utilisateur;

import com.dominos.domain.utilisateur.MotDePasseService;
import com.dominos.domain.utilisateur.StatutUtilisateur;
import com.dominos.domain.utilisateur.Utilisateur;
import com.dominos.infrastructure.utilisateur.UtilisateurRepositoryMemoire;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UtilisateurService")
class UtilisateurServiceTest {

    private UtilisateurService           service;
    private UtilisateurRepositoryMemoire repository;

    @BeforeEach
    void setUp() {
        repository = new UtilisateurRepositoryMemoire();
        service    = new UtilisateurService(repository, new MotDePasseService());
    }

    @Nested
    @DisplayName("inscrire()")
    class Inscrire {

        @Test
        @DisplayName("inscrit un nouvel utilisateur")
        void inscrireNouvelUtilisateur() {
            Utilisateur u = service.inscrire("alice_42", "alice@example.com", "motdepasse123");
            assertNotNull(u.getId());
            assertEquals("alice_42", u.getPseudo());
            assertEquals(StatutUtilisateur.LIBRE, u.getStatut());
            assertEquals(1, repository.compter());
        }

        @Test
        @DisplayName("mot de passe non stocké en clair")
        void motDePasseHache() {
            Utilisateur u = service.inscrire("alice_42", "alice@example.com", "motdepasse123");
            assertNotEquals("motdepasse123", u.getMotDePasseHache());
        }

        @Test
        @DisplayName("pseudo déjà pris → PseudoDejaPrisException")
        void pseudoDejaPris() {
            service.inscrire("alice_42", "alice@example.com", "motdepasse123");
            assertThrows(UtilisateurExceptions.PseudoDejaPrisException.class,
                () -> service.inscrire("alice_42", "autre@example.com", "motdepasse456"));
        }

        @Test
        @DisplayName("email déjà pris → EmailDejaPrisException")
        void emailDejaPris() {
            service.inscrire("alice_42", "alice@example.com", "motdepasse123");
            assertThrows(UtilisateurExceptions.EmailDejaPrisException.class,
                () -> service.inscrire("bob_99", "alice@example.com", "motdepasse456"));
        }
    }

    @Nested
    @DisplayName("authentifier()")
    class Authentifier {

        @BeforeEach
        void inscrireAlice() {
            service.inscrire("alice_42", "alice@example.com", "motdepasse123");
        }

        @Test
        @DisplayName("bons identifiants → retourne l'utilisateur")
        void bonsIdentifiants() {
            Utilisateur u = service.authentifier("alice_42", "motdepasse123");
            assertEquals("alice_42", u.getPseudo());
        }

        @Test
        @DisplayName("mauvais mot de passe → AuthentificationException")
        void mauvaisMotDePasse() {
            assertThrows(UtilisateurExceptions.AuthentificationException.class,
                () -> service.authentifier("alice_42", "mauvaismdp"));
        }

        @Test
        @DisplayName("pseudo inconnu → AuthentificationException")
        void pseudoInconnu() {
            assertThrows(UtilisateurExceptions.AuthentificationException.class,
                () -> service.authentifier("inconnu", "motdepasse123"));
        }
    }

    @Nested
    @DisplayName("changerStatut()")
    class ChangerStatut {

        private String idAlice;

        @BeforeEach
        void inscrireAlice() {
            idAlice = service.inscrire("alice_42", "alice@example.com", "motdepasse123").getId();
        }

        @Test
        @DisplayName("LIBRE → EN_ATTENTE : valide")
        void libreVersEnAttente() {
            Utilisateur u = service.changerStatut(idAlice, StatutUtilisateur.EN_ATTENTE);
            assertEquals(StatutUtilisateur.EN_ATTENTE, u.getStatut());
        }

        @Test
        @DisplayName("changement persisté")
        void changementPersiste() {
            service.changerStatut(idAlice, StatutUtilisateur.EN_ATTENTE);
            assertEquals(StatutUtilisateur.EN_ATTENTE,
                service.consulter(idAlice).getStatut());
        }

        @Test
        @DisplayName("id inconnu → UtilisateurIntrouvableException")
        void idInconnu() {
            assertThrows(UtilisateurExceptions.UtilisateurIntrouvableException.class,
                () -> service.changerStatut("id-inexistant", StatutUtilisateur.EN_ATTENTE));
        }
    }
}
