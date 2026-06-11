package com.dominos.api.dto.partie;

import com.dominos.domain.Domino;
import com.dominos.domain.Joueur;
import com.dominos.domain.Partie;
import com.dominos.domain.Plateau;
import com.dominos.moteur.EtatPartie;
import com.dominos.moteur.ResultatCoup;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTOs liés aux parties.
 *
 * Mis à jour :
 *  - JouerAvecCoteRequete : nouvelle requête pour confirmer le côté
 *  - CoupReponse          : nouveau champ cotesPossibles
 */
public final class PartieDtos {

    private PartieDtos() {}

    // -------------------------------------------------------------------------
    // Requêtes
    // -------------------------------------------------------------------------

    public record JouerCoupRequete(String utilisateurId, String domino) {
        public void valider() {
            if (utilisateurId == null || utilisateurId.isBlank())
                throw new IllegalArgumentException("L'identifiant utilisateur est requis");
            if (domino == null || domino.isBlank())
                throw new IllegalArgumentException("Le domino est requis");
        }
    }

    /**
     * Nouvelle requête — confirmer le côté après CHOIX_REQUIS.
     *
     * {
     *   "utilisateurId": "uuid-alice",
     *   "domino": "[1|3]",
     *   "cote": "GAUCHE"
     * }
     */
    public record JouerAvecCoteRequete(
        String utilisateurId,
        String domino,
        String cote
    ) {
        public void valider() {
            if (utilisateurId == null || utilisateurId.isBlank())
                throw new IllegalArgumentException("L'identifiant utilisateur est requis");
            if (domino == null || domino.isBlank())
                throw new IllegalArgumentException("Le domino est requis");
            if (cote == null || cote.isBlank())
                throw new IllegalArgumentException(
                    "Le côté est requis (GAUCHE ou DROITE)");
            if (!cote.equalsIgnoreCase("GAUCHE") && !cote.equalsIgnoreCase("DROITE"))
                throw new IllegalArgumentException(
                    "Côté invalide : '" + cote + "' — attendu GAUCHE ou DROITE");
        }
    }

    public record PasserTourRequete(String utilisateurId) {
        public void valider() {
            if (utilisateurId == null || utilisateurId.isBlank())
                throw new IllegalArgumentException("L'identifiant utilisateur est requis");
        }
    }

    // -------------------------------------------------------------------------
    // Réponses
    // -------------------------------------------------------------------------

    public record PartieReponse(
        String               id,
        EtatPartie           etat,
        int                  manche,
        List<String>         plateau,
        String               joueurCourant,
        Map<String, Integer> scores,
        List<String>         mainJoueurCourant,
        List<String>         dominosJouables
    ) {
        public static PartieReponse depuis(String id, Partie partie,
                                            List<Domino> jouables) {
            List<String> plateauStr = partie.getPlateau().getDominos()
                .stream().map(Domino::toString).toList();

            Map<String, Integer> scoresStr = partie.getScores().entrySet().stream()
                .collect(Collectors.toMap(
                    e -> e.getKey().getPseudo(), Map.Entry::getValue));

            Joueur courant = partie.getJoueurCourant();
            List<String> main = partie.getMain(courant).getDominos()
                .stream().map(Domino::toString).toList();

            return new PartieReponse(
                id, EtatPartie.EN_COURS, partie.getMancheCourante(),
                plateauStr, courant.getPseudo(), scoresStr, main,
                jouables.stream().map(Domino::toString).toList());
        }
    }

    /**
     * Réponse après jouerCoup() ou passerTour().
     *
     * Réponse normale :
     * { "aJoue": true, "etat": "EN_COURS", "message": "...", "gagnant": null, "cotesPossibles": null }
     *
     * Réponse si CHOIX_REQUIS :
     * {
     *   "aJoue": false,
     *   "etat": "CHOIX_REQUIS",
     *   "message": "Le domino [1|3] peut être posé des deux côtés...",
     *   "gagnant": null,
     *   "cotesPossibles": ["GAUCHE", "DROITE"]
     * }
     */
    public record CoupReponse(
        boolean      aJoue,
        EtatPartie   etat,
        String       message,
        String       gagnant,
        List<String> cotesPossibles   // non null si CHOIX_REQUIS
    ) {
        public static CoupReponse depuis(ResultatCoup r) {
            List<String> cotes = null;
            if (r.cotesPossibles() != null) {
                cotes = r.cotesPossibles().stream()
                    .map(Plateau.Cote::name)
                    .toList();
            }
            return new CoupReponse(
                r.aJoue(),
                r.etat(),
                r.message(),
                r.gagnant() != null ? r.gagnant().getPseudo() : null,
                cotes
            );
        }
    }
}
