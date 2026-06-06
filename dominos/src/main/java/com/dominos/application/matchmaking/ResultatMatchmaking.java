package com.dominos.application.matchmaking;

import com.dominos.domain.partie.PartieEnAttente;

import java.util.Optional;

/**
 * Décrit le résultat d'une tentative de rejoindre la file.
 *
 * Deux cas possibles :
 *
 *  EN_ATTENTE  → le joueur a rejoint la file, pas encore assez de joueurs.
 *                partieEnAttente est vide.
 *
 *  PARTIE_TROUVEE → 3 joueurs réunis, une partie est prête.
 *                   partieEnAttente contient les 3 joueurs.
 *
 * Pourquoi un DTO plutôt qu'une exception pour PARTIE_TROUVEE ?
 *   Trouver une partie n'est pas une erreur — c'est le cas nominal.
 *   Un DTO permet à l'appelant (REST controller, WebSocket handler)
 *   de réagir proprement sans try/catch.
 */
public class ResultatMatchmaking {

    public enum Statut { EN_ATTENTE, PARTIE_TROUVEE }

    private final Statut          statut;
    private final PartieEnAttente partieEnAttente;
    private final int             joueursEnAttente;

    private ResultatMatchmaking(Statut statut,
                                PartieEnAttente partieEnAttente,
                                int joueursEnAttente) {
        this.statut           = statut;
        this.partieEnAttente  = partieEnAttente;
        this.joueursEnAttente = joueursEnAttente;
    }

    // -------------------------------------------------------------------------
    // Factories
    // -------------------------------------------------------------------------

    public static ResultatMatchmaking enAttente(int joueursEnAttente) {
        return new ResultatMatchmaking(Statut.EN_ATTENTE, null, joueursEnAttente);
    }

    public static ResultatMatchmaking partieTrouvee(PartieEnAttente partie) {
        return new ResultatMatchmaking(Statut.PARTIE_TROUVEE, partie, 0);
    }

    // -------------------------------------------------------------------------
    // Accesseurs
    // -------------------------------------------------------------------------

    public Statut getStatut()                        { return statut; }
    public boolean estEnAttente()                    { return statut == Statut.EN_ATTENTE; }
    public boolean estPartieTrouvee()                { return statut == Statut.PARTIE_TROUVEE; }
    public int getJoueursEnAttente()                 { return joueursEnAttente; }
    public Optional<PartieEnAttente> getPartie()     { return Optional.ofNullable(partieEnAttente); }

    @Override
    public String toString() {
        return switch (statut) {
            case EN_ATTENTE    -> "En attente (" + joueursEnAttente + "/3 joueurs)";
            case PARTIE_TROUVEE -> "Partie trouvée : " + partieEnAttente;
        };
    }
}
