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

    public boolean possedeCoupPossible(int gauche, int droite) {
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

    public boolean posseeDomino(Domino d) {
        if (d == null || main == null || main.isEmpty()) {
            return false;
        }
        return main.contains(d);
    }

    public void afficherDominosJoueur(){
        System.out.print("Domino " + getNom() + ":");
        for (Domino domino : main) {
            System.out.print(" [" + domino.getGauche() + "|" + domino.getDroite() + "]");
        }
        System.out.print("\n");
    }

    public boolean hasDoubleSix(){
        for(Domino domino: main){
            if(domino.doubleNombre(6)){
                return true;
            }
        }
        return false;
    }

    public boolean tourJoueur(int actif, boolean isTtour){
        if(actif == 1 || isTtour){
            return true;
        }
        return false;
    }

}
