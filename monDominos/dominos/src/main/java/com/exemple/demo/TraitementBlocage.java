package com.exemple.demo;


import java.util.List;

public class TraitementBlocage {

       // La factorisation
    public Joueur casDeBlocage( List<Joueur> joueurs, int manche) {
        if (joueurs.isEmpty()) {
            System.out.println("Aucun joueur dans la liste");
            return null;
        }

        Joueur gagnant = trouverGagnantMinPoints(joueurs);
        int minimum = gagnant.calculerTotalPoints();

        int nombreGagnants = compterJoueursAvecMinPoints(joueurs, minimum);

        return annoncerResultatBlocage(gagnant, nombreGagnants, minimum, manche);
    }

    private Joueur trouverGagnantMinPoints(List<Joueur> joueurs){
        Joueur gagnant = joueurs.get(0);
        int minimum = gagnant.calculerTotalPoints();
        for(Joueur joueur: joueurs){
            int points = joueur.calculerTotalPoints();
            System.out.println(joueur.getNom() + ": " + points + " points");
            if(points < minimum){
                minimum = points;
                gagnant = joueur;
            }
        }
        return gagnant;
    }

    private int compterJoueursAvecMinPoints(List<Joueur> joueurs, int minPoints){
        int nombreGagnants = 0;
        for(Joueur joueur: joueurs){
            if(joueur.calculerTotalPoints() == minPoints){
                nombreGagnants++;
            }
        }
        return nombreGagnants;
    }

    private Joueur annoncerResultatBlocage(Joueur joueur, int nombreGangnant, int minimum, int manche){
        if(nombreGangnant > 1){
            System.out.println("Égalité avec " + minimum + " points - Aucun gagnant pour la manche " + manche);
            return null;
        } else{
            System.out.println("Manche - " + manche + ". " + joueur.getNom() + " possède le minimum de point:" + minimum + ". Gangne la manche.");
            return joueur;
        }
    }
    
    public TraitementBlocage(){

    }
}
