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

    public boolean possedeDomino(Domino d) {
        if (d == null || main == null || main.isEmpty()) {
            return false;
        }
        return main.contains(d);
    }

    public void afficherDominosJoueur() {
        System.out.print("Domino " + getNom() + ":");
        for (Domino domino : main) {
            System.out.print(" [" + domino.getGauche() + "|" + domino.getDroite() + "]");
        }
        System.out.println();
    }

    public boolean hasDouble(int valeur) {
        for (Domino domino : main) {
            if (domino.doubleNombre(valeur)) {
                return true;
            }
        }
        return false;
    }

    public int calculerTotalPoints() {
        int total = 0;
        for (Domino d : main) total += d.getValeurTotale();
        return total;
    }
}
