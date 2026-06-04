package com.dominos.domain.utilisateur;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entité représentant un compte utilisateur de la plateforme.
 *
 * Règles métier encodées ici :
 *  - pseudo   : obligatoire, 3 à 20 caractères, alphanumérique + underscore
 *  - email    : obligatoire, format valide
 *  - motDePasseHache : jamais le mot de passe en clair (responsabilité du service)
 *  - statut   : LIBRE par défaut à la création
 *
 * Pourquoi séparer Utilisateur de Joueur (Étape 1) ?
 *  Joueur est un participant dans une PARTIE (état temporaire).
 *  Utilisateur est un COMPTE sur la plateforme (état persistant).
 *  Un Utilisateur peut jouer plusieurs parties — à l'Étape 4, on liera
 *  les deux via un identifiant commun.
 */
public class Utilisateur {

    private final String        id;
    private final String        pseudo;
    private final String        email;
    private       String        motDePasseHache;
    private       StatutUtilisateur statut;
    private final LocalDateTime dateInscription;

    /**
     * Constructeur principal — utilisé à l'inscription.
     * L'id et la date sont générés automatiquement.
     */
    public Utilisateur(String pseudo, String email, String motDePasseHache) {
        this(UUID.randomUUID().toString(), pseudo, email, motDePasseHache,
             StatutUtilisateur.LIBRE, LocalDateTime.now());
    }

    /**
     * Constructeur complet — utilisé pour reconstruire depuis la persistance.
     */
    public Utilisateur(String id, String pseudo, String email,
                       String motDePasseHache, StatutUtilisateur statut,
                       LocalDateTime dateInscription) {
        this.id               = validerNonVide(id,              "id");
        this.pseudo           = validerPseudo(pseudo);
        this.email            = validerEmail(email);
        this.motDePasseHache  = validerNonVide(motDePasseHache, "motDePasseHache");
        this.statut           = Objects.requireNonNull(statut,  "statut requis");
        this.dateInscription  = Objects.requireNonNull(dateInscription, "dateInscription requise");
    }

    // -------------------------------------------------------------------------
    // Accesseurs
    // -------------------------------------------------------------------------

    public String getId()                     { return id; }
    public String getPseudo()                 { return pseudo; }
    public String getEmail()                  { return email; }
    public String getMotDePasseHache()        { return motDePasseHache; }
    public StatutUtilisateur getStatut()      { return statut; }
    public LocalDateTime getDateInscription() { return dateInscription; }

    // -------------------------------------------------------------------------
    // Comportement métier
    // -------------------------------------------------------------------------

    /**
     * Change le statut de l'utilisateur.
     *
     * @throws IllegalStateException si la transition est invalide
     */
    public void changerStatut(StatutUtilisateur nouveauStatut) {
        validerTransition(this.statut, nouveauStatut);
        this.statut = nouveauStatut;
    }

    public boolean estLibre()     { return statut == StatutUtilisateur.LIBRE; }
    public boolean estEnAttente() { return statut == StatutUtilisateur.EN_ATTENTE; }
    public boolean estEnPartie()  { return statut == StatutUtilisateur.EN_PARTIE; }

    // -------------------------------------------------------------------------
    // Validation interne
    // -------------------------------------------------------------------------

    private static String validerNonVide(String valeur, String champ) {
        if (valeur == null || valeur.isBlank()) {
            throw new IllegalArgumentException(champ + " ne peut pas être vide");
        }
        return valeur;
    }

    private static String validerPseudo(String pseudo) {
        if (pseudo == null || pseudo.isBlank()) {
            throw new IllegalArgumentException("Le pseudo ne peut pas être vide");
        }
        if (pseudo.length() < 3 || pseudo.length() > 20) {
            throw new IllegalArgumentException(
                "Le pseudo doit contenir entre 3 et 20 caractères (reçu : " + pseudo.length() + ")");
        }
        if (!pseudo.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException(
                "Le pseudo ne peut contenir que des lettres, chiffres et underscores");
        }
        return pseudo;
    }

    private static String validerEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("L'email ne peut pas être vide");
        }
        // Validation simple mais suffisante : presence d'un @ et d'un domaine
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Format d'email invalide : " + email);
        }
        return email.toLowerCase();
    }

    private static void validerTransition(StatutUtilisateur actuel, StatutUtilisateur nouveau) {
        boolean valide = switch (actuel) {
            case LIBRE      -> nouveau == StatutUtilisateur.EN_ATTENTE;
            case EN_ATTENTE -> nouveau == StatutUtilisateur.LIBRE
                            || nouveau == StatutUtilisateur.EN_PARTIE;
            case EN_PARTIE  -> nouveau == StatutUtilisateur.LIBRE;
        };
        if (!valide) {
            throw new IllegalStateException(
                "Transition invalide : " + actuel + " → " + nouveau);
        }
    }

    // -------------------------------------------------------------------------
    // Equals / hashCode / toString
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Utilisateur other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Utilisateur{pseudo='" + pseudo + "', email='" + email
             + "', statut=" + statut + "}";
    }
}
