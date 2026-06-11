package com.dominos.domain.tournoi;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Représente un match dans le contexte d'un tournoi.
 *
 * Un match relie :
 *  - une phase du tournoi (GROUPES, QUARTS, etc.)
 *  - les 3 joueurs participants (leurs ids)
 *  - l'identifiant de la partie créée (partieId)
 *  - le gagnant (null si match en cours)
 *
 * Invariant : toujours exactement 3 joueurs par match.
 */
public class MatchTournoi {

    private final String       id;
    private final PhaseTournoi phase;
    private final int          numeroGroupe;
    private final List<String> joueursIds;   // utilisateurIds
    private       String       partieId;     // lié à la Partie créée
    private       String       gagnantId;    // null si en cours
    private       StatutMatch  statut;

    public enum StatutMatch { EN_ATTENTE, EN_COURS, TERMINE }

    public MatchTournoi(PhaseTournoi phase, int numeroGroupe, List<String> joueursIds) {
        if (joueursIds == null || joueursIds.size() != 3) {
            throw new IllegalArgumentException(
                "Un match requiert exactement 3 joueurs");
        }
        this.id           = UUID.randomUUID().toString();
        this.phase        = phase;
        this.numeroGroupe = numeroGroupe;
        this.joueursIds   = Collections.unmodifiableList(List.copyOf(joueursIds));
        this.statut       = StatutMatch.EN_ATTENTE;
    }

    // -------------------------------------------------------------------------
    // Comportement métier
    // -------------------------------------------------------------------------

    public void demarrer(String partieId) {
        if (statut != StatutMatch.EN_ATTENTE) {
            throw new IllegalStateException("Le match a déjà démarré");
        }
        this.partieId = partieId;
        this.statut   = StatutMatch.EN_COURS;
    }

    public void terminer(String gagnantId) {
        if (statut != StatutMatch.EN_COURS) {
            throw new IllegalStateException("Le match n'est pas en cours");
        }
        if (!joueursIds.contains(gagnantId)) {
            throw new IllegalArgumentException(
                "Le gagnant doit être l'un des joueurs du match");
        }
        this.gagnantId = gagnantId;
        this.statut    = StatutMatch.TERMINE;
    }

    public boolean estTermine()   { return statut == StatutMatch.TERMINE; }
    public boolean estEnCours()   { return statut == StatutMatch.EN_COURS; }
    public boolean estEnAttente() { return statut == StatutMatch.EN_ATTENTE; }

    // -------------------------------------------------------------------------
    // Accesseurs
    // -------------------------------------------------------------------------

    public String       getId()           { return id; }
    public PhaseTournoi getPhase()        { return phase; }
    public int          getNumeroGroupe() { return numeroGroupe; }
    public List<String> getJoueursIds()  { return joueursIds; }
    public String       getPartieId()    { return partieId; }
    public String       getGagnantId()   { return gagnantId; }
    public StatutMatch  getStatut()      { return statut; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MatchTournoi other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Match{phase=" + phase + ", groupe=" + numeroGroupe
            + ", statut=" + statut + "}";
    }
}
