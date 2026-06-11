package com.dominos.moteur;

import com.dominos.domain.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Moteur de jeu — toutes les règles, zéro I/O.
 *
 * CORRECTION choix de côté :
 *
 * Quand jouerCoup() détecte qu'un domino peut être posé des deux côtés,
 * il retourne CHOIX_REQUIS au lieu de choisir automatiquement.
 * Le joueur doit alors rappeler jouerCoupAvecCote() avec son choix.
 *
 * Nouvelles méthodes :
 *  - jouerCoup(Partie, Domino)               → détecte l'ambiguïté
 *  - jouerCoupAvecCote(Partie, Domino, Cote) → pose avec côté explicite
 */
public class MoteurJeu {

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    public void initialiserManche(Partie partie) {
        partie.getPlateau().vider();
        partie.reinitialiserPasses();
        for (Joueur j : partie.getJoueurs()) {
            partie.getMain(j).vider();
        }

        List<Domino> pioche = creerPioche();
        Collections.shuffle(pioche);

        for (Joueur joueur : partie.getJoueurs()) {
            for (int i = 0; i < Partie.DOMINOS_MAIN; i++) {
                partie.getMain(joueur).ajouter(pioche.remove(0));
            }
        }

        partie.setJoueurCourantIndex(determinerJoueurDepart(partie));
    }

    // -------------------------------------------------------------------------
    // Jouer un coup — avec détection d'ambiguïté
    // -------------------------------------------------------------------------

    /**
     * Tente de jouer un domino.
     *
     * Cas 1 — Domino non possédé ou non jouable :
     *   Retourne EN_COURS avec aJoue=false et un message d'erreur.
     *
     * Cas 2 — Un seul côté possible :
     *   Pose automatiquement. Retourne EN_COURS, VICTOIRE, BLOCAGE ou TERMINEE.
     *
     * Cas 3 — Deux côtés possibles (AMBIGUÏTÉ) :
     *   Retourne CHOIX_REQUIS avec cotesPossibles={GAUCHE, DROITE}.
     *   Le domino N'EST PAS posé — le joueur doit rappeler jouerCoupAvecCote().
     */
    public ResultatCoup jouerCoup(Partie partie, Domino domino) {
        Joueur     joueur   = partie.getJoueurCourant();
        MainJoueur main     = partie.getMain(joueur);
        Plateau    plateau  = partie.getPlateau();

        // Validation
        if (!main.contient(domino)) {
            return new ResultatCoup(joueur, false, EtatPartie.EN_COURS, null,
                joueur.getPseudo() + " ne possède pas le domino " + domino);
        }

        List<Domino> jouables = main.getDominosJouables(
            plateau.getGaucheOuMoinsUn(), plateau.getDroiteOuMoinsUn());

        if (!jouables.contains(domino)) {
            return new ResultatCoup(joueur, false, EtatPartie.EN_COURS, null,
                "Le domino " + domino + " n'est pas jouable sur ce plateau");
        }

        // Premier domino — pas d'ambiguïté
        if (plateau.estVide()) {
            plateau.poserPremier(domino);
            main.retirer(domino);
            partie.reinitialiserPasses();
            return construireResultat(partie, joueur,
                joueur.getPseudo() + " pose " + domino + " comme premier domino.");
        }

        // Vérifier ambiguïté
        Set<Plateau.Cote> cotesPossibles = plateau.getCotesPossibles(domino);

        if (cotesPossibles.size() == 2) {
            // Le joueur doit choisir — on ne pose rien
            return new ResultatCoup(
                joueur, false, EtatPartie.CHOIX_REQUIS, null,
                "Le domino " + domino + " peut être posé des deux côtés. "
                + "Choisissez : GAUCHE (extrémité " + plateau.getGaucheOuMoinsUn()
                + ") ou DROITE (extrémité " + plateau.getDroiteOuMoinsUn() + ")",
                EnumSet.copyOf(cotesPossibles)
            );
        }

        // Un seul côté — pose automatique
        Plateau.ResultatPose pose = plateau.poser(domino);
        main.retirer(domino);
        partie.reinitialiserPasses();
        return construireResultat(partie, joueur,
            joueur.getPseudo() + " pose " + pose + ".");
    }

