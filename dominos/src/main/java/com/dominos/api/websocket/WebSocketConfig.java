package com.dominos.api.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration WebSocket avec STOMP.
 *
 * @EnableWebSocketMessageBroker : active le support WebSocket avec
 * un broker de messages (système de routage des messages).
 *
 * Deux concepts clés :
 *
 * 1. ENDPOINT de connexion :
 *    L'URL à laquelle les clients se connectent pour établir
 *    la connexion WebSocket. Ex : ws://localhost:8080/ws
 *    withSockJS() : active SockJS comme fallback si WebSocket
 *    n'est pas disponible (navigateurs anciens).
 *
 * 2. TOPICS (broker de messages) :
 *    /topic  → messages du serveur vers les clients (broadcast)
 *              Ex: /topic/parties/uuid → tous les joueurs de la partie
 *    /app    → messages des clients vers le serveur
 *              Ex: /app/parties/uuid/jouer → jouer un domino via WS
 *    /queue  → messages privés vers un seul client (non utilisé ici)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configure le broker de messages.
     *
     * enableSimpleBroker("/topic") : active un broker en mémoire
     * pour diffuser les messages sur les topics /topic/...
     *
     * setApplicationDestinationPrefixes("/app") : les messages
     * envoyés par les clients vers /app/... sont routés vers
     * les méthodes @MessageMapping de nos controllers.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Enregistre l'endpoint WebSocket.
     *
     * Les clients se connectent à : ws://localhost:8080/ws
     * setAllowedOriginPatterns("*") : autorise toutes les origines
     * (à restreindre en production).
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
