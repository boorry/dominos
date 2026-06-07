package com.dominos.api.dto.utilisateur;

import com.dominos.domain.utilisateur.StatutUtilisateur;
import com.dominos.domain.utilisateur.Utilisateur;

/**
 * DTOs (Data Transfer Objects) liés aux utilisateurs.
 *
 * Regroupés dans un seul fichier pour la lisibilité.
 * Chaque record est immuable par nature (Java 17+).
 *
 * Pourquoi des records ?
 *   Un record est une classe immuable avec getters, equals, hashCode
 *   et toString générés automatiquement. Parfait pour des DTOs
 *   qui ne font que transporter des données.
 */
public final class UtilisateurDtos {

    private UtilisateurDtos() {}

    // -------------------------------------------------------------------------
    // Requêtes (ce que le client envoie)
    // -------------------------------------------------------------------------

    /**
     * POST /api/users
     *
     * Exemple JSON reçu :
     * {
     *   "pseudo": "alice_42",
     *   "email": "alice@example.com",
     *   "motDePasse": "monMotDePasse123"
     * }
     */
    public record InscriptionRequete(
        String pseudo,
        String email,
        String motDePasse
    ) {
        /** Validation basique — les règles métier restent dans le domaine. */
        public void valider() {
            if (pseudo == null || pseudo.isBlank())
                throw new IllegalArgumentException("Le pseudo est requis");
            if (email == null || email.isBlank())
                throw new IllegalArgumentException("L'email est requis");
            if (motDePasse == null || motDePasse.isBlank())
                throw new IllegalArgumentException("Le mot de passe est requis");
        }
    }

    /**
     * POST /api/auth/login
     *
     * Exemple JSON reçu :
     * {
     *   "pseudo": "alice_42",
     *   "motDePasse": "monMotDePasse123"
     * }
     */
    public record ConnexionRequete(
        String pseudo,
        String motDePasse
    ) {
        public void valider() {
            if (pseudo == null || pseudo.isBlank())
                throw new IllegalArgumentException("Le pseudo est requis");
            if (motDePasse == null || motDePasse.isBlank())
                throw new IllegalArgumentException("Le mot de passe est requis");
        }
    }

    // -------------------------------------------------------------------------
    // Réponses (ce que le serveur retourne)
    // -------------------------------------------------------------------------

    /**
     * Réponse après inscription ou consultation d'un profil.
     *
     * Exemple JSON retourné :
     * {
     *   "id": "uuid-alice",
     *   "pseudo": "alice_42",
     *   "email": "alice@example.com",
     *   "statut": "LIBRE"
     * }
     *
     * Note : motDePasseHache n'est JAMAIS exposé dans une réponse.
     */
    public record UtilisateurReponse(
        String id,
        String pseudo,
        String email,
        StatutUtilisateur statut
    ) {
        /** Convertit un objet domaine en DTO de réponse. */
        public static UtilisateurReponse depuis(Utilisateur u) {
            return new UtilisateurReponse(
                u.getId(),
                u.getPseudo(),
                u.getEmail(),
                u.getStatut()
            );
        }
    }

    /**
     * Réponse après connexion — inclut un token d'identification.
     *
     * À cette étape, on utilise l'id comme token simple.
     * À l'Étape 6, on remplacera par un vrai JWT.
     *
     * Exemple JSON retourné :
     * {
     *   "token": "uuid-alice",
     *   "utilisateur": { "id": "...", "pseudo": "alice_42", ... }
     * }
     */
    public record ConnexionReponse(
        String token,
        UtilisateurReponse utilisateur
    ) {
        public static ConnexionReponse depuis(Utilisateur u) {
            return new ConnexionReponse(
                u.getId(), // token simple = id pour l'instant
                UtilisateurReponse.depuis(u)
            );
        }
    }
}
