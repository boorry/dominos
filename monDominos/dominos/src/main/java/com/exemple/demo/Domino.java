package com.exemple.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Domino {
    private int gauche;
    private int droite;

    public Domino(int gauche, int droite) {
        this.gauche = gauche;
        this.droite = droite;
    }

    public int getGauche() { return gauche; }
    public int getDroite() { return droite; }

    public void retourner() {
        int tmp = gauche;
        gauche = droite;
        droite = tmp;
    }

    public boolean correspond(int valeur) {
        return gauche == valeur || droite == valeur;
    }

    public int getValeurTotale() {
        return gauche + droite;
    }

    public boolean doubleNombre(int nombre){
        return (gauche == nombre && droite == nombre);
    }

    @Override
    public String toString() {
        return "[" + gauche + "|" + droite + "]";
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Domino domino = (Domino) o;
        return (gauche == domino.gauche && droite == domino.droite) ||
               (gauche == domino.droite && droite == domino.gauche);
    }

    @Override
    public int hashCode() {
        // Générer un hash symétrique pour [x|y] et [y|x]
        return Objects.hash(Math.min(gauche, droite), Math.max(gauche, droite));
    }

}
