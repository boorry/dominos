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

    public int getGauche() { 
        return gauche; 
    }

    public int getDroite() { 
        return droite; 
    }

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

    public boolean isDominoJouable(Domino item, int gaucheTable, int droiteTable){
        return (gaucheTable == item.getGauche()
                || droiteTable == item.getGauche() 
                || gaucheTable == item.getDroite() 
                || droiteTable == item.getDroite());
    }

    public static Domino parseDomino(String input){
        String[] valeurs = extraireValeurs(input);
        return valeurDeDomino(valeurs);
    }

    private static Domino valeurDeDomino(String[] entre){
        int gauche = Integer.parseInt(entre[0]);
        int droite = Integer.parseInt(entre[1]);

        if(isInvalidDomino(gauche, droite)){
            throw new IllegalArgumentException("Valeurs invalides (doivent être entre 0 et 6)");
        }
        return new Domino(gauche, droite);
    }

    private static boolean isInvalidDomino(int gauche, int droite){
        return (gauche < 0 || gauche > 6 || droite < 0 || droite > 6);
    }

    private static String[] extraireValeurs(String input){
        if(isFormatCrochet(input)){
            return input.replaceAll("[\\[\\]]", "").split("\\|");
        }
        else if(isFormatVirgule(input)){
            return input.split("\\s*,\\s*");
        }
        else{
            throw new IllegalArgumentException("Format invalide. Utilisez : [x|y] ou x,y");
        }
    }

    private static boolean isFormatCrochet(String input){
        return input.matches("\\[\\d\\|\\d\\]");
    }

    private static boolean isFormatVirgule(String input){
        return input.matches("\\d\\s*,\\s*\\d");
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
