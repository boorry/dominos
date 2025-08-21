package com.exemple.demo.outil;

public class Jeux {
    private ChoixJeu choix;

    private void jeuChoisi(){
        if(choix != null){
            choix.afficherChoixJeu();
        }
        else{
            System.out.println("Choisissez type de jeu");
        }
    }
    public void choisirTypeJeux(ChoixJeu choix){
        this.choix = choix;
        jeuChoisi();
    }

    public Jeux(){
        
    }
}