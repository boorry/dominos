package com.dominos.api.dto.partie;

import com.dominos.domain.Domino;
import com.dominos.domain.Joueur;
import com.dominos.domain.Partie;
import com.dominos.moteur.EtatPartie;
import com.dominos.moteur.ResultatCoup;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DTOs liés aux parties.
 */
public final class PartieDtos {

    private PartieDtos() {}

    // -------------------------------------------------------------------------
    // Requêtes
    // -------------------------------------------------------------------------

    /**
     * POST /api/parties/{id}/jouer
     *
     * Exemple JSON reçu :
     * {
     *   "utilisateurId": "uuid-alice",
     *   "domino": "[3|5]"
     * }
     */
    public record JouerCoupRequete(
        String utilisateurId,
        String domino
    ) {
        public void valider() {
            if (utilisateurId == null || utilisateurId.isBlank())
                throw new IllegalArgumentException("L'identifiant utilisateur est requis");
            if (domino == null || domino.isBlank())
                throw new IllegalArgumentException("Le domino est requis");
        }
    }

    /**
     * POST /api/parties/{id}/passer
     *
     * Exemple JSON reçu :
     * {
     *   "utilisateurId": "uuid-alice"
     * }
     */
    public record PasserTourRequete(String utilisateurId) {
        public void valider() {
            if (utilisateurId == null || utilisateurId.isBlank())
                throw new IllegalArgumentException("L'identifiant utilisateur est requis");
        }
    }

    // -------------------------------------------------------------------------
    // Réponses
    // -------------------------------------------------------------------------

    /**
     * GET /api/parties/{id}
     *
     * Exemple JSON retourné :
     * {
     *   "id": "uuid-partie",
     *   "etat": "EN_COURS",
     *   "manche": 1,
     *   "plateau": ["[3|5]", "[5|2]"],
     *   "joueurCourant": "Alice",
     *   "scores": { "Alice": 0, "Bob": 0, "Carl": 0 },
     *   "mainJoueurCourant": ["[1|2]", "[3|4]"]
     * }
     */
    public record PartieReponse(
        String              id,
        EtatPartie          etat,
        int                 manche,
        List<String>        plateau,
        String              joueurCourant,
        Map<String, Integer> scores,
        List<String>        mainJoueurCourant,
        List<String>        dominosJouables
    ) {
        public static PartieReponse depuis(String id, Partie partie, List<Domino> jouables) {
            List<String> plateauStr = partie.getPlateau().getDominos()
                .stream().map(Domino::toString).toList();

            Map<String, Integer> scoresStr = partie.getScores().entrySet().stream()
                .collect(Collectors.toMap(
                    e -> e.getKey().getPseudo(),
                    Map.Entry::getValue
                ));

            Joueur courant = partie.getJoueurCourant();
            List<String> main = partie.getMain(courant).getDominos()
                .stream().map(Domino::toString).toList();

            List<String> jouablesStr = jouables.stream()
                .map(Domino::toString).toList();

            return new PartieReponse(
                id,
                EtatPartie.EN_COURS,
                partie.getMancheCourante(),
                plateauStr,
                courant.getPseudo(),
                scoresStr,
                main,
                jouablesStr
            );
        }
    }

    /**
     * Réponse après jouerCoup() ou passerTour()
     *
     * Exemple JSON retourné :
     * {
     *   "aJoue": true,
     *   "etat": "EN_COURS",
     *   "message": "Alice pose [3|5] à droite.",
     *   "gagnant": null
     * }
     */
    public record CoupReponse(
        boolean    aJoue,
        EtatPartie etat,
        String     message,
        String     gagnant
    ) {
        public static CoupReponse depuis(ResultatCoup resultat) {
            return new CoupReponse(
                resultat.aJoue(),
                resultat.etat(),
                resultat.message(),
                resultat.gagnant() != null ? resultat.gagnant().getPseudo() : null
            );
        }
    }
}
