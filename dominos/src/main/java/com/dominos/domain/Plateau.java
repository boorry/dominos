package com.dominos.domain;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Représente la chaîne de dominos posés sur la table.
 *
 * CORRECTION — règle du choix de côté :
 *
 * Quand un domino peut être posé des deux côtés
 * (ex : plateau [1|4][4|2][2|3], domino [1|3]),
 * le joueur DOIT choisir. Le moteur ne décide pas à sa place.
 *
 * Nouvelles méthodes :
 *  - getCotesPossibles(Domino)         → retourne GAUCHE, DROITE ou les deux
 *  - poserACote(Domino, Cote)          → pose explicitement d'un côté
 *  - poser(Domino)                     → pose automatiquement si un seul côté
 *                                        possible, lève exception si ambiguïté
 */
public class Plateau {

    private final Deque<Domino> chaine = new ArrayDeque<>();

    // -------------------------------------------------------------------------
    // Consultation
    // -------------------------------------------------------------------------

    public boolean estVide() { return chaine.isEmpty(); }

    public Optional<Integer> getExtrémiteGauche() {
        return chaine.isEmpty()
            ? Optional.empty()
            : Optional.of(chaine.peekFirst().getGauche());
    }

    public Optional<Integer> getExtremiteDroite() {
        return chaine.isEmpty()
            ? Optional.empty()
            : Optional.of(chaine.peekLast().getDroite());
    }

    public int getGaucheOuMoinsUn() {
        return getExtrémiteGauche().orElse(-1);
    }

    public int getDroiteOuMoinsUn() {
        return getExtremiteDroite().orElse(-1);
    }

    public List<Domino> getDominos() {
        return Collections.unmodifiableList(new ArrayList<>(chaine));
    }

    // -------------------------------------------------------------------------
    // Analyse des côtés possibles — NOUVELLE MÉTHODE CLÉE
    // -------------------------------------------------------------------------

    /**
     * Retourne l'ensemble des côtés sur lesquels ce domino peut être posé.
     *
     * Cas possibles :
     *  - {GAUCHE}         → seulement à gauche
     *  - {DROITE}         → seulement à droite
     *  - {GAUCHE, DROITE} → le joueur doit choisir
     *  - {}               → domino non jouable (ne devrait pas arriver)
     *
     * Exemple :
     *   Plateau [1|4][4|2][2|3], domino [1|3]
     *   gauche du plateau = 1 → [1|3] peut se poser à gauche (droite du domino = 3? non, gauche = 1 = gauche plateau → retourné ou direct)
     *   droite du plateau = 3 → [1|3] peut se poser à droite (gauche du domino = 1? non, droite = 3 = droite plateau → retourné ou direct)
     *
     *   Analyse complète :
     *   - peutJouerAGauche    : droite du domino(3) == gauche plateau(1) ? NON
     *   - peutJouerADroite    : gauche du domino(1) == droite plateau(3) ? NON
     *   - peutJouerAGaucheRetourné : gauche du domino(1) == gauche plateau(1) ? OUI → retourner → [3|1], droite=1=gauche plateau ✓
     *   - peutJouerADroiteRetourné : droite du domino(3) == droite plateau(3) ? OUI → retourner → [3|1], gauche=3=droite plateau ✓
     *
     *   Résultat : {GAUCHE, DROITE} → le joueur choisit
     */
    public Set<Cote> getCotesPossibles(Domino d) {
        if (chaine.isEmpty()) {
            return EnumSet.of(Cote.DROITE); // premier domino → un seul côté (convention)
        }

        Set<Cote> cotes = EnumSet.noneOf(Cote.class);
        int gaucheTable = getGaucheOuMoinsUn();
        int droiteTable = getDroiteOuMoinsUn();

        // Côté gauche : droite du domino == gauche du plateau (direct)
        //            OU gauche du domino == gauche du plateau (retourné)
        if (d.getDroite() == gaucheTable || d.getGauche() == gaucheTable) {
            cotes.add(Cote.GAUCHE);
        }

        // Côté droit : gauche du domino == droite du plateau (direct)
        //           OU droite du domino == droite du plateau (retourné)
        if (d.getGauche() == droiteTable || d.getDroite() == droiteTable) {
            cotes.add(Cote.DROITE);
        }

        return cotes;
    }

    /**
     * Vrai si le joueur doit choisir le côté de pose.
     * (le domino peut être posé des deux côtés)
     */
    public boolean choixRequis(Domino d) {
        Set<Cote> cotes = getCotesPossibles(d);
        return cotes.contains(Cote.GAUCHE) && cotes.contains(Cote.DROITE);
    }

    // -------------------------------------------------------------------------
    // Placement
    // -------------------------------------------------------------------------

