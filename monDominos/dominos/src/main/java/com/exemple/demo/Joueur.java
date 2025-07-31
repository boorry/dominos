package com.exemple.demo;
import java.util.*;

public class Joueur {
    private String nom;
    private List<Domino> main = new ArrayList<>();

    public Joueur(String nom) {
        this.nom = nom;
    }

    public String getNom() { return nom; }
    public List<Domino> getMain() { return main; }

    public void ajouterDomino(Domino d) {
        main.add(d);
    }

    public void retirerDomino(Domino d) {
        main.remove(d);
    }

    public boolean aDesCoupPossibles(int gauche, int droite) {
        for (Domino d : main) {
            if (d.correspond(gauche) || d.correspond(droite)) return true;
        }
        return false;
    }

    public int calculerTotalPoints() {
        int total = 0;
        for (Domino d : main) total += d.getValeurTotale();
        return total;
    }

    public boolean aDomino(Domino d) {
        return main.contains(d);
    }
}
