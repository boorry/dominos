package com.exemple.demo.outil;

public class Duel implements ChoixJeu {

    @Override
    public void leJeu() {
        // implémentation de duel entre 3 joueurs.
    }

    @Override
    public void afficherChoixJeu() {
        System.out.println("Vous allez participer à une partie de 3");
    }

    @Override
    public int setTotalPoinFinl() {
        return 120; // à configurer
    }

    public Duel(){
        
    }
    
}
