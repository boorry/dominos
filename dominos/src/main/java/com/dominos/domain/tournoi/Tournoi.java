package com.dominos.domain.tournoi;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Agrégat racine représentant un tournoi de dominos.
 *
 * Responsabilités :
 *  - Gérer les inscriptions (27 joueurs requis)
 *  - Organiser le bracket (groupes, quarts, demi, finale)
 *  - Suivre l'avancement des matchs
 *  - Déterminer les qualifiés à chaque phase
 *  - Désigner le champion
 *
 * Règles métier :
 *  - Exactement 27 joueurs (9 groupes de 3)
 *  - Un joueur ne peut s'inscrire qu'une fois
 *  - Le tournoi ne peut démarrer qu'avec 27 joueurs inscrits
 *  - Toutes les parties restent à 3 joueurs
 */
public class Tournoi {

    public static final int NB_JOUEURS_REQUIS = 27;
    public static final int JOUEURS_PAR_MATCH = 3;

    private final String          id;
    private final String          nom;
    private       StatutTournoi   statut;
    private       PhaseTournoi    phaseActuelle;
    private final List<String>    joueursInscritsIds;
    private final List<MatchTournoi> matchs;
    private       String          championId;
    private final LocalDateTime   dateCreation;
    private       LocalDateTime   dateDebut;
    private       LocalDateTime   dateFin;

