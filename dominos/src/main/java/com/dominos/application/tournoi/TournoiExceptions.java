package com.dominos.application.tournoi;

/**
 * Exceptions métier des tournois.
 *
 * Mappées en HTTP :
 *  - TournoiIntrouvableException  → 404
 *  - TournoiCompletException      → 409
 *  - JoueurDejaInscritException   → 409
 *  - TournoiNonDemarrable         → 400
 */
public final class TournoiExceptions {

    private TournoiExceptions() {}

    public static class TournoiIntrouvableException extends RuntimeException {
        public TournoiIntrouvableException(String id) {
            super("Tournoi introuvable : " + id);
        }
    }

    public static class TournoiCompletException extends RuntimeException {
        public TournoiCompletException() {
            super("Le tournoi est complet (27 joueurs)");
        }
    }

    public static class JoueurDejaInscritException extends RuntimeException {
        public JoueurDejaInscritException(String pseudo) {
            super("Le joueur '" + pseudo + "' est déjà inscrit à ce tournoi");
        }
    }

    public static class TournoiNonDemarrableException extends RuntimeException {
        public TournoiNonDemarrableException(String raison) {
            super("Impossible de démarrer le tournoi : " + raison);
        }
    }

    public static class PhaseNonTermineeException extends RuntimeException {
        public PhaseNonTermineeException(String phase) {
            super("La phase " + phase + " n'est pas encore terminée");
        }
    }
}
