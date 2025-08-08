package com.exemple.demo;
import java.util.*;

public class JeuDomino {
    private List<Domino> pioche = new ArrayList<>();
    private final List<Joueur> joueurs = new ArrayList<>();
    private final Deque<Domino> table = new LinkedList<>();
    private int joueurCourantIndex;
    private final Map<Joueur, Integer> scores = new HashMap<>();
    private static final int SCORE_MAX = 120;
    private int manche;
    private final Scanner scanner = new Scanner(System.in);

    private final Joueur Joueur_1 = new Joueur("Joueur_1");
    private final Joueur Joueur_2 = new Joueur("Joueur_2");
    private final Joueur Joueur_3 = new Joueur("Joueur_3");

    public static void main(String[] args) {
        JeuDomino jeu = new JeuDomino();
        jeu.jouerPartie();
    }

    public void jouerPartie() {
        // Initialiser les scores et le compteur de manches
        scores.put(Joueur_1, 0);
        scores.put(Joueur_2, 0);
        scores.put(Joueur_3, 0);
        manche = 1;

        while (true) {
            System.out.println("\n=================================");
            System.out.println("=== Début de la Manche " + manche + " ===");
            System.out.println("=================================");
            initialiser();
            Joueur gagnantManche = jouer();
            int points = calculerPointsManche(gagnantManche);

            // Ajouter les points au gagnant
            if (gagnantManche != null) {
                scores.put(gagnantManche, scores.get(gagnantManche) + points);
                System.out.println(gagnantManche.getNom() + " gagne la manche " + manche + " avec " + points + " points !");
                afficherScores();
            } else {
                System.out.println("Égalité dans la manche " + manche + ". Aucun point attribué.");
                afficherScores();
            }

            // Vérifier si un joueur a atteint 120 points
            for (Joueur joueur : joueurs) {
                if (scores.get(joueur) >= SCORE_MAX) {
                    System.out.println("\n=================================");
                    System.out.println("=== Fin de la Partie après " + manche + " manches ===");
                    System.out.println(joueur.getNom() + " gagne la partie avec " + scores.get(joueur) + " points !");
                    System.out.println("\nRésumé final:");
                    afficherScores();
                    return;
                }
            }

            manche++;
            // Réinitialiser pour la prochaine manche
            table.clear();
            for (Joueur joueur : joueurs) {
                joueur.getMain().clear();
            }
        }
    }

    private void initialiser() {
        // 1. Réinitialiser la pioche
        pioche.clear();
        for (int gauche = 0; gauche <= 6; gauche++) {
            for (int droite = gauche; droite <= 6; droite++) {
                pioche.add(new Domino(gauche, droite));
            }
        }
        Collections.shuffle(pioche);

        // Afficher la pioche pour débogage
        listerDominos(pioche);

        // 2. Ajouter les joueurs (si non déjà fait)
        if (joueurs.isEmpty()) {
            joueurs.add(Joueur_1);
            joueurs.add(Joueur_2);
            joueurs.add(Joueur_3);
        }

        // 3. Distribuer 7 dominos à chaque joueur
        for (Joueur joueur : joueurs) {
            for (int i = 0; i < 7; i++) {
                if (!pioche.isEmpty()) {
                    joueur.ajouterDomino(pioche.remove(0));
                }
            }
        }

        // Afficher les mains des joueurs
        Joueur_1.afficherDominosJoueur();
        Joueur_2.afficherDominosJoueur();
        Joueur_3.afficherDominosJoueur();

        // 4. Déterminer le joueur qui commence
        if (manche == 1) {
            System.out.println(" ----- FIND HIGHEST DOUBLE ---- ");
            joueurCourantIndex = findHighestDouble();
            System.out.println(joueurs.get(joueurCourantIndex).getNom() + " commence la manche " + manche + ".");
        } else {
            joueurCourantIndex = (manche - 1) % 3;
            System.out.println(joueurs.get(joueurCourantIndex).getNom() + " commence la manche " + manche + " (rotation).");
        }
    }

