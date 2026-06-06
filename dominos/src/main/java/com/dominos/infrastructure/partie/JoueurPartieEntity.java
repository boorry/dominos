package com.dominos.infrastructure.partie;

import jakarta.persistence.*;

/**
 * Entité JPA — représente la table "joueur_partie".
 *
 * Cette table fait le lien entre un utilisateur et une partie :
 *   utilisateur_id | partie_id | score | position
 *   ───────────────|───────────|───────|─────────
 *   uuid-alice     | uuid-p1   |  45   |   1
 *   uuid-bob       | uuid-p1   |  20   |   2
 *   uuid-carl      | uuid-p1   |  55   |   3
 *
 * Relation @ManyToOne :
 *   Plusieurs joueurs → une partie.
 *   @JoinColumn : la colonne "partie_id" dans cette table
 *   pointe vers la clé primaire de la table "partie".
 */
@Entity
@Table(name = "joueur_partie")
public class JoueurPartieEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Référence vers la partie.
     * @ManyToOne : plusieurs joueurs peuvent être dans une partie.
     * @JoinColumn : crée la colonne "partie_id" comme clé étrangère.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partie_id", nullable = false)
    private PartieEntity partie;

    @Column(name = "utilisateur_id", nullable = false, length = 36)
    private String utilisateurId;

    @Column(name = "pseudo", nullable = false, length = 20)
    private String pseudo;

    @Column(name = "score", nullable = false)
    private int score;

    /** Position d'arrivée (1 = gagnant, 2, 3). Null si partie en cours. */
    @Column(name = "position")
    private Integer position;

    // -------------------------------------------------------------------------
    // Constructeurs
    // -------------------------------------------------------------------------

    protected JoueurPartieEntity() {}

    public JoueurPartieEntity(PartieEntity partie,
                               String utilisateurId,
                               String pseudo) {
        this.partie        = partie;
        this.utilisateurId = utilisateurId;
        this.pseudo        = pseudo;
        this.score         = 0;
    }

    // -------------------------------------------------------------------------
    // Accesseurs
    // -------------------------------------------------------------------------

    public Long getId()                  { return id; }
    public PartieEntity getPartie()      { return partie; }
    public String getUtilisateurId()     { return utilisateurId; }
    public String getPseudo()            { return pseudo; }
    public int getScore()                { return score; }
    public void setScore(int score)      { this.score = score; }
    public Integer getPosition()         { return position; }
    public void setPosition(Integer pos) { this.position = pos; }
}