    /**
     * Joue un domino sur un côté explicitement choisi par le joueur.
     *
     * Appelé après jouerCoup() qui a retourné CHOIX_REQUIS.
     *
     * @param partie  la partie en cours
     * @param domino  le domino à poser (même que celui soumis dans jouerCoup)
     * @param cote    GAUCHE ou DROITE — choix du joueur
     */
    public ResultatCoup jouerCoupAvecCote(Partie partie,
                                           Domino domino,
                                           Plateau.Cote cote) {
        Joueur     joueur  = partie.getJoueurCourant();
        MainJoueur main    = partie.getMain(joueur);
        Plateau    plateau = partie.getPlateau();

        // Re-valider que le domino est toujours dans la main
        if (!main.contient(domino)) {
            return new ResultatCoup(joueur, false, EtatPartie.EN_COURS, null,
                joueur.getPseudo() + " ne possède pas le domino " + domino);
        }

        // Poser au côté choisi
        try {
            Plateau.ResultatPose pose = plateau.poserACote(domino, cote);
            main.retirer(domino);
            partie.reinitialiserPasses();
            return construireResultat(partie, joueur,
                joueur.getPseudo() + " pose " + pose + ".");
        } catch (IllegalArgumentException e) {
            return new ResultatCoup(joueur, false, EtatPartie.EN_COURS, null,
                "Impossible de poser " + domino + " à " + cote + " : " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Passer son tour
    // -------------------------------------------------------------------------

    public ResultatCoup passerTour(Partie partie) {
        Joueur joueur = partie.getJoueurCourant();
        partie.incrementerPasses();

        if (partie.estBloque()) {
            Joueur gagnant = resoudreBlocage(partie);
            String msg     = construireMessageBlocage(partie, gagnant);

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
    // Requêtes
    // -------------------------------------------------------------------------

    public List<Domino> getDominosJouables(Partie partie) {
        Joueur  joueur  = partie.getJoueurCourant();
        Plateau plateau = partie.getPlateau();
        return partie.getMain(joueur).getDominosJouables(
            plateau.getGaucheOuMoinsUn(), plateau.getDroiteOuMoinsUn());
    }

    // -------------------------------------------------------------------------
    // Privé
    // -------------------------------------------------------------------------

    private List<Domino> creerPioche() {
        List<Domino> pioche = new ArrayList<>();
        for (int g = 0; g <= 6; g++) {
            for (int d = g; d <= 6; d++) {
                pioche.add(new Domino(g, d));
            }
        }
        return pioche;
    }

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
        return 0;
    }

    /**
     * Construit le ResultatCoup après une pose réussie.
     * Vérifie victoire et fait avancer le joueur.
     */
    private ResultatCoup construireResultat(Partie partie,
                                             Joueur joueur,
                                             String messagePose) {
        // Victoire ?
        if (partie.getMain(joueur).estVide()) {
            int points = calculerPointsManche(partie, joueur);
            partie.ajouterScore(joueur, points);

            if (partie.aUnGagnant()) {
                return new ResultatCoup(joueur, true, EtatPartie.TERMINEE, joueur,
                    messagePose + "\n" + joueur.getPseudo()
                    + " remporte la partie avec " + partie.getScore(joueur) + " points !");
            }
            partie.incrementerManche();
            return new ResultatCoup(joueur, true, EtatPartie.VICTOIRE, joueur,
                messagePose + "\n" + joueur.getPseudo() + " gagne la manche "
                + (partie.getMancheCourante() - 1)
                + " et marque " + points + " points !");
        }

        partie.passerAuJoueurSuivant();
        return new ResultatCoup(joueur, true, EtatPartie.EN_COURS, null, messagePose);
    }

    private int calculerPointsManche(Partie partie, Joueur gagnant) {
        return partie.getJoueurs().stream()
            .filter(j -> !j.equals(gagnant))
            .mapToInt(j -> partie.getMain(j).calculerScore())
            .sum();
    }

    private Joueur resoudreBlocage(Partie partie) {
        Joueur gagnant  = null;
        int    minimum  = Integer.MAX_VALUE;
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
            sb.append(j.getPseudo()).append("=")
              .append(partie.getMain(j).calculerScore()).append(" ");
        }
        if (gagnant == null) {
            sb.append("→ Égalité, aucun gagnant.");
        } else {
            sb.append("→ ").append(gagnant.getPseudo()).append(" gagne la manche.");
        }
        return sb.toString();
    }
}
