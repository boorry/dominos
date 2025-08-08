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

    public boolean doubleNombre(int nombre) {
        return (gauche == nombre && droite == nombre);
    }

    public static Domino parseDomino(String input) {
        String[] parts;
        
        // Format avec crochets : [x|y]
        if (input.matches("\\[\\d\\|\\d\\]")) {
            parts = input.replaceAll("[\\[\\]]", "").split("\\|");
        }
        // Format avec virgule : x,y ou x, y ou x , y
        else if (input.matches("\\d\\s*,\\s*\\d")) {
            parts = input.split("\\s*,\\s*");
        }
        else {
            throw new IllegalArgumentException("Format invalide. Utilisez : [x|y] ou x,y");
        }
        
        int gauche = Integer.parseInt(parts[0]);
        int droite = Integer.parseInt(parts[1]);
        
        if (gauche < 0 || gauche > 6 || droite < 0 || droite > 6) {
            throw new IllegalArgumentException("Valeurs invalides (doivent être entre 0 et 6)");
        }
        
        return new Domino(gauche, droite);
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
        return Objects.hash(Math.min(gauche, droite), Math.max(gauche, droite));
    }
}
