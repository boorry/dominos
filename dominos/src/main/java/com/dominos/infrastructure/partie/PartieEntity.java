package com.dominos.infrastructure.partie;

import com.dominos.moteur.EtatPartie;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité JPA — représente la table "partie" en base de données.
 *
 * Une partie contient :
 *  - son identifiant unique
 *  - son état (EN_COURS, VICTOIRE, BLOCAGE, TERMINEE)
 *  - la manche courante
 *  - la date de création
 *  - la liste des joueurs participants (table joueur_partie)
 *
 * Relation @OneToMany :
 *   Une partie contient plusieurs JoueurPartieEntity.
 *   "cascade = ALL" : si on sauvegarde la partie, les joueurs sont
 *   sauvegardés automatiquement.
 *   "orphanRemoval = true" : si on retire un joueur de la liste,
 *   il est supprimé de la base automatiquement.
 */
@Entity
@Table(name = "partie")
public class PartieEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "etat", nullable = false, length = 20)
    private EtatPartie etat;

    @Column(name = "manche_courante", nullable = false)
    private int mancheCourante;

    @Column(name = "cree_le", nullable = false)
    private LocalDateTime creeLe;

    @Column(name = "termine_le")
    private LocalDateTime termineLe;

    /**
     * Liste des joueurs dans cette partie.
     *
     * @OneToMany : une partie → plusieurs joueurs
     * mappedBy   : le champ "partie" dans JoueurPartieEntity porte la relation
     * cascade    : les opérations (save, delete) se propagent aux joueurs
     */
    @OneToMany(mappedBy = "partie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JoueurPartieEntity> joueurs = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Constructeurs
    // -------------------------------------------------------------------------

    /** Constructeur vide requis par JPA. */
    protected PartieEntity() {}

    public PartieEntity(String id, EtatPartie etat, int mancheCourante) {
        this.id            = id;
        this.etat          = etat;
        this.mancheCourante = mancheCourante;
        this.creeLe        = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Accesseurs
    // -------------------------------------------------------------------------

    public String getId()                      { return id; }
    public EtatPartie getEtat()                { return etat; }
    public void setEtat(EtatPartie etat)       { this.etat = etat; }
    public int getMancheCourante()             { return mancheCourante; }
    public void setMancheCourante(int manche)  { this.mancheCourante = manche; }
    public LocalDateTime getCreeLe()           { return creeLe; }
    public LocalDateTime getTermineLe()        { return termineLe; }
    public void setTermineLe(LocalDateTime t)  { this.termineLe = t; }
    public List<JoueurPartieEntity> getJoueurs(){ return joueurs; }
}
