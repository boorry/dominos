package com.exemple.demo;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public String toString() {
        return "[" + gauche + "|" + droite + "]";
    }

}
