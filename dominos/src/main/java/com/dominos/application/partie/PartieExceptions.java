package com.dominos.application.partie;

/**
 * Exceptions métier liées aux parties.
 *
 * Mappées en HTTP par GlobalExceptionHandler à l'Étape 5 :
 *  - PartieIntrouvableException → 404 Not Found
 *  - PasTonTourException        → 403 Forbidden
 *  - PartieTermineeException    → 409 Conflict
 */
public final class PartieExceptions {

    private PartieExceptions() {}

    public static class PartieIntrouvableException extends RuntimeException {
        public PartieIntrouvableException(String partieId) {
            super("Aucune partie active trouvée avec l'id : " + partieId);
        }
    }

    public static class PasTonTourException extends RuntimeException {
        public PasTonTourException(String pseudoJoueurCourant) {
            super("Ce n'est pas votre tour — c'est au tour de " + pseudoJoueurCourant);
        }
    }

    public static class PartieTermineeException extends RuntimeException {
        public PartieTermineeException(String partieId) {
            super("La partie " + partieId + " est déjà terminée");
        }
    }
}
