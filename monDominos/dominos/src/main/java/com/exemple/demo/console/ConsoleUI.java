package com.dominos.console;

import com.dominos.domain.*;
import com.dominos.moteur.EtatPartie;
import com.dominos.moteur.MoteurJeu;
import com.dominos.moteur.ResultatCoup;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Couche présentation console.
 *
 * Responsabilités :
 *  - Lire les entrées utilisateur (Scanner)
 *  - Afficher l'état du jeu (System.out)
 *  - Appeler le MoteurJeu et afficher les ResultatCoup
 *
 * Le moteur ne sait pas qu'il tourne en console.
 * Demain, cette classe sera remplacée par un WebSocket handler
 * ou un REST controller — le moteur ne changera pas.
 */
public class ConsoleUI {

    private final MoteurJeu moteur   = new MoteurJeu();
    private final Scanner   scanner  = new Scanner(System.in);

    // -------------------------------------------------------------------------
    // Point d'entrée
    // -------------------------------------------------------------------------

    public void demarrer() {
        afficherBanniere("DEBUT DE LA PARTIE DE DOMINOS");

        Partie partie = creerPartie();
        jouerPartie(partie);

        afficherBanniere("FIN DE LA PARTIE");
        scanner.close();
    }

    // -------------------------------------------------------------------------
    // Création de la partie
    // -------------------------------------------------------------------------

    private Partie creerPartie() {
        List<Joueur> joueurs = new ArrayList<>();
        for (int i = 1; i <= Partie.NB_JOUEURS; i++) {
            System.out.print("Nom du joueur N°" + i + " : ");
            String nom = scanner.nextLine().trim();
            if (nom.isEmpty()) nom = "Joueur_" + i;
            joueurs.add(new Joueur(nom));
        }
        return new Partie(joueurs);
    }

    // -------------------------------------------------------------------------
    // Boucle de jeu
    // -------------------------------------------------------------------------

    private void jouerPartie(Partie partie) {
        while (true) {
            afficherSeparateur("Manche " + partie.getMancheCourante());
            moteur.initialiserManche(partie);
            afficherMainsJoueurs(partie);

            EtatPartie etatFin = jouerManche(partie);

            afficherScores(partie);

            if (etatFin == EtatPartie.TERMINEE) {
                Joueur gagnant = partie.getGagnantPartie();
                System.out.println("\n🏆 " + gagnant.getPseudo()
                    + " remporte la partie avec " + partie.getScore(gagnant) + " points !");
                break;
            }

            // Préparer la manche suivante
            partie.getPlateau().vider();
        }
    }

    /**
     * Joue une manche complète jusqu'à victoire ou blocage.
     * Retourne VICTOIRE, BLOCAGE, ou TERMINEE.
     */
    private EtatPartie jouerManche(Partie partie) {
        while (true) {
            Joueur joueur = partie.getJoueurCourant();
            afficherSeparateur("Tour de " + joueur.getPseudo());
            afficherPlateau(partie);
            afficherMainJoueur(partie, joueur);

            List<Domino> jouables = moteur.getDominosJouables(partie);
            ResultatCoup resultat;

            if (jouables.isEmpty()) {
                System.out.println(joueur.getPseudo() + " n'a aucun domino jouable → passe.");
                resultat = moteur.passerTour(partie);
            } else {
                resultat = demanderEtJouer(partie, joueur, jouables);
            }

            System.out.println("→ " + resultat.message());

            if (resultat.estTermine()) {
                return resultat.etat();
            }
        }
    }

    /**
     * Demande au joueur de choisir un domino et joue le coup.
     * Recommence si le choix est invalide.
     */
    private ResultatCoup demanderEtJouer(Partie partie, Joueur joueur, List<Domino> jouables) {
        while (true) {
            if (jouables.size() == 1) {
                System.out.println("Un seul domino jouable : " + jouables.get(0) + " → posé automatiquement.");
                return moteur.jouerCoup(partie, jouables.get(0));
            }

            afficherDominosJouables(jouables);
            System.out.print("Votre choix (format [x|y] ou x,y) : ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Entrée vide — réessayez.");
                continue;
            }

            Domino choix;
            try {
                choix = DominoParser.parse(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Format invalide : " + e.getMessage());
                continue;
            }

            ResultatCoup resultat = moteur.jouerCoup(partie, choix);
            if (!resultat.aJoue()) {
                System.out.println("Coup invalide : " + resultat.message());
                continue;
            }

            return resultat;
        }
    }

    // -------------------------------------------------------------------------
    // Affichage
    // -------------------------------------------------------------------------

    private void afficherMainsJoueurs(Partie partie) {
        System.out.println("\nMains distribuées :");
        for (Joueur j : partie.getJoueurs()) {
            afficherMainJoueur(partie, j);
        }
    }

    private void afficherMainJoueur(Partie partie, Joueur joueur) {
        System.out.print("  " + joueur.getPseudo() + " : ");
        partie.getMain(joueur).getDominos().forEach(d -> System.out.print(d + " "));
        System.out.println();
    }

    private void afficherPlateau(Partie partie) {
        Plateau plateau = partie.getPlateau();
        System.out.print("Plateau : ");
        if (plateau.estVide()) {
            System.out.println("[vide]");
        } else {
            plateau.getDominos().forEach(d -> System.out.print(d + " "));
            System.out.println();
        }
    }

    private void afficherDominosJouables(List<Domino> jouables) {
        System.out.print("Dominos jouables : ");
        jouables.forEach(d -> System.out.print(d + " "));
        System.out.println();
    }

    private void afficherScores(Partie partie) {
        System.out.println("\nScores après la manche " + partie.getMancheCourante() + " :");
        partie.getScores().forEach((j, s) ->
            System.out.println("  " + j.getPseudo() + " : " + s + " pts"));
    }

    private void afficherSeparateur(String titre) {
        System.out.println("\n=== " + titre + " ===");
    }

    private void afficherBanniere(String titre) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  " + titre);
        System.out.println("=".repeat(60));
    }
}
