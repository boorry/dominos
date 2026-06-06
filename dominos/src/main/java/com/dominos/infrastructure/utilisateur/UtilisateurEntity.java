package com.dominos.infrastructure.utilisateur;

import com.dominos.domain.utilisateur.StatutUtilisateur;
import com.dominos.domain.utilisateur.Utilisateur;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entité JPA — représente la table "utilisateur" en base de données.
 *
 * Pourquoi une classe séparée de Utilisateur (domain) ?
 *
 *   Utilisateur (domain) = règles métier pures, pas de dépendance JPA.
 *   UtilisateurEntity    = mapping technique vers la table SQL.
 *
 *   Séparer les deux respecte le principe d'isolation des couches :
 *   votre domaine ne dépend pas de Jakarta/Hibernate.
 *   Si vous changez de base de données demain, seule l'Entity change.
 *
 * Annotations JPA expliquées :
 *   @Entity          → cette classe est mappée à une table
 *   @Table           → nom de la table dans PostgreSQL
 *   @Id              → clé primaire
 *   @Column          → colonne SQL avec ses contraintes
 *   @Enumerated      → stocke un enum en base (STRING = le nom texte)
 */
@Entity
@Table(name = "utilisateur")
public class UtilisateurEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @Column(name = "pseudo", nullable = false, unique = true, length = 20)
    private String pseudo;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "mot_de_passe_hache", nullable = false)
    private String motDePasseHache;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutUtilisateur statut;

    @Column(name = "date_inscription", nullable = false)
    private LocalDateTime dateInscription;

    // -------------------------------------------------------------------------
    // Constructeurs
    // -------------------------------------------------------------------------

    /** Constructeur vide requis par JPA. */
    protected UtilisateurEntity() {}

    // -------------------------------------------------------------------------
    // Conversion domain ↔ entity
    // -------------------------------------------------------------------------

    /**
     * Crée une Entity depuis un objet domaine.
     * Appelé quand on veut sauvegarder un Utilisateur en base.
     */
    public static UtilisateurEntity depuisDomaine(Utilisateur utilisateur) {
        UtilisateurEntity entity = new UtilisateurEntity();
        entity.id              = utilisateur.getId();
        entity.pseudo          = utilisateur.getPseudo();
        entity.email           = utilisateur.getEmail();
        entity.motDePasseHache = utilisateur.getMotDePasseHache();
        entity.statut          = utilisateur.getStatut();
        entity.dateInscription = utilisateur.getDateInscription();
        return entity;
    }

    /**
     * Convertit cette Entity en objet domaine.
     * Appelé quand on lit depuis la base et qu'on veut travailler
     * avec les règles métier.
     */
    public Utilisateur versDomaine() {
        return new Utilisateur(
            id,
            pseudo,
            email,
            motDePasseHache,
            statut,
            dateInscription
        );
    }

    // -------------------------------------------------------------------------
    // Accesseurs (requis par JPA)
    // -------------------------------------------------------------------------

    public String getId()                     { return id; }
    public String getPseudo()                 { return pseudo; }
    public String getEmail()                  { return email; }
    public StatutUtilisateur getStatut()      { return statut; }
    public void setStatut(StatutUtilisateur s){ this.statut = s; }
}
