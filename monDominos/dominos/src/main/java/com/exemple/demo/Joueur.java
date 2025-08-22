package com.exemple.demo;
import java.util.*;

public class Joueur {
    private String nom;
    private List<Domino> main = new ArrayList<>();

    public Joueur(String nom) {
        this.nom = nom;
    }

    public String getNom() { 
        return nom; 
    }

    public List<Domino> getMain() { 
        return main; 
    }

    public void ajouterDomino(Domino d) {
        main.add(d);
    }

    public void viderMainJoueur(){
        main.clear();
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

    public List<Domino> getDominosJouables(int gaucheTable, int droiteTable) {
        if (gaucheTable == -1 && droiteTable == -1) {
            return  new ArrayList<>(main);
        } else {
            return lesDominoJouable(main, gaucheTable, droiteTable);
        }
    }
    
    private boolean isDominoJouable(Domino item, int gaucheTable, int droiteTable){
        return (gaucheTable == item.getGauche()
                || droiteTable == item.getGauche() 
                || gaucheTable == item.getDroite() 
                || droiteTable == item.getDroite());
    }

    private List<Domino> lesDominoJouable(List<Domino> enMain, int gaucheTable, int droiteTable){
        List<Domino> jouables = new ArrayList<>();
        for(Domino d: enMain){
            if(isDominoJouable(d, gaucheTable, droiteTable)){
                jouables.add(d);
            }
        }
        return jouables;
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
        for (Domino d : main) 
            total += d.getValeurTotale();
        return total;
    }

    public void retournerDominoSiBesoin(Domino d, Deque<Domino> table) {
        if (peutJouerAGauche(d, table)) {
            jouerAGauche(d, table, false);
        } else if (peutJouerADroite(d, table)) {
            jouerADroite(d, table, false);
        } else if (peutJouerAGaucheRetourner(d, table)) {
            jouerAGauche(d, table, true);
        } else if (peutJouerADroiteRetourner(d, table)) {
            jouerADroite(d, table, true);
        }
    }

    private void jouerAGauche(Domino d, Deque<Domino> table, boolean retourner) {
        if (retourner) d.retourner();
        table.addFirst(d);
        System.out.println(getNom() + " pose automatiquement " + d + " à gauche" + (retourner ? " (retourné)" : "") + ".");
    }

    private void jouerADroite(Domino d, Deque<Domino> table, boolean retourner) {
        if (retourner) d.retourner();
        table.addLast(d);
        System.out.println(getNom() + " pose automatiquement " + d + " à droite" + (retourner ? " (retourné)" : "") + ".");
    }

    private boolean peutJouerAGauche(Domino d, Deque<Domino> table) {
        int gaucheTable = table.isEmpty() ? -1 : table.getFirst().getGauche();
        return d.getDroite() == gaucheTable;
    }

    private boolean peutJouerADroite(Domino d, Deque<Domino> table) {
        int droiteTable = table.isEmpty() ? -1 : table.getLast().getDroite();
        return d.getGauche() == droiteTable;
    }

    private boolean peutJouerAGaucheRetourner(Domino d, Deque<Domino> table) {
        int gaucheTable = table.isEmpty() ? -1 : table.getFirst().getGauche();
        return d.getGauche() == gaucheTable;
    }

    private boolean peutJouerADroiteRetourner(Domino d, Deque<Domino> table) {
        int droiteTable = table.isEmpty() ? -1 : table.getLast().getDroite();
        return d.getDroite() == droiteTable;
    }
}
