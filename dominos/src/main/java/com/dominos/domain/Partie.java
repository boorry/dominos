package com.dominos.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contient l'état complet d'une partie de dominos.
 *
 * Responsabilités :
 *  - Associer chaque Joueur à sa MainJoueur
 *  - Maintenir les scores cumulés par joueur (sur plusieurs manches)
 *  - Conserver le numéro de manche courante
 *  - Conserver l'index du joueur courant
 *  - Donner accès au Plateau
 *
 * Partie est un agrégat DDD : elle est la racine qui coordonne
 * Joueur, MainJoueur et Plateau pour une session de jeu.
 * Le MoteurJeu opère sur une Partie.
 */
public class Partie {

    public static final int NB_JOUEURS   = 3;
    public static final int DOMINOS_MAIN = 7;
    public static final int SCORE_MAX    = 120;

    private final List<Joueur> joueurs;
    private final Map<Joueur, MainJoueur> mains;
    private final Map<Joueur, Integer>    scores;
    private final Plateau                 plateau;

    private int  mancheCourante       = 1;
    private int  joueurCourantIndex   = 0;
    private int  passesConsecutives   = 0;

    public Partie(List<Joueur> joueurs) {
        if (joueurs == null || joueurs.size() != NB_JOUEURS) {
            throw new IllegalArgumentException("Une partie requiert exactement " + NB_JOUEURS + " joueurs");
        }
        this.joueurs  = new ArrayList<>(joueurs);
        this.mains    = new LinkedHashMap<>();
        this.scores   = new LinkedHashMap<>();
        this.plateau  = new Plateau();

        for (Joueur j : joueurs) {
            mains.put(j, new MainJoueur());
            scores.put(j, 0);
        }
    }

    // -------------------------------------------------------------------------
    // Joueurs
    // -------------------------------------------------------------------------

    public List<Joueur> getJoueurs() {
        return Collections.unmodifiableList(joueurs);
    }

    public Joueur getJoueurCourant() {
        return joueurs.get(joueurCourantIndex);
    }

    public int getJoueurCourantIndex() {
        return joueurCourantIndex;
    }

    public void setJoueurCourantIndex(int index) {
        if (index < 0 || index >= joueurs.size()) {
            throw new IllegalArgumentException("Index joueur invalide : " + index);
        }
        this.joueurCourantIndex = index;
    }

    public void passerAuJoueurSuivant() {
        joueurCourantIndex = (joueurCourantIndex + 1) % joueurs.size();
    }

    // -------------------------------------------------------------------------
    // Mains
    // -------------------------------------------------------------------------

    public MainJoueur getMain(Joueur joueur) {
        return mains.get(joueur);
    }

    // -------------------------------------------------------------------------
    // Plateau
    // -------------------------------------------------------------------------

    public Plateau getPlateau() {
        return plateau;
    }

    // -------------------------------------------------------------------------
    // Scores
    // -------------------------------------------------------------------------

    public int getScore(Joueur joueur) {
        return scores.getOrDefault(joueur, 0);
    }

    public void ajouterScore(Joueur joueur, int points) {
        scores.put(joueur, scores.get(joueur) + points);
    }

    public Map<Joueur, Integer> getScores() {
        return Collections.unmodifiableMap(scores);
    }

    public boolean aUnGagnant() {
        return scores.values().stream().anyMatch(s -> s >= SCORE_MAX);
    }

    public Joueur getGagnantPartie() {
        return joueurs.stream()
            .filter(j -> scores.get(j) >= SCORE_MAX)
            .findFirst()
            .orElse(null);
    }

    // -------------------------------------------------------------------------
    // Manche
    // -------------------------------------------------------------------------

    public int getMancheCourante() {
        return mancheCourante;
    }

    public void incrementerManche() {
        mancheCourante++;
    }

    // -------------------------------------------------------------------------
    // Passes consécutives (détection blocage)
    // -------------------------------------------------------------------------

    public int getPassesConsecutives() {
        return passesConsecutives;
    }

    public void incrementerPasses() {
        passesConsecutives++;
    }

    public void reinitialiserPasses() {
        passesConsecutives = 0;
    }

    public boolean estBloque() {
        return passesConsecutives >= joueurs.size();
    }
}
