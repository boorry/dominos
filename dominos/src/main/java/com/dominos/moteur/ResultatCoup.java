package com.dominos.moteur;

import com.dominos.domain.Joueur;
import com.dominos.domain.Plateau;

import java.util.Set;

/**
 * Décrit le résultat d'une action sur le moteur de jeu.
 *
 * Nouveau champ : cotesPossibles
 *   Non null uniquement quand etat == CHOIX_REQUIS.
 *   Contient {GAUCHE, DROITE} — le joueur doit rappeler
 *   jouerCoupAvecCote() en précisant son choix.
 */
public record ResultatCoup(
    Joueur            joueurActif,
    boolean           aJoue,
    EtatPartie        etat,
    Joueur            gagnant,
    String            message,
    Set<Plateau.Cote> cotesPossibles   // non null si CHOIX_REQUIS
) {
    /** Constructeur sans cotesPossibles (cas normal). */
    public ResultatCoup(Joueur joueurActif, boolean aJoue,
                        EtatPartie etat, Joueur gagnant, String message) {
        this(joueurActif, aJoue, etat, gagnant, message, null);
    }

    public boolean estTermine() {
        return etat == EtatPartie.VICTOIRE
            || etat == EtatPartie.BLOCAGE
            || etat == EtatPartie.TERMINEE;
    }

    public boolean choixRequis() {
        return etat == EtatPartie.CHOIX_REQUIS;
    }
}
