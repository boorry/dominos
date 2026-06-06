package com.dominos.application.matchmaking;

import com.dominos.application.utilisateur.UtilisateurExceptions;
import com.dominos.application.utilisateur.UtilisateurService;
import com.dominos.domain.utilisateur.MotDePasseService;
import com.dominos.domain.utilisateur.StatutUtilisateur;
import com.dominos.domain.utilisateur.Utilisateur;
import com.dominos.infrastructure.matchmaking.MatchmakingRepositoryMemoire;
import com.dominos.infrastructure.utilisateur.UtilisateurRepositoryMemoire;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MatchmakingService")
class MatchmakingServiceTest {

    private MatchmakingService           matchmaking;
    private UtilisateurService           utilisateurService;
    private UtilisateurRepositoryMemoire utilisateurRepo;
    private String idAlice, idBob, idCarl, idDave;

    @BeforeEach
    void setUp() {
        utilisateurRepo    = new UtilisateurRepositoryMemoire();
        utilisateurService = new UtilisateurService(utilisateurRepo, new MotDePasseService());
        matchmaking        = new MatchmakingService(utilisateurRepo, new MatchmakingRepositoryMemoire());

        idAlice = utilisateurService.inscrire("alice_42", "alice@example.com", "motdepasse1").getId();
        idBob   = utilisateurService.inscrire("bob_99",   "bob@example.com",   "motdepasse2").getId();
        idCarl  = utilisateurService.inscrire("carl_7",   "carl@example.com",  "motdepasse3").getId();
        idDave  = utilisateurService.inscrire("dave_5",   "dave@example.com",  "motdepasse4").getId();
    }

    @Nested
    @DisplayName("rejoindreFile()")
    class RejoindreFile {

        @Test
        @DisplayName("1 joueur → EN_ATTENTE 1/3")
        void unJoueur() {
            ResultatMatchmaking r = matchmaking.rejoindreFile(idAlice);
            assertTrue(r.estEnAttente());
            assertEquals(1, r.getJoueursEnAttente());
        }

        @Test
        @DisplayName("le statut passe à EN_ATTENTE")
        void statutMisAJour() {
            matchmaking.rejoindreFile(idAlice);
            assertEquals(StatutUtilisateur.EN_ATTENTE,
                utilisateurRepo.trouverParId(idAlice).orElseThrow().getStatut());
        }

        @Test
        @DisplayName("3 joueurs → PARTIE_TROUVEE")
        void troisJoueursPartieTrouvee() {
            matchmaking.rejoindreFile(idAlice);
            matchmaking.rejoindreFile(idBob);
            ResultatMatchmaking r = matchmaking.rejoindreFile(idCarl);
            assertTrue(r.estPartieTrouvee());
            assertTrue(r.getPartie().isPresent());
            assertEquals(3, r.getPartie().get().getJoueurs().size());
        }

        @Test
        @DisplayName("les 3 joueurs passent EN_PARTIE après matchmaking")
        void statutEnPartieApresMatchmaking() {
            matchmaking.rejoindreFile(idAlice);
            matchmaking.rejoindreFile(idBob);
            matchmaking.rejoindreFile(idCarl);

            assertEquals(StatutUtilisateur.EN_PARTIE,
                utilisateurRepo.trouverParId(idAlice).orElseThrow().getStatut());
            assertEquals(StatutUtilisateur.EN_PARTIE,
                utilisateurRepo.trouverParId(idBob).orElseThrow().getStatut());
            assertEquals(StatutUtilisateur.EN_PARTIE,
                utilisateurRepo.trouverParId(idCarl).orElseThrow().getStatut());
        }

        @Test
        @DisplayName("joueur déjà EN_ATTENTE → exception")
        void dejaEnAttente() {
            matchmaking.rejoindreFile(idAlice);
            assertThrows(MatchmakingExceptions.JoueurDejaEnAttenteException.class,
                () -> matchmaking.rejoindreFile(idAlice));
        }

        @Test
        @DisplayName("joueur EN_PARTIE → exception")
        void dejaEnPartie() {
            Utilisateur alice = utilisateurRepo.trouverParId(idAlice).orElseThrow();
            alice.changerStatut(StatutUtilisateur.EN_ATTENTE);
            alice.changerStatut(StatutUtilisateur.EN_PARTIE);
            utilisateurRepo.sauvegarder(alice);

            assertThrows(MatchmakingExceptions.JoueurDejaEnPartieException.class,
                () -> matchmaking.rejoindreFile(idAlice));
        }

        @Test
        @DisplayName("id inconnu → UtilisateurIntrouvableException")
        void idInconnu() {
            assertThrows(UtilisateurExceptions.UtilisateurIntrouvableException.class,
                () -> matchmaking.rejoindreFile("id-inexistant"));
        }
    }

    @Nested
    @DisplayName("quitterFile()")
    class QuitterFile {

        @Test
        @DisplayName("quitter remet le statut à LIBRE")
        void quitterRemetLibre() {
            matchmaking.rejoindreFile(idAlice);
            matchmaking.quitterFile(idAlice);
            assertEquals(StatutUtilisateur.LIBRE,
                utilisateurRepo.trouverParId(idAlice).orElseThrow().getStatut());
        }

        @Test
        @DisplayName("quitter sans être en attente → exception")
        void quitterSansEtreEnAttente() {
            assertThrows(MatchmakingExceptions.JoueurNonEnAttenteException.class,
                () -> matchmaking.quitterFile(idAlice));
        }
    }
}