    public Tournoi(String nom) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom du tournoi est requis");
        }
        this.id                 = UUID.randomUUID().toString();
        this.nom                = nom;
        this.statut             = StatutTournoi.INSCRIPTION;
        this.joueursInscritsIds = new ArrayList<>();
        this.matchs             = new ArrayList<>();
        this.dateCreation       = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Inscriptions
    // -------------------------------------------------------------------------

    /**
     * Inscrit un joueur au tournoi.
     *
     * @throws IllegalStateException    si le tournoi n'est plus en inscription
     * @throws IllegalStateException    si le tournoi est complet
     * @throws IllegalArgumentException si le joueur est déjà inscrit
     */
    public void inscrire(String utilisateurId) {
        if (statut != StatutTournoi.INSCRIPTION) {
            throw new IllegalStateException(
                "Les inscriptions sont fermées — statut : " + statut);
        }
        if (joueursInscritsIds.size() >= NB_JOUEURS_REQUIS) {
            throw new IllegalStateException(
                "Le tournoi est complet (" + NB_JOUEURS_REQUIS + " joueurs)");
        }
        if (joueursInscritsIds.contains(utilisateurId)) {
            throw new IllegalArgumentException(
                "Ce joueur est déjà inscrit au tournoi");
        }
        joueursInscritsIds.add(utilisateurId);
    }

    public boolean estComplet() {
        return joueursInscritsIds.size() == NB_JOUEURS_REQUIS;
    }

    // -------------------------------------------------------------------------
    // Démarrage
    // -------------------------------------------------------------------------

    /**
     * Démarre le tournoi et crée les matchs de la phase de groupes.
     *
     * Les joueurs sont mélangés aléatoirement puis répartis
     * en 9 groupes de 3.
     *
     * @throws IllegalStateException si pas 27 joueurs inscrits
     */
    public void demarrer() {
        if (statut != StatutTournoi.INSCRIPTION) {
            throw new IllegalStateException("Le tournoi a déjà démarré");
        }
        if (joueursInscritsIds.size() != NB_JOUEURS_REQUIS) {
            throw new IllegalStateException(
                "Il faut " + NB_JOUEURS_REQUIS + " joueurs pour démarrer "
                + "(actuellement : " + joueursInscritsIds.size() + ")");
        }

        this.statut       = StatutTournoi.EN_COURS;
        this.phaseActuelle = PhaseTournoi.GROUPES;
        this.dateDebut     = LocalDateTime.now();

        creerMatchsPhaseGroupes();
    }

    // -------------------------------------------------------------------------
    // Avancement des matchs
    // -------------------------------------------------------------------------

    /**
     * Enregistre la fin d'un match et son gagnant.
     * Si tous les matchs de la phase sont terminés, passe à la phase suivante.
     *
     * @return true si on passe à une nouvelle phase
     */
    public boolean terminerMatch(String matchId, String gagnantId) {
        MatchTournoi match = trouverMatch(matchId);
        match.terminer(gagnantId);

        if (tousLesMatchsPhaseTermines()) {
            if (phaseActuelle == PhaseTournoi.FINALE) {
                terminerTournoi(gagnantId);
            } else {
                passerPhaseSuivante();
                return true;
            }
        }
        return false;
    }

    /**
     * Lie un match à une partie créée.
     */
    public void demarrerMatch(String matchId, String partieId) {
        trouverMatch(matchId).demarrer(partieId);
    }

    // -------------------------------------------------------------------------
    // Consultation
    // -------------------------------------------------------------------------

    public List<MatchTournoi> getMatchsPhaseActuelle() {
        return matchs.stream()
            .filter(m -> m.getPhase() == phaseActuelle)
            .toList();
    }

    public List<MatchTournoi> getMatchsEnAttente() {
        return matchs.stream()
            .filter(MatchTournoi::estEnAttente)
            .toList();
    }

    public List<String> getGagnantsPhase(PhaseTournoi phase) {
        return matchs.stream()
            .filter(m -> m.getPhase() == phase && m.estTermine())
            .map(MatchTournoi::getGagnantId)
            .filter(Objects::nonNull)
            .toList();
    }

    // -------------------------------------------------------------------------
    // Privé — logique de bracket
    // -------------------------------------------------------------------------

    private void creerMatchsPhaseGroupes() {
        List<String> joueursMelanges = new ArrayList<>(joueursInscritsIds);
        Collections.shuffle(joueursMelanges);

        for (int groupe = 0; groupe < 9; groupe++) {
            int debut = groupe * 3;
            List<String> groupeJoueurs = joueursMelanges.subList(debut, debut + 3);
            matchs.add(new MatchTournoi(
                PhaseTournoi.GROUPES, groupe + 1,
                new ArrayList<>(groupeJoueurs)));
        }
    }

    private void passerPhaseSuivante() {
        List<String> qualifies = getGagnantsPhase(phaseActuelle);
        phaseActuelle = phaseActuelle.suivante();
        creerMatchsPhase(phaseActuelle, qualifies);
    }

    private void creerMatchsPhase(PhaseTournoi phase, List<String> joueurs) {
        // Mélanger les qualifiés pour les répartir aléatoirement
        List<String> joueursMelanges = new ArrayList<>(joueurs);
        Collections.shuffle(joueursMelanges);

        int nbMatchs = phase.nombreDeMatchs();
        for (int i = 0; i < nbMatchs; i++) {
            int debut = i * JOUEURS_PAR_MATCH;
            List<String> groupeJoueurs =
                joueursMelanges.subList(debut, debut + JOUEURS_PAR_MATCH);
            matchs.add(new MatchTournoi(
                phase, i + 1, new ArrayList<>(groupeJoueurs)));
        }
    }

    private void terminerTournoi(String championId) {
        this.championId = championId;
        this.statut     = StatutTournoi.TERMINE;
        this.dateFin    = LocalDateTime.now();
    }

    private boolean tousLesMatchsPhaseTermines() {
        return matchs.stream()
            .filter(m -> m.getPhase() == phaseActuelle)
            .allMatch(MatchTournoi::estTermine);
    }

    private MatchTournoi trouverMatch(String matchId) {
        return matchs.stream()
            .filter(m -> m.getId().equals(matchId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Match introuvable : " + matchId));
    }

    // -------------------------------------------------------------------------
    // Accesseurs
    // -------------------------------------------------------------------------

    public String             getId()                  { return id; }
    public String             getNom()                 { return nom; }
    public StatutTournoi      getStatut()              { return statut; }
    public PhaseTournoi       getPhaseActuelle()       { return phaseActuelle; }
    public List<String>       getJoueursInscritsIds()  { return Collections.unmodifiableList(joueursInscritsIds); }
    public int                getNbJoueursInscrits()   { return joueursInscritsIds.size(); }
    public List<MatchTournoi> getMatchs()              { return Collections.unmodifiableList(matchs); }
    public String             getChampionId()          { return championId; }
    public LocalDateTime      getDateCreation()        { return dateCreation; }
    public LocalDateTime      getDateDebut()           { return dateDebut; }
    public LocalDateTime      getDateFin()             { return dateFin; }

    @Override
    public String toString() {
        return "Tournoi{nom='" + nom + "', statut=" + statut
            + ", joueurs=" + joueursInscritsIds.size() + "/" + NB_JOUEURS_REQUIS + "}";
    }
}
