package com.dominos.application.utilisateur;

/**
 * Exceptions métier liées aux utilisateurs.
 *
 * On utilise des exceptions spécifiques plutôt que des exceptions génériques
 * pour que la couche API (Étape 5) puisse retourner les bons codes HTTP :
 *  - PseudoDejaPrisException     → 409 Conflict
 *  - EmailDejaPrisException      → 409 Conflict
 *  - AuthentificationException   → 401 Unauthorized
 *  - UtilisateurIntrouvable      → 404 Not Found
 */
public final class UtilisateurExceptions {

    private UtilisateurExceptions() {}

    public static class PseudoDejaPrisException extends RuntimeException {
        public PseudoDejaPrisException(String pseudo) {
            super("Le pseudo '" + pseudo + "' est déjà utilisé");
        }
    }

    public static class EmailDejaPrisException extends RuntimeException {
        public EmailDejaPrisException(String email) {
            super("L'email '" + email + "' est déjà utilisé");
        }
    }

    public static class AuthentificationException extends RuntimeException {
        public AuthentificationException() {
            super("Pseudo ou mot de passe incorrect");
            // Message volontairement vague pour ne pas révéler
            // si c'est le pseudo ou le mot de passe qui est faux
        }
    }

    public static class UtilisateurIntrouvableException extends RuntimeException {
        public UtilisateurIntrouvableException(String id) {
            super("Aucun utilisateur trouvé avec l'id : " + id);
        }
    }
}
