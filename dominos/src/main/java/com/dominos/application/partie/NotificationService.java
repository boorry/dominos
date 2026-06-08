package com.dominos.application.partie;

import com.dominos.api.websocket.EvenementPartie;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service de notification WebSocket.
 *
 * Responsabilité unique : envoyer des événements aux clients
 * connectés sur le topic d'une partie.
 *
 * SimpMessagingTemplate est fourni par Spring WebSocket.
 * Il permet d'envoyer un message à tous les abonnés d'un topic.
 *
 * Topic d'une partie : /topic/parties/{partieId}
 * Tous les joueurs d'une partie s'abonnent à ce topic
 * et reçoivent instantanément chaque événement.
 */
@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // -------------------------------------------------------------------------
    // Méthode principale
    // -------------------------------------------------------------------------

    /**
     * Envoie un événement à tous les joueurs d'une partie.
     *
     * Le topic est : /topic/parties/{partieId}
     * Tous les clients abonnés à ce topic reçoivent le message.
     */
    public void notifier(String partieId, EvenementPartie evenement) {
        String topic = "/topic/parties/" + partieId;
        messagingTemplate.convertAndSend(topic, evenement);
    }

    // -------------------------------------------------------------------------
    // Raccourcis par type d'événement
    // -------------------------------------------------------------------------

    public void notifierPartieCreee(String partieId,
                                     java.util.List<String> joueurs) {
        notifier(partieId, EvenementPartie.partieCreee(partieId, joueurs));
    }

    public void notifierCoupJoue(String partieId,
                                  String joueurActif,
                                  String message,
                                  java.util.List<String> plateau,
                                  String joueurSuivant,
                                  java.util.Map<String, Integer> scores,
                                  int manche) {
        notifier(partieId, EvenementPartie.coupJoue(
            partieId, joueurActif, message, plateau, joueurSuivant, scores, manche));
    }

    public void notifierJoueurPasse(String partieId,
                                     String joueurActif,
                                     String joueurSuivant,
                                     int manche) {
        notifier(partieId, EvenementPartie.joueurPasse(
            partieId, joueurActif, joueurSuivant, manche));
    }

    public void notifierMancheTerminee(String partieId,
                                        String gagnant,
                                        java.util.Map<String, Integer> scores,
                                        int manche,
                                        String message) {
        notifier(partieId, EvenementPartie.mancheTerminee(
            partieId, gagnant, scores, manche, message));
    }

    public void notifierPartieTerminee(String partieId,
                                        String gagnant,
                                        java.util.Map<String, Integer> scores,
                                        String message) {
        notifier(partieId, EvenementPartie.partieTerminee(
            partieId, gagnant, scores, message));
    }

    public void notifierErreur(String partieId, String message) {
        notifier(partieId, EvenementPartie.erreur(partieId, message));
    }
}
