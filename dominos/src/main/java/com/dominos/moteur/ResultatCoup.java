package com.dominos.moteur;

import com.dominos.domain.Joueur;

/**
 * Décrit le résultat d'une action sur le moteur de jeu (jouerCoup / passerTour).
 *
 * C'est le contrat entre le moteur (sans I/O) et la couche qui présente
 * l'information (Console, WebSocket, API REST...).
 *
 * Le moteur ne print rien — il retourne un ResultatCoup.
 * C'est la couche présentation qui décide comment afficher le message.
 *
 * @param joueurActif  Le joueur qui vient d'agir
 * @param aJoue        Vrai si un domino a été posé, faux si passage
 * @param etat         État de la partie après ce coup
 * @param gagnant      Joueur gagnant (non null si etat == VICTOIRE ou BLOCAGE)
 * @param message      Description lisible de ce qui s'est passé
 */
public record ResultatCoup(
    Joueur joueurActif,
    boolean aJoue,
    EtatPartie etat,
    Joueur gagnant,
    String message
) {
    /** Raccourci : la manche/partie est-elle terminée ? */
    public boolean estTermine() {
        return etat == EtatPartie.VICTOIRE
            || etat == EtatPartie.BLOCAGE
            || etat == EtatPartie.TERMINEE;
    }
}
