package com.exemple.demo;
import java.util.*;

public class JeuDomino {
    private final List<Domino> pioche = new ArrayList<>();
    private final List<Joueur> joueurs = new ArrayList<>();
    private final Deque<Domino> table = new LinkedList<>();
    private int joueurCourantIndex;

    public void initialiser() {
        // 1. Créer tous les dominos
        for (int i = 0; i <= 6; i++) {
            for (int j = i; j <= 6; j++) {
                pioche.add(new Domino(i, j));
            }
        }
        Collections.shuffle(pioche);


        listerDominos(pioche);

        // 2. Créer les joueurs
        joueurs.add(new Joueur("Joueur 1"));
        joueurs.add(new Joueur("Joueur 2"));
        joueurs.add(new Joueur("Joueur 3"));

        // 3. Distribuer 7 dominos chacun
        for (int i = 0; i < 7; i++) {
            for (Joueur joueur : joueurs) {
                joueur.ajouterDomino(pioche.remove(0));
            }
        }

        // 4. Chercher le double 6
        for (int i = 0; i < joueurs.size(); i++) {
            for (Domino d : joueurs.get(i).getMain()) {
                if (d.getGauche() == 6 && d.getDroite() == 6) {
                    table.add(d);
                    joueurs.get(i).retirerDomino(d);
                    joueurCourantIndex = i;
                    System.out.println(joueurs.get(i).getNom() + " commence avec [6|6]");
                    return;
                }
            }
        }
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
        dominos.stream().forEach(System.out::println);
        // pour arranger l'affichage de dominos en entier
        for (Domino domino : dominos) {
            
        }

/* 
        dominos.stream()
            .sorted((d1, d2) -> {
                int max1 = Math.max(d1.getGauche(), d1.getDroite());
                int max2 = Math.max(d2.getGauche(), d2.getDroite());
                return Integer.compare(max1, max2);
            })
            .forEach(System.out::println);
*/
    }

}
