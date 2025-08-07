package com.exemple.demo;
import java.util.*;

public class JeuDomino {
    private final List<Domino> pioche = new ArrayList<>();
    private final List<Joueur> joueurs = new ArrayList<>();
    private final Deque<Domino> table = new LinkedList<>();
    private int joueurCourantIndex;

    private final Joueur Joueur_1 = new Joueur("Joueur_1");
    private final Joueur Joueur_2 = new Joueur("Joueur_2");
    private final Joueur Joueur_3 = new Joueur("Joueur_3");

    public void lesTroisJoueurs(Joueur j1, Joueur j2, Joueur j3){
        joueurs.add(j1); joueurs.add(j2); joueurs.add(j3);
    }

    public void initialiser() {
        // 1. Créer tous les dominos
        for(int gauche = 6; gauche >= 0; gauche--){
            for(int droite = gauche; droite >= 0; droite--){
                pioche.add(new Domino(gauche, droite));
            }
        }
        Collections.shuffle(pioche);

        listerDominos(pioche);

        // 2. Créer les joueurs -> joueurs.add(new Joueur("Joueur_1"));
        lesTroisJoueurs(Joueur_1, Joueur_2, Joueur_3);

        // 3. Distribuer 7 dominos chacun
        while (pioche.size() > 7) {
            for(Joueur joueur: joueurs){
                joueur.ajouterDomino(pioche.remove(0));
            }
        }

        Joueur_1.afficherDominosJoueur();
        Joueur_2.afficherDominosJoueur();
        Joueur_3.afficherDominosJoueur();


        //affichePioche(pioche);

        System.out.println(" -- ");

        findDoubleSix(pioche);

        System.out.println(" -- ");

        casDeBlocage(joueurs);

    }

    public void jouer() {
        Scanner scanner = new Scanner(System.in);
        int passesConsecutives = 0;

        while (true) {
            Joueur joueur = joueurs.get(joueurCourantIndex);
            System.out.println("\n" + joueur.getNom() + " joue...");
            afficherTable();

            boolean aJoue = false;
            List<Domino> main = joueur.getMain();

            if (!table.isEmpty()) {
                int gauche = table.getFirst().getGauche();
                int droite = table.getLast().getDroite();

                for (Domino d : new ArrayList<>(main)) {
                    if (d.correspond(gauche)) {
                        if (d.getDroite() == gauche) d.retourner();
                        table.addFirst(d);
                        joueur.retirerDomino(d);
                        System.out.println(joueur.getNom() + " joue " + d + " à gauche.");
                        aJoue = true;
                        break;
                    } else if (d.correspond(droite)) {
                        if (d.getGauche() == droite) d.retourner();
                        table.addLast(d);
                        joueur.retirerDomino(d);
                        System.out.println(joueur.getNom() + " joue " + d + " à droite.");
                        aJoue = true;
                        break;
                    }
                }
            } else {
                // Premier tour déjà joué, ne devrait pas arriver
            }

            if (!aJoue) {
                System.out.println(joueur.getNom() + " passe son tour.");
                passesConsecutives++;
            } else {
                passesConsecutives = 0;
            }

            // Vérifier victoire
            if (joueur.getMain().isEmpty()) {
                System.out.println("\n " + joueur.getNom() + " a gagné (plus de dominos) !");
                break;
            }

            // Vérifier blocage
            if (passesConsecutives >= 3) {
                System.out.println("\n Jeu bloqué !");
                Joueur gagnant = joueurs.get(0);
                int min = gagnant.calculerTotalPoints();
                for (Joueur j : joueurs) {
                    int total = j.calculerTotalPoints();
                    System.out.println(j.getNom() + " : " + total + " points");
                    if (total < min) {
                        min = total;
                        gagnant = j;
                    }
                }
                System.out.println(" Victoire!" + gagnant.getNom() + " gagne avec le moins de points !");
                break;
            }

            joueurCourantIndex = (joueurCourantIndex + 1) % 3;
        }

        afficherTable();
        System.out.println(" Fin de la partie.");
    }

    private void afficherTable() {
        System.out.print("Table: ");
        for (Domino d : table) {
            System.out.print(d + " ");
        }
        System.out.println();
    }

    public void listerDominos(List<Domino> dominos){
        int index = 0;
        for (int i = 6; i >= 0; i--) {
            for (int j = i; j >= 0; j--) {
                Domino d = dominos.get(index++);
                System.out.print(" [" + d.getGauche() + "|" + d.getDroite() + "]");
            }
            System.out.println();
        }
    }

    public void affichePioche(List<Domino> dominos){
        System.out.print("Domino Pioche :");
        for (Domino domino : dominos) {
            System.out.print(" [" + domino.getGauche() + "|" + domino.getDroite() + "]");
        }
        System.out.print("\n");
    }

    public void findDoubleSix(List<Domino> pioche) {
        boolean trouve = false;
        for (Joueur joueur : joueurs) {
            if (joueur.hasDoubleSix()) {
                System.out.println("Le joueur " + joueur.getNom() + " possède le double 6 et commence le jeu.");
                trouve = true;
                break; // double six trouvé
            }
        }

        if (!trouve) {
            System.out.println("Aucun joueur ne possède le double 6. On affiche la pioche :");
            affichePioche(pioche);
        }
    }

    public void casDeBlocage(List<Joueur> joueurs){
       if (joueurs.isEmpty()) {
            System.out.println("Aucun joueur dans la liste");
            return;
        }
    
        // Trouver le minimum
        int minimum = Integer.MAX_VALUE;
        for (Joueur joueur : joueurs) {
            int points = joueur.calculerTotalPoints();
            if (points < minimum) {
                minimum = points;
            }
        }
    
        // Affichage des points de chaque joueur
        for (Joueur joueur : joueurs) {
            System.out.println(joueur.getNom() + ": " + joueur.calculerTotalPoints() + " points");
        }
    
        // Compter combien de joueurs ont le minimum
        int nombreGagnants = 0;
        Joueur gagnantUnique = null;
    
        for (Joueur joueur : joueurs) {
            if (joueur.calculerTotalPoints() == minimum) {
                nombreGagnants++;
                gagnantUnique = joueur;
            }
        }
    
        if (nombreGagnants > 1) {
            System.out.println("Égalité avec " + minimum + " points - Aucun gagnant");
        } else {
            System.out.println("Gagnant: " + gagnantUnique.getNom() + " avec " + minimum + " points");
        }
    }




 


}
