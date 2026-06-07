package com.dominos.application.stats;

import com.dominos.api.dto.stats.StatsReponse;
import com.dominos.application.utilisateur.UtilisateurExceptions;
import com.dominos.application.utilisateur.UtilisateurRepository;
import com.dominos.domain.utilisateur.Utilisateur;

/**
 * Service applicatif pour les statistiques.
 *
 * Responsabilités :
 *  - Vérifier que l'utilisateur existe
 *  - Déléguer le calcul au StatsRepository
 *  - Construire le DTO de réponse
 */
public class StatsService {

    private final UtilisateurRepository utilisateurRepository;
    private final StatsRepository       statsRepository;

    public StatsService(UtilisateurRepository utilisateurRepository,
                        StatsRepository statsRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.statsRepository       = statsRepository;
    }

    /**
     * Retourne les statistiques d'un joueur.
     *
     * @throws UtilisateurExceptions.UtilisateurIntrouvableException si id inconnu
     */
    public StatsReponse getStats(String utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository
            .trouverParId(utilisateurId)
            .orElseThrow(() ->
                new UtilisateurExceptions.UtilisateurIntrouvableException(utilisateurId));

        StatsResultat resultat = statsRepository.calculerStats(utilisateurId);

        if (resultat.partiesJouees() == 0) {
            return StatsReponse.vide(utilisateurId, utilisateur.getPseudo());
        }

        return new StatsReponse(
            utilisateurId,
            utilisateur.getPseudo(),
            resultat.partiesJouees(),
            resultat.partiesGagnees(),
            resultat.partiesPerdues(),
            resultat.scoreTotal(),
            resultat.scoreMoyen(),
            resultat.meilleurScore()
        );
    }
}
