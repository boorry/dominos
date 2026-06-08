package com.dominos.api.websocket;

import com.dominos.application.partie.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Tests du NotificationService.
 *
 * @ExtendWith(MockitoExtension.class) : active Mockito pour les tests.
 * @Mock : crée un mock de SimpMessagingTemplate sans démarrer Spring.
 *
 * On vérifie que :
 *  - le bon topic est utilisé
 *  - le bon type d'événement est envoyé
 *  - les données de l'événement sont correctes
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService")
class NotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private NotificationService service;

    private static final String PARTIE_ID = "uuid-partie";

    @BeforeEach
    void setUp() {
        service = new NotificationService(messagingTemplate);
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("notifierPartieCreee envoie sur le bon topic")
    void partieCreee_bonTopic() {
        service.notifierPartieCreee(PARTIE_ID, List.of("alice_42", "bob_99", "carl_7"));

        verify(messagingTemplate).convertAndSend(
            eq("/topic/parties/" + PARTIE_ID),
            any(EvenementPartie.class)
        );
    }

    @Test
    @DisplayName("notifierPartieCreee — type PARTIE_CREEE et joueurs corrects")
    void partieCreee_contenu() {
        ArgumentCaptor<EvenementPartie> captor =
            ArgumentCaptor.forClass(EvenementPartie.class);

        service.notifierPartieCreee(PARTIE_ID, List.of("alice_42", "bob_99", "carl_7"));

        verify(messagingTemplate).convertAndSend(
            eq("/topic/parties/" + PARTIE_ID), captor.capture());

        EvenementPartie e = captor.getValue();
        assertEquals(TypeEvenement.PARTIE_CREEE, e.getType());
        assertEquals(PARTIE_ID, e.getPartieId());
        assertEquals(3, e.getJoueurs().size());
        assertTrue(e.getJoueurs().contains("alice_42"));
    }

    @Test
    @DisplayName("notifierCoupJoue — type COUP_JOUE avec plateau")
    void coupJoue_contenu() {
        ArgumentCaptor<EvenementPartie> captor =
            ArgumentCaptor.forClass(EvenementPartie.class);

        service.notifierCoupJoue(
            PARTIE_ID,
            "alice_42",
            "alice_42 pose [5|5] comme premier domino.",
            List.of("[5|5]"),
            "bob_99",
            Map.of("alice_42", 0, "bob_99", 0, "carl_7", 0),
            1
        );

        verify(messagingTemplate).convertAndSend(
            eq("/topic/parties/" + PARTIE_ID), captor.capture());

        EvenementPartie e = captor.getValue();
        assertEquals(TypeEvenement.COUP_JOUE,  e.getType());
        assertEquals("alice_42",               e.getJoueurActif());
        assertEquals("bob_99",                 e.getJoueurSuivant());
        assertEquals(1,                        e.getPlateau().size());
        assertEquals("[5|5]",                  e.getPlateau().get(0));
    }

    @Test
    @DisplayName("notifierJoueurPasse — type JOUEUR_PASSE")
    void joueurPasse_contenu() {
        ArgumentCaptor<EvenementPartie> captor =
            ArgumentCaptor.forClass(EvenementPartie.class);

        service.notifierJoueurPasse(PARTIE_ID, "bob_99", "carl_7", 1);

        verify(messagingTemplate).convertAndSend(
            eq("/topic/parties/" + PARTIE_ID), captor.capture());

        EvenementPartie e = captor.getValue();
        assertEquals(TypeEvenement.JOUEUR_PASSE, e.getType());
        assertEquals("bob_99",                   e.getJoueurActif());
        assertEquals("carl_7",                   e.getJoueurSuivant());
        assertTrue(e.getMessage().contains("bob_99"));
    }

    @Test
    @DisplayName("notifierMancheTerminee — type MANCHE_TERMINEE avec gagnant")
    void mancheTerminee_contenu() {
        ArgumentCaptor<EvenementPartie> captor =
            ArgumentCaptor.forClass(EvenementPartie.class);

        service.notifierMancheTerminee(
            PARTIE_ID, "alice_42",
            Map.of("alice_42", 45, "bob_99", 0, "carl_7", 0),
            1, "alice_42 gagne la manche 1 !"
        );

        verify(messagingTemplate).convertAndSend(
            eq("/topic/parties/" + PARTIE_ID), captor.capture());

        EvenementPartie e = captor.getValue();
        assertEquals(TypeEvenement.MANCHE_TERMINEE, e.getType());
        assertEquals("alice_42",                    e.getGagnant());
        assertEquals(45, e.getScores().get("alice_42"));
    }

    @Test
    @DisplayName("notifierPartieTerminee — type PARTIE_TERMINEE")
    void partieTerminee_contenu() {
        ArgumentCaptor<EvenementPartie> captor =
            ArgumentCaptor.forClass(EvenementPartie.class);

        service.notifierPartieTerminee(
            PARTIE_ID, "alice_42",
            Map.of("alice_42", 120, "bob_99", 45, "carl_7", 30),
            "alice_42 remporte la partie avec 120 points !"
        );

        verify(messagingTemplate).convertAndSend(
            eq("/topic/parties/" + PARTIE_ID), captor.capture());

        EvenementPartie e = captor.getValue();
        assertEquals(TypeEvenement.PARTIE_TERMINEE, e.getType());
        assertEquals("alice_42",                    e.getGagnant());
        assertEquals(120, e.getScores().get("alice_42"));
    }

    @Test
    @DisplayName("notifierErreur — type ERREUR avec message")
    void erreur_contenu() {
        ArgumentCaptor<EvenementPartie> captor =
            ArgumentCaptor.forClass(EvenementPartie.class);

        service.notifierErreur(PARTIE_ID, "Domino non jouable");

        verify(messagingTemplate).convertAndSend(
            eq("/topic/parties/" + PARTIE_ID), captor.capture());

        EvenementPartie e = captor.getValue();
        assertEquals(TypeEvenement.ERREUR,  e.getType());
        assertEquals("Domino non jouable",  e.getMessage());
    }
}
