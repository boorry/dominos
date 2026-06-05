package com.dominos.application.matchmaking;

/**
 * Exceptions métier du matchmaking.
 *
 * Mappées en HTTP à l'Étape 5 :
 *  - JoueurDejaEnAttenteException  → 409 Conflict
 *  - JoueurDejaEnPartieException   → 409 Conflict
 *  - JoueurNonEnAttenteException   → 400 Bad Request
 */
public final class MatchmakingExceptions {

    private MatchmakingExceptions() {}

    public static class JoueurDejaEnAttenteException extends RuntimeException {
        public JoueurDejaEnAttenteException(String pseudo) {
            super("Le joueur '" + pseudo + "' est déjà dans la file d'attente");
        }
    }

    public static class JoueurDejaEnPartieException extends RuntimeException {
        public JoueurDejaEnPartieException(String pseudo) {
            super("Le joueur '" + pseudo + "' est déjà en partie");
        }
    }

    public static class JoueurNonEnAttenteException extends RuntimeException {
        public JoueurNonEnAttenteException(String pseudo) {
            super("Le joueur '" + pseudo + "' n'est pas dans la file d'attente");
        }
    }
}