    private Joueur jouer() {
        int passesConsecutives = 0;

        while (true) {
            Joueur joueur = joueurs.get(joueurCourantIndex);
            System.out.println("\nManche " + manche + " - Tour de " + joueur.getNom() + ":");
            afficherTable();
            joueur.afficherDominosJoueur();

            boolean aJoue = false;
            List<Domino> main = new ArrayList<>(joueur.getMain());

            while (!aJoue) {
                if (main.isEmpty()) {
                    System.out.println(joueur.getNom() + " n'a plus de dominos et passe son tour.");
                    passesConsecutives++;
                    break;
                }

                System.out.print("Entrez un domino (format x,y ou [x|y]) ou appuyez sur Entrée pour passer : ");
                String input = scanner.nextLine().trim();

                // Passage de tour avec Entrée
                if (input.isEmpty()) {
                    System.out.println(joueur.getNom() + " passe son tour.");
                    passesConsecutives++;
                    break;
                }

                Domino domino;
                try {
                    domino = Domino.parseDomino(input);
                } catch (IllegalArgumentException e) {
                    System.out.println("Erreur : Format invalide. Utilisez [x|y] avec x,y entre 0 et 6.");
                    continue;
                }

                // Vérifier si le domino est dans la main
                if (!joueur.possedeDomino(domino)) {
                    System.out.println("Erreur : Vous ne possédez pas le domino " + domino + ".");
                    continue;
                }

                // Vérifier si le domino est jouable
                if (table.isEmpty()) {
                    // Premier domino : toujours jouable
                    table.addLast(domino);
                    joueur.retirerDomino(domino);
                    System.out.println(joueur.getNom() + " pose " + domino + " comme premier domino.");
                    aJoue = true;
                    passesConsecutives = 0;
                } else {
                    int gaucheTable = table.getFirst().getGauche();
                    int droiteTable = table.getLast().getDroite();

                    // Vérifier correspondance avec continuité
                    boolean canPlayLeft = domino.getDroite() == gaucheTable;
                    boolean canPlayRight = domino.getGauche() == droiteTable;
                    boolean canPlayLeftFlipped = domino.getGauche() == gaucheTable;
                    boolean canPlayRightFlipped = domino.getDroite() == droiteTable;

                    if (canPlayLeft || canPlayRight) {
                        // Poser sans retourner
                        if (canPlayLeft) {
                            table.addFirst(domino);
                            System.out.println(joueur.getNom() + " joue " + domino + " à gauche.");
                        } else {
                            table.addLast(domino);
                            System.out.println(joueur.getNom() + " joue " + domino + " à droite.");
                        }
                        joueur.retirerDomino(domino);
                        aJoue = true;
                        passesConsecutives = 0;
                    } else if (canPlayLeftFlipped || canPlayRightFlipped) {
                        // Retourner le domino
                        domino.retourner();
                        if (canPlayLeftFlipped) {
                            table.addFirst(domino);
                            System.out.println(joueur.getNom() + " joue " + domino + " à gauche (retourné).");
                        } else {
                            table.addLast(domino);
                            System.out.println(joueur.getNom() + " joue " + domino + " à droite (retourné).");
                        }
                        joueur.retirerDomino(domino);
                        aJoue = true;
                        passesConsecutives = 0;
                    } else {
                        System.out.println("Erreur : Le domino " + domino + " ne correspond pas aux extrémités [" + gaucheTable + "|...] ou [...|" + droiteTable + "].");
                    }
                }
            }

            // Vérifier victoire
            if (joueur.getMain().isEmpty()) {
                System.out.println("\n" + joueur.getNom() + " a gagné la manche " + manche + " (plus de dominos) !");
                return joueur;
            }

            // Vérifier blocage
            if (passesConsecutives >= joueurs.size()) {
                System.out.println("\nJeu bloqué dans la manche " + manche + " !");
                return casDeBlocage();
            }

            // Passer au joueur suivant
            joueurCourantIndex = currentTour(joueurCourantIndex + 1) - 1;
        }
    }

    private int calculerPointsManche(Joueur gagnant) {
        if (gagnant == null) {
            return 0;
        }
        int points = 0;
        for (Joueur joueur : joueurs) {
            if (joueur != gagnant) {
                points += joueur.calculerTotalPoints();
            }
        }
        // lister les pioche
        affichePioche(pioche);
        return points;
    }

    private Joueur casDeBlocage() {
        if (joueurs.isEmpty()) {
            System.out.println("Aucun joueur dans la liste");
            return null;
        }

        Joueur gagnant = joueurs.get(0);
        int minimum = gagnant.calculerTotalPoints();

        for (Joueur joueur : joueurs) {
            int points = joueur.calculerTotalPoints();
            System.out.println(joueur.getNom() + ": " + points + " points");
            if (points < minimum) {
                minimum = points;
                gagnant = joueur;
            }
        }

        int nombreGagnants = 0;
        for (Joueur joueur : joueurs) {
            if (joueur.calculerTotalPoints() == minimum) {
                nombreGagnants++;
            }
        }

        // lister les pioche
        affichePioche(pioche);

        if (nombreGagnants > 1) {
            System.out.println("Égalité avec " + minimum + " points - Aucun gagnant dans la manche " + manche);
            return null;
        } else {
            System.out.println("Gagnant de la manche " + manche + ": " + gagnant.getNom() + " avec " + minimum + " points");
            return gagnant;
        }
        
    }

    private void afficherScores() {
        System.out.println("\nScores actuels après la manche " + manche + ":");
        for (Joueur joueur : joueurs) {
            System.out.println(joueur.getNom() + ": " + scores.get(joueur) + " points");
        }
    }

    private void afficherTable() {
        System.out.print("Table: ");
        if (table.isEmpty()) {
            System.out.println("[Vide]");
        } else {
            for (Domino d : table) {
                System.out.print(d + " ");
            }
            System.out.println();
        }
    }

    private void listerDominos(List<Domino> dominos) {
        System.out.println("Pioche initiale:");
        for (int i = 0; i < dominos.size(); i++) {
            System.out.print(dominos.get(i));
            if (i % 7 == 6) System.out.println();
        }
        System.out.println();
    }

    private void affichePioche(List<Domino> dominos) {
        System.out.print("Domino Pioche: ");
        for (Domino domino : dominos) {
            System.out.print(domino + " ");
        }
        System.out.println();
    }

    private int findHighestDouble() {
        for (int valeur = 6; valeur >= 0; valeur--) {
            for (int i = 0; i < joueurs.size(); i++) {
                if (joueurs.get(i).hasDouble(valeur)) {
                    return i;
                }
            }
        }
        return 0;
    }

    public int currentTour(int current) {
        current = (current < 1 || current > 3) ? 3 : current;
        return (current % 3) + 1;
    }
}