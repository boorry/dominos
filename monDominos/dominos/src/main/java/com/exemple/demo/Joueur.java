package com.exemple.demo;
import java.util.*;

public class Joueur {
    private String nom;
    private List<Domino> main = new ArrayList<>();
    private List<Domino> jouables = new ArrayList<>();
    private List<Domino> resteMain = new ArrayList<>();

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
        retournerLesDominosNonJouer();
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

    private List<Domino> lesDominoJouable(List<Domino> enMain, int gaucheTable, int droiteTable){
        List<Domino> tmpListDomino = new ArrayList<>(main);
        for(Domino d: tmpListDomino){
            if(d.isDominoJouable(d, gaucheTable, droiteTable)){
                jouables.add(d);
                //resteMain.remove(d);
            }
        }
        return jouables;
    }

    private void retournerLesDominosNonJouer(){
        if(!jouables.isEmpty()){
            for(Domino d: jouables){
                //ajouterDomino(d);
            }
            jouables.clear();
        }
    }

    public void afficherDominosJouable(List<Domino> jouables_){
        System.out.print("Vos dominos jouables: ");
        for(Domino jouable: jouables_){
            System.out.print(" " + jouable);
        }
        System.out.println(" ");
    }

    public void afficherDominosJoueur() {
        System.out.print("Domino de " + getNom() + ":");
        for (Domino domino : main) {
            //System.out.print(" [" + domino.getGauche() + "|" + domino.getDroite() + "]");
            System.out.print(" " + domino);
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

    public void joueEnRetournant(Domino domino, Deque<Domino> table){
        int gaucheTable = table.isEmpty() ? -1 : table.getFirst().getGauche();
        int droiteTable = table.isEmpty() ? -1 : table.getLast().getDroite();

        boolean canPlayLeft = domino.getDroite() == gaucheTable;
        boolean canPlayRight = domino.getGauche() == droiteTable;
        boolean canPlayLeftFlipped = domino.getGauche() == gaucheTable;
        boolean canPlayRightFlipped = domino.getDroite() == droiteTable;

        if (canPlayLeft || canPlayRight) {
            if (canPlayLeft) {
                table.addFirst(domino);
                System.out.println(getNom() + " pose automatiquement " + domino + " à gauche.");
            } else {
                table.addLast(domino);
                System.out.println(getNom() + " pose automatiquement " + domino + " à droite.");
            }
        } else if (canPlayLeftFlipped || canPlayRightFlipped) {
            domino.retourner();
            if (canPlayLeftFlipped) {
                table.addFirst(domino);
                System.out.println(getNom() + " pose automatiquement " + domino + " à gauche (retourné).");
            } else {
                table.addLast(domino);
                System.out.println(getNom() + " pose automatiquement " + domino + " à droite (retourné).");
            }
        }
    }

    // refactoring

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
