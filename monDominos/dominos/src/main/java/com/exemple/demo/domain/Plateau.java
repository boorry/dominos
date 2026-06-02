package com.dominos.domain;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * Représente la chaîne de dominos posés sur la table.
 *
 * Responsabilités :
 *  - Maintenir la chaîne ordonnée de dominos
 *  - Exposer les extrémités gauche et droite
 *  - Valider et poser un domino à gauche ou à droite
 *  - Déterminer automatiquement le côté de pose (et si retournement nécessaire)
 *
 * Changements vs Table originale :
 *  - Renommé Plateau (cohérent avec la nomenclature du cahier des charges)
 *  - poserDomino() remplace siDominoEgalDroiteGaucheTable() + tout le code
 *    de placement dispersé dans Joueur
 *  - Domino est maintenant immutable → retourner() retourne une nouvelle instance
 *  - getGaucheTable() / getDroiteTable() retournent Optional<Integer> :
 *    plus clair que la convention "-1 = vide" de l'ancienne version
 *
 * Pourquoi sortir la logique de placement de Joueur ?
 *  Dans l'ancienne version, c'est Joueur qui décidait où poser le domino
 *  (retournerDominoSiBesoin, jouerAGauche, jouerADroite...). Cela crée
 *  une dépendance Joueur → Table (sens inverse du domaine). Plateau doit
 *  être maître de sa propre cohérence.
 */
public class Plateau {

    private final Deque<Domino> chaine = new ArrayDeque<>();

    // -------------------------------------------------------------------------
    // Consultation
    // -------------------------------------------------------------------------

    public boolean estVide() {
        return chaine.isEmpty();
    }

    /**
     * Valeur de l'extrémité gauche du plateau.
     * Empty si le plateau est vide.
     */
    public Optional<Integer> getExtrémiteGauche() {
        return chaine.isEmpty()
            ? Optional.empty()
            : Optional.of(chaine.peekFirst().getGauche());
    }

    /**
     * Valeur de l'extrémité droite du plateau.
     * Empty si le plateau est vide.
     */
    public Optional<Integer> getExtremiteDroite() {
        return chaine.isEmpty()
            ? Optional.empty()
            : Optional.of(chaine.peekLast().getDroite());
    }

    /**
     * Valeur gauche pour les calculs de jouabilité.
     * Retourne -1 si le plateau est vide (convention interne, non exposée).
     */
    public int getGaucheOuMoinsUn() {
        return getExtrémiteGauche().orElse(-1);
    }

    /**
     * Valeur droite pour les calculs de jouabilité.
     * Retourne -1 si le plateau est vide.
     */
    public int getDroiteOuMoinsUn() {
        return getExtremiteDroite().orElse(-1);
    }

    /**
     * Vue non modifiable de la chaîne, de gauche à droite.
     */
    public List<Domino> getDominos() {
        return Collections.unmodifiableList(new ArrayList<>(chaine));
    }

    // -------------------------------------------------------------------------
    // Placement
    // -------------------------------------------------------------------------

    /**
     * Pose le premier domino sur un plateau vide.
     *
     * @throws IllegalStateException si le plateau n'est pas vide
     */
    public void poserPremier(Domino d) {
        if (!chaine.isEmpty()) {
            throw new IllegalStateException("Le plateau n'est pas vide — utilisez poser()");
        }
        chaine.addLast(d);
    }

    /**
     * Pose un domino sur le plateau en déterminant automatiquement le côté
     * et en retournant le domino si nécessaire.
     *
     * Retourne le ResultatPose qui décrit ce qui a été fait :
     * côté posé et si le domino a été retourné.
     *
     * @throws IllegalArgumentException si le domino n'est pas jouable
     * @throws IllegalStateException    si le plateau est vide (utiliser poserPremier)
     */
    public ResultatPose poser(Domino d) {
        if (chaine.isEmpty()) {
            throw new IllegalStateException("Plateau vide — utilisez poserPremier()");
        }

        int gaucheTable = getGaucheOuMoinsUn();
        int droiteTable = getDroiteOuMoinsUn();

        if (!d.estJouable(gaucheTable, droiteTable)) {
            throw new IllegalArgumentException(
                "Le domino " + d + " ne peut pas être posé sur [" + gaucheTable + "|...|" + droiteTable + "]"
            );
        }

        // Priorité : sans retournement d'abord
        if (d.getDroite() == gaucheTable) {
            chaine.addFirst(d);
            return new ResultatPose(d, Cote.GAUCHE, false);
        }
        if (d.getGauche() == droiteTable) {
            chaine.addLast(d);
            return new ResultatPose(d, Cote.DROITE, false);
        }

        // Avec retournement
        Domino dRetourné = d.retourner();
        if (dRetourné.getDroite() == gaucheTable) {
            chaine.addFirst(dRetourné);
            return new ResultatPose(dRetourné, Cote.GAUCHE, true);
        }

        // Dernier cas : droite du retourné == droite table
        chaine.addLast(dRetourné);
        return new ResultatPose(dRetourné, Cote.DROITE, true);
    }

    /**
     * Réinitialise le plateau pour une nouvelle manche.
     */
    public void vider() {
        chaine.clear();
    }

    // -------------------------------------------------------------------------
    // Types internes
    // -------------------------------------------------------------------------

    public enum Cote { GAUCHE, DROITE }

    /**
     * Décrit le résultat d'une pose : côté choisi et si le domino a été retourné.
     * Utilisé par le MoteurJeu pour construire le message d'événement.
     */
    public record ResultatPose(Domino domino, Cote cote, boolean retourné) {

        @Override
        public String toString() {
            return domino + " posé à " + cote + (retourné ? " (retourné)" : "");
        }
    }
}
