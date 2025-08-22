package com.exemple.demo.outil;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import com.exemple.demo.Domino;

public class Table {
     private final Deque<Domino> dominoTable = new LinkedList<>();


    public void viderTable(){
        dominoTable.clear();
    }
    
    public boolean siTableVide(){
        return dominoTable.isEmpty();
    }

    public void ajouterGauche(Domino d) {
        dominoTable.addFirst(d);
    }

    public void ajouterDroite(Domino d) {
        dominoTable.addLast(d);
    }

    public void siDominoEgalDroiteGaucheTable(Domino d, int choix){
        switch (choix) {
            case 1:
                ajouterGauche(d);
                break;
            case 2:
                ajouterDroite(d);
                break;
            default:
            System.out.println("Choisissez où vous voulez placer le domino, 1 au début ou 2 à la fin");
                break;
        }
    }

    public int getGaucheTable(){
        return (dominoTable.isEmpty())? -1 : dominoTable.getFirst().getGauche();
    }

    public int getDroiteTable(){
        return (dominoTable.isEmpty())? -1 : dominoTable.getLast().getDroite();
    }

    public Table(){
        
    }
}
