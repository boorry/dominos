package com.dominos.domain;

import java.util.Objects;

/**
 * Value Object représentant une pièce de domino.
 *
 * Invariants :
 *  - gauche et droite sont entre 0 et 6 inclus
 *  - deux dominos sont égaux si leurs valeurs correspondent dans n'importe quel sens
 *
 * Changements vs version originale :
 *  - isDominoJouable(Domino item, ...) → correspond(int, int) : suppression du
 *    paramètre redondant, renommage pour clarté
 *  - parseDomino / extraireValeurs déplacés dans DominoParser (SRP)
 *  - retourner() renommé retourner() mais rendu immutable-friendly :
 *    on expose retourné() qui retourne une nouvelle instance
 */
public final class Domino {

    private final int gauche;
    private final int droite;

    public Domino(int gauche, int droite) {
        valider(gauche, droite);
        this.gauche = gauche;
        this.droite = droite;
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private static void valider(int gauche, int droite) {
        if (gauche < 0 || gauche > 6 || droite < 0 || droite > 6) {
            throw new IllegalArgumentException(
                "Valeurs invalides : [" + gauche + "|" + droite + "] — attendu entre 0 et 6"
            );
        }
    }

    // -------------------------------------------------------------------------
    // Accesseurs
    // -------------------------------------------------------------------------

    public int getGauche() {
        return gauche;
    }

    public int getDroite() {
        return droite;
    }

    public int getValeurTotale() {
        return gauche + droite;
    }

    // -------------------------------------------------------------------------
    // Comportement métier
    // -------------------------------------------------------------------------

    /**
     * Retourne une nouvelle instance avec gauche et droite inversés.
     * Domino est maintenant immutable — on ne mute plus l'objet.
     */
    public Domino retourner() {
        return new Domino(droite, gauche);
    }

    /**
     * Vrai si ce domino possède la valeur donnée d'un côté ou de l'autre.
     */
    public boolean correspond(int valeur) {
        return gauche == valeur || droite == valeur;
    }

    /**
     * Vrai si ce domino peut être posé sur un plateau dont les extrémités
     * sont gaucheTable et droiteTable.
     *
     * Remplace isDominoJouable(Domino item, int gaucheTable, int droiteTable)
     * — le paramètre item était toujours this, ce qui était redondant.
     */
    public boolean estJouable(int gaucheTable, int droiteTable) {
        return gauche == gaucheTable
            || droite == gaucheTable
            || gauche == droiteTable
            || droite == droiteTable;
    }

    /**
     * Vrai si les deux faces sont identiques (ex. [3|3]).
     */
    public boolean estDouble() {
        return gauche == droite;
    }

    /**
     * Vrai si les deux faces valent exactement la valeur donnée (ex. [6|6]).
     */
    public boolean estDouble(int valeur) {
        return gauche == valeur && droite == valeur;
    }

    // -------------------------------------------------------------------------
    // Equals / hashCode / toString
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "[" + gauche + "|" + droite + "]";
    }

    /**
     * Deux dominos sont égaux si leurs valeurs correspondent dans n'importe
     * quel sens : [3|5] == [5|3].
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Domino other)) return false;
        return (gauche == other.gauche && droite == other.droite)
            || (gauche == other.droite && droite == other.gauche);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Math.min(gauche, droite), Math.max(gauche, droite));
    }
}
