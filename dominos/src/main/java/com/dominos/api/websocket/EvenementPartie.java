package com.dominos.api.websocket;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Message WebSocket envoyé aux clients lors d'un événement de partie.
 *
 * Ce DTO est sérialisé en JSON par Spring et envoyé sur le topic :
 *   /topic/parties/{partieId}
 *
 * Exemple JSON reçu par le client :
 * {
 *   "type": "COUP_JOUE",
 *   "partieId": "uuid-partie",
 *   "joueurActif": "alice_42",
 *   "message": "alice_42 pose [3|5] à droite.",
 *   "plateau": ["[6|6]", "[6|3]", "[3|5]"],
 *   "joueurSuivant": "bob_99",
 *   "scores": {"alice_42": 0, "bob_99": 0, "carl_7": 0},
 *   "gagnant": null,
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 *
 * Tous les champs sont optionnels selon le type d'événement.
 * Le client ignore les champs null.
 */
public class EvenementPartie {

    private TypeEvenement       type;
    private String              partieId;
    private String              joueurActif;
    private String              message;
    private List<String>        plateau;
    private String              joueurSuivant;
    private Map<String, Integer> scores;
    private String              gagnant;
    private int                 manche;
    private List<String>        joueurs;
    private String              timestamp;

    // -------------------------------------------------------------------------
    // Constructeur privé — utiliser les factories
    // -------------------------------------------------------------------------

    private EvenementPartie() {
        this.timestamp = LocalDateTime.now().toString();
    }

    // -------------------------------------------------------------------------
    // Factories — un par type d'événement
    // -------------------------------------------------------------------------

    /**
     * PARTIE_CREEE — envoyé quand le matchmaking réunit 3 joueurs.
     */
    public static EvenementPartie partieCreee(String partieId,
                                               List<String> joueurs) {
        EvenementPartie e = new EvenementPartie();
        e.type     = TypeEvenement.PARTIE_CREEE;
        e.partieId = partieId;
        e.joueurs  = joueurs;
        e.message  = "La partie commence ! Bonne chance à tous.";
        return e;
    }

    /**
     * COUP_JOUE — envoyé après qu'un joueur a posé un domino.
     */
    public static EvenementPartie coupJoue(String partieId,
                                            String joueurActif,
                                            String message,
                                            List<String> plateau,
                                            String joueurSuivant,
                                            Map<String, Integer> scores,
                                            int manche) {
        EvenementPartie e = new EvenementPartie();
        e.type          = TypeEvenement.COUP_JOUE;
        e.partieId      = partieId;
        e.joueurActif   = joueurActif;
        e.message       = message;
        e.plateau       = plateau;
        e.joueurSuivant = joueurSuivant;
        e.scores        = scores;
        e.manche        = manche;
        return e;
    }

    /**
     * JOUEUR_PASSE — envoyé quand un joueur passe son tour.
     */
    public static EvenementPartie joueurPasse(String partieId,
                                               String joueurActif,
                                               String joueurSuivant,
                                               int manche) {
        EvenementPartie e = new EvenementPartie();
        e.type          = TypeEvenement.JOUEUR_PASSE;
        e.partieId      = partieId;
        e.joueurActif   = joueurActif;
        e.message       = joueurActif + " passe son tour.";
        e.joueurSuivant = joueurSuivant;
        e.manche        = manche;
        return e;
    }

    /**
     * MANCHE_TERMINEE — envoyé à la fin d'une manche.
     */
    public static EvenementPartie mancheTerminee(String partieId,
                                                  String gagnant,
                                                  Map<String, Integer> scores,
                                                  int manche,
                                                  String message) {
        EvenementPartie e = new EvenementPartie();
        e.type    = TypeEvenement.MANCHE_TERMINEE;
        e.partieId = partieId;
        e.gagnant = gagnant;
        e.scores  = scores;
        e.manche  = manche;
        e.message = message;
        return e;
    }

    /**
     * PARTIE_TERMINEE — envoyé quand un joueur atteint 120 points.
     */
    public static EvenementPartie partieTerminee(String partieId,
                                                  String gagnant,
                                                  Map<String, Integer> scores,
                                                  String message) {
        EvenementPartie e = new EvenementPartie();
        e.type    = TypeEvenement.PARTIE_TERMINEE;
        e.partieId = partieId;
        e.gagnant = gagnant;
        e.scores  = scores;
        e.message = message;
        return e;
    }

    /**
     * ERREUR — envoyé si un coup invalide est tenté.
     */
    public static EvenementPartie erreur(String partieId, String message) {
        EvenementPartie e = new EvenementPartie();
        e.type    = TypeEvenement.ERREUR;
        e.partieId = partieId;
        e.message = message;
        return e;
    }

    // -------------------------------------------------------------------------
    // Getters (requis par Jackson pour la sérialisation JSON)
    // -------------------------------------------------------------------------

    public TypeEvenement        getType()          { return type; }
    public String               getPartieId()      { return partieId; }
    public String               getJoueurActif()   { return joueurActif; }
    public String               getMessage()       { return message; }
    public List<String>         getPlateau()       { return plateau; }
    public String               getJoueurSuivant() { return joueurSuivant; }
    public Map<String, Integer> getScores()        { return scores; }
    public String               getGagnant()       { return gagnant; }
    public int                  getManche()        { return manche; }
    public List<String>         getJoueurs()       { return joueurs; }
    public String               getTimestamp()     { return timestamp; }
}
