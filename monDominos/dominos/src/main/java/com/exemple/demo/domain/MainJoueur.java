package com.dominos.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Représente la main d'un joueur pour une partie donnée.
 *
 * Responsabilités :
 *  - Contenir les dominos distribués à un joueur
 *  - Calculer les dominos jouables selon les extrémités du plateau
 *  - Calculer le score résiduel (somme des valeurs restantes)
 *
 * Pourquoi extraire depuis Joueur ?
 *  Dans l'ancienne version, Joueur portait à la fois son identité (nom)
 *  et sa main de jeu. Or la main appartient à une PARTIE, pas au joueur :
 *  entre deux parties, la main change. Séparer les deux permet de réutiliser
 *  Joueur dans un contexte persistant (compte utilisateur) sans le coupler
 *  à l'état d'une partie en cours.
 *
 * Bug corrigé :
 *  Dans l'ancienne version, `jouables` était un champ d'instance qui
 *  s'accumulait entre les tours. Ici, getDominosJouables() recalcule
 *  à chaque appel → plus de fuite d'état.
 */
public class MainJoueur {

    private final List<Domino> dominos = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Gestion des dominos
    // -------------------------------------------------------------------------

    public void ajouter(Domino d) {
        if (d == null) throw new IllegalArgumentException("Impossible d'ajouter un domino null");
        dominos.add(d);
    }

    /**
     * Retire le domino de la main.
     *
     * @throws IllegalStateException si le domino n'est pas dans la main
     */
    public void retirer(Domino d) {
        if (!dominos.remove(d)) {
            throw new IllegalStateException("Domino " + d + " absent de la main");
        }
    }

    public boolean contient(Domino d) {
        return dominos.contains(d);
    }

    public boolean estVide() {
        return dominos.isEmpty();
    }

    public int taille() {
        return dominos.size();
    }

    /**
     * Retourne une vue non modifiable de la main.
     */
    public List<Domino> getDominos() {
        return Collections.unmodifiableList(dominos);
    }

    public void vider() {
        dominos.clear();
    }

    // -------------------------------------------------------------------------
    // Logique métier
    // -------------------------------------------------------------------------

    /**
     * Retourne la liste des dominos jouables selon les extrémités du plateau.
     *
     * Si le plateau est vide (gaucheTable == -1 && droiteTable == -1),
     * tous les dominos sont jouables (premier coup de la manche).
     *
     * Calcul pur — aucun effet de bord, aucun champ muté.
     */
    public List<Domino> getDominosJouables(int gaucheTable, int droiteTable) {
        if (gaucheTable == -1 && droiteTable == -1) {
            return new ArrayList<>(dominos);
        }
        List<Domino> jouables = new ArrayList<>();
        for (Domino d : dominos) {
            if (d.estJouable(gaucheTable, droiteTable)) {
                jouables.add(d);
            }
        }
        return jouables;
    }

    /**
     * Vrai si le joueur possède au moins un domino double valant exactement
     * la valeur donnée (utilisé pour déterminer qui commence la manche 1).
     */
    public boolean possedeDouble(int valeur) {
        return dominos.stream().anyMatch(d -> d.estDouble(valeur));
    }

    /**
     * Somme des valeurs de tous les dominos restants.
     * Utilisée pour déterminer le gagnant en cas de blocage.
     */
    public int calculerScore() {
        return dominos.stream().mapToInt(Domino::getValeurTotale).sum();
    }
}