    /**
     * Pose le premier domino sur un plateau vide.
     */
    public void poserPremier(Domino d) {
        if (!chaine.isEmpty()) {
            throw new IllegalStateException(
                "Le plateau n'est pas vide — utilisez poserACote()");
        }
        chaine.addLast(d);
    }

    /**
     * Pose un domino sur le côté explicitement choisi par le joueur.
     *
     * C'est la méthode principale pour poser un domino.
     * Elle oriente (retourne) le domino si nécessaire.
     *
     * @throws IllegalArgumentException si le domino ne peut pas être posé de ce côté
     * @throws IllegalStateException    si le plateau est vide
     */
    public ResultatPose poserACote(Domino d, Cote cote) {
        if (chaine.isEmpty()) {
            throw new IllegalStateException(
                "Plateau vide — utilisez poserPremier()");
        }

        Set<Cote> cotesPossibles = getCotesPossibles(d);
        if (!cotesPossibles.contains(cote)) {
            throw new IllegalArgumentException(
                "Le domino " + d + " ne peut pas être posé à "
                + cote + " sur ce plateau");
        }

        return switch (cote) {
            case GAUCHE -> poserAGauche(d);
            case DROITE -> poserADroite(d);
        };
    }

    /**
     * Pose automatiquement si un seul côté est possible.
     * Lève une exception si les deux côtés sont possibles
     * (le joueur doit choisir via poserACote).
     *
     * Conservé pour compatibilité avec le code existant.
     *
     * @throws AmbiguiteCoException si deux côtés sont possibles
     * @throws IllegalArgumentException si domino non jouable
     */
    public ResultatPose poser(Domino d) {
        if (chaine.isEmpty()) {
            throw new IllegalStateException("Plateau vide — utilisez poserPremier()");
        }

        Set<Cote> cotes = getCotesPossibles(d);

        if (cotes.isEmpty()) {
            throw new IllegalArgumentException(
                "Le domino " + d + " n'est pas jouable sur ce plateau");
        }

        if (cotes.size() == 2) {
            throw new AmbiguiteCoException(d,
                getGaucheOuMoinsUn(), getDroiteOuMoinsUn());
        }

        Cote cote = cotes.iterator().next();
        return poserACote(d, cote);
    }

    public void vider() { chaine.clear(); }

    // -------------------------------------------------------------------------
    // Privé — placement avec orientation automatique
    // -------------------------------------------------------------------------

    private ResultatPose poserAGauche(Domino d) {
        int gaucheTable = getGaucheOuMoinsUn();

        // Direct : droite du domino == gauche du plateau
        if (d.getDroite() == gaucheTable) {
            chaine.addFirst(d);
            return new ResultatPose(d, Cote.GAUCHE, false);
        }
        // Retourné : gauche du domino == gauche du plateau → retourner → droite == gauche table
        Domino dRetourne = d.retourner();
        chaine.addFirst(dRetourne);
        return new ResultatPose(dRetourne, Cote.GAUCHE, true);
    }

    private ResultatPose poserADroite(Domino d) {
        int droiteTable = getDroiteOuMoinsUn();

        // Direct : gauche du domino == droite du plateau
        if (d.getGauche() == droiteTable) {
            chaine.addLast(d);
            return new ResultatPose(d, Cote.DROITE, false);
        }
        // Retourné : droite du domino == droite du plateau → retourner → gauche == droite table
        Domino dRetourne = d.retourner();
        chaine.addLast(dRetourne);
        return new ResultatPose(dRetourne, Cote.DROITE, true);
    }

    // -------------------------------------------------------------------------
    // Types internes
    // -------------------------------------------------------------------------

    public enum Cote { GAUCHE, DROITE }

    public record ResultatPose(Domino domino, Cote cote, boolean retourné) {
        @Override
        public String toString() {
            return domino + " posé à " + cote + (retourné ? " (retourné)" : "");
        }
    }

    /**
     * Exception levée quand un domino peut être posé des deux côtés.
     * Le joueur doit choisir explicitement via poserACote().
     */
    public static class AmbiguiteCoException extends RuntimeException {
        private final Domino domino;
        private final int    gaucheTable;
        private final int    droiteTable;

        public AmbiguiteCoException(Domino domino, int gaucheTable, int droiteTable) {
            super("Le domino " + domino + " peut être posé des deux côtés "
                + "(gauche=" + gaucheTable + ", droite=" + droiteTable
                + ") — choisissez explicitement le côté");
            this.domino      = domino;
            this.gaucheTable = gaucheTable;
            this.droiteTable = droiteTable;
        }

        public Domino getDomino()      { return domino; }
        public int    getGaucheTable() { return gaucheTable; }
        public int    getDroiteTable() { return droiteTable; }
    }
}
