package com.dominos.moteur;

import com.dominos.domain.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Moteur de jeu — contient toutes les règles, sans aucune I/O.
 *
 * Le moteur opère sur un objet Partie et retourne des ResultatCoup.
 * Il ne connaît ni Scanner, ni System.out, ni WebSocket, ni HTTP.
 * C'est la couche appelante (Console, WebSocket handler, REST controller)
 * qui décide comment présenter les résultats.
 *
 * Méthodes publiques :
 *  - initialiserManche(Partie)   : distribue les dominos, détermine qui commence
 *  - jouerCoup(Partie, Domino)   : le joueur courant pose un domino
 *  - passerTour(Partie)          : le joueur courant passe son tour
 *
 * Toutes ces méthodes font avancer la Partie et retournent un ResultatCoup
 * décrivant ce qui s'est passé et le nouvel état.
 */
public class MoteurJeu {

    // -------------------------------------------------------------------------
    // Initialisation d'une manche
    // -------------------------------------------------------------------------

    /**
     * Prépare une nouvelle manche :
     *  1. Vide les mains et le plateau
     *  2. Crée et mélange la pioche
     *  3. Distribue 7 dominos à chaque joueur
     *  4. Détermine le joueur qui commence
     */
    public void initialiserManche(Partie partie) {
        // Réinitialiser l'état de la manche
        partie.getPlateau().vider();
        partie.reinitialiserPasses();
        for (Joueur j : partie.getJoueurs()) {
            partie.getMain(j).vider();
        }

        // Créer et mélanger la pioche
        List<Domino> pioche = creerPioche();
        Collections.shuffle(pioche);

        // Distribuer
        for (Joueur joueur : partie.getJoueurs()) {
            for (int i = 0; i < Partie.DOMINOS_MAIN; i++) {
                partie.getMain(joueur).ajouter(pioche.remove(0));
            }
        }

        // Déterminer le joueur qui commence
        int index = determinerJoueurDepart(partie);
        partie.setJoueurCourantIndex(index);
    }

    // -------------------------------------------------------------------------
    // Actions de jeu
    // -------------------------------------------------------------------------

    /**
     * Le joueur courant pose le domino donné.
     *
     * Contrôles effectués :
     *  - Le joueur possède ce domino
     *  - Le domino est dans la liste des jouables
     *
     * Après le coup :
     *  - Vérifie victoire (main vide)
     *  - Passe au joueur suivant
     */
    public ResultatCoup jouerCoup(Partie partie, Domino domino) {
        Joueur joueur  = partie.getJoueurCourant();
        MainJoueur main = partie.getMain(joueur);
        Plateau   plateau = partie.getPlateau();

        // Validation
        if (!main.contient(domino)) {
            return new ResultatCoup(joueur, false, EtatPartie.EN_COURS, null,
                joueur.getPseudo() + " ne possède pas le domino " + domino);
        }

        List<Domino> jouables = main.getDominosJouables(
            plateau.getGaucheOuMoinsUn(),
            plateau.getDroiteOuMoinsUn()
        );
        if (!jouables.contains(domino)) {
            return new ResultatCoup(joueur, false, EtatPartie.EN_COURS, null,
                "Le domino " + domino + " n'est pas jouable sur ce plateau");
        }

        // Poser le domino
        String messagePose = poserSurPlateau(plateau, domino, joueur.getPseudo());
        main.retirer(domino);
        partie.reinitialiserPasses();

        // Victoire ?
        if (main.estVide()) {
            int points = calculerPointsManche(partie, joueur);
            partie.ajouterScore(joueur, points);

            if (partie.aUnGagnant()) {
                return new ResultatCoup(joueur, true, EtatPartie.TERMINEE, joueur,
                    messagePose + "\n" + joueur.getPseudo() + " remporte la partie avec "
                    + partie.getScore(joueur) + " points !");
            }

            partie.incrementerManche();
            return new ResultatCoup(joueur, true, EtatPartie.VICTOIRE, joueur,
                messagePose + "\n" + joueur.getPseudo() + " gagne la manche "
                + (partie.getMancheCourante() - 1) + " et marque " + points + " points !");
        }

        partie.passerAuJoueurSuivant();
        return new ResultatCoup(joueur, true, EtatPartie.EN_COURS, null, messagePose);
    }

