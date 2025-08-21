package com.exemple.demo.outil;

public class Tournoi implements ChoixJeu {

    @Override
    public void leJeu() {
        // implémentation de type tournoi
        // entre 27 joueurs
    }

    @Override
    public void afficherChoixJeu() {
        System.out.println("Vous allez participer à un tournoi");
    }

    @Override
    public int setTotalPoinFinl() {
        return 120; // config
    }

    public Tournoi(){

    }
    
}