    /**
     * Le joueur courant passe son tour (aucun domino jouable).
     *
     * Après le passage :
     *  - Vérifie le blocage (toutes les passes consécutives)
     *  - Passe au joueur suivant si pas de blocage
     */
    public ResultatCoup passerTour(Partie partie) {
        Joueur joueur = partie.getJoueurCourant();
        partie.incrementerPasses();

        if (partie.estBloque()) {
            Joueur gagnant = resoudreBlocage(partie);
            String msg = construireMessageBlocage(partie, gagnant);

            if (gagnant != null) {
                int points = calculerPointsManche(partie, gagnant);
                partie.ajouterScore(gagnant, points);
                msg += "\n" + gagnant.getPseudo() + " marque " + points + " points.";

                if (partie.aUnGagnant()) {
                    return new ResultatCoup(joueur, false, EtatPartie.TERMINEE, gagnant, msg);
                }
            }

            partie.incrementerManche();
            return new ResultatCoup(joueur, false, EtatPartie.BLOCAGE, gagnant, msg);
        }

        partie.passerAuJoueurSuivant();
        return new ResultatCoup(joueur, false, EtatPartie.EN_COURS, null,
            joueur.getPseudo() + " passe son tour.");
    }

    // -------------------------------------------------------------------------
    // Requêtes utilitaires (sans effet de bord)
    // -------------------------------------------------------------------------

    /**
     * Retourne les dominos jouables pour le joueur courant.
     * Utilisé par la couche présentation pour afficher les choix.
     */
    public List<Domino> getDominosJouables(Partie partie) {
        Joueur joueur = partie.getJoueurCourant();
        Plateau plateau = partie.getPlateau();
        return partie.getMain(joueur).getDominosJouables(
            plateau.getGaucheOuMoinsUn(),
            plateau.getDroiteOuMoinsUn()
        );
    }

    // -------------------------------------------------------------------------
    // Privé — logique interne
    // -------------------------------------------------------------------------

    private List<Domino> creerPioche() {
        List<Domino> pioche = new ArrayList<>();
        for (int g = 0; g <= 6; g++) {
            for (int d = g; d <= 6; d++) {
                pioche.add(new Domino(g, d));
            }
        }
        return pioche; // 28 dominos
    }

    /**
     * Manche 1 : le joueur avec le double le plus élevé commence.
     * Manches suivantes : rotation (index = manche % nbJoueurs).
     */
    private int determinerJoueurDepart(Partie partie) {
        if (partie.getMancheCourante() == 1) {
            return indexJoueurAvecPlusHautDouble(partie);
        }
        return (partie.getMancheCourante() - 1) % Partie.NB_JOUEURS;
    }

    private int indexJoueurAvecPlusHautDouble(Partie partie) {
        List<Joueur> joueurs = partie.getJoueurs();
        for (int valeur = 6; valeur >= 0; valeur--) {
            for (int i = 0; i < joueurs.size(); i++) {
                if (partie.getMain(joueurs.get(i)).possedeDouble(valeur)) {
                    return i;
                }
            }
        }
        return 0; // fallback improbable
    }

    private String poserSurPlateau(Plateau plateau, Domino domino, String pseudoJoueur) {
        if (plateau.estVide()) {
            plateau.poserPremier(domino);
            return pseudoJoueur + " pose " + domino + " comme premier domino.";
        }
        Plateau.ResultatPose resultat = plateau.poser(domino);
        return pseudoJoueur + " pose " + resultat.toString() + ".";
    }

    private int calculerPointsManche(Partie partie, Joueur gagnant) {
        return partie.getJoueurs().stream()
            .filter(j -> !j.equals(gagnant))
            .mapToInt(j -> partie.getMain(j).calculerScore())
            .sum();
    }

    /**
     * En cas de blocage : le joueur avec le moins de points dans la main gagne.
     * En cas d'égalité : retourne null.
     */
    private Joueur resoudreBlocage(Partie partie) {
        Joueur gagnant = null;
        int minimum = Integer.MAX_VALUE;
        boolean egalite = false;

        for (Joueur j : partie.getJoueurs()) {
            int score = partie.getMain(j).calculerScore();
            if (score < minimum) {
                minimum = score;
                gagnant = j;
                egalite = false;
            } else if (score == minimum) {
                egalite = true;
            }
        }
        return egalite ? null : gagnant;
    }

    private String construireMessageBlocage(Partie partie, Joueur gagnant) {
        StringBuilder sb = new StringBuilder("Jeu bloqué ! Scores en main : ");
        for (Joueur j : partie.getJoueurs()) {
            sb.append(j.getPseudo())
              .append("=")
              .append(partie.getMain(j).calculerScore())
              .append(" ");
        }
        if (gagnant == null) {
            sb.append("→ Égalité, aucun gagnant pour cette manche.");
        } else {
            sb.append("→ ").append(gagnant.getPseudo()).append(" gagne la manche.");
        }
        return sb.toString();
    }
}
