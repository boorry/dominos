package com.dominos.infrastructure.config;

import com.dominos.application.matchmaking.MatchmakingRepository;
import com.dominos.application.matchmaking.MatchmakingService;
import com.dominos.application.utilisateur.UtilisateurRepository;
import com.dominos.application.utilisateur.UtilisateurService;
import com.dominos.domain.utilisateur.MotDePasseService;
import com.dominos.moteur.MoteurJeu;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Spring — déclare les beans de la couche application.
 *
 * Un "bean" Spring est un objet dont Spring gère le cycle de vie
 * (création, injection, destruction).
 *
 * @Configuration : cette classe contient des déclarations de beans.
 * @Bean          : cette méthode crée et retourne un bean Spring.
 *
 * Pourquoi déclarer les services ici ?
 *   UtilisateurService et MatchmakingService dépendent d'interfaces
 *   (UtilisateurRepository, MatchmakingRepository).
 *   C'est ici qu'on dit à Spring quelle implémentation concrète utiliser.
 *
 *   En test : on peut remplacer ces beans par des mocks.
 *   En production : Spring injecte les implémentations JPA.
 */
@Configuration
public class ApplicationConfig {

    /**
     * Bean MotDePasseService.
     * Pas de dépendance — Spring peut le créer directement.
     */
    @Bean
    public MotDePasseService motDePasseService() {
        return new MotDePasseService();
    }

    /**
     * Bean UtilisateurService.
     * Spring injecte automatiquement UtilisateurRepository
     * (implémentation : UtilisateurRepositoryJpa) et MotDePasseService.
     */
    @Bean
    public UtilisateurService utilisateurService(UtilisateurRepository repository,
                                                  MotDePasseService motDePasseService) {
        return new UtilisateurService(repository, motDePasseService);
    }

    /**
     * Bean MatchmakingService.
     * Spring injecte UtilisateurRepository et MatchmakingRepository.
     */
    @Bean
    public MatchmakingService matchmakingService(UtilisateurRepository utilisateurRepository,
                                                  MatchmakingRepository matchmakingRepository) {
        return new MatchmakingService(utilisateurRepository, matchmakingRepository);
    }

    /**
     * Bean MoteurJeu.
     * Pas de dépendance — le moteur est stateless.
     */
    @Bean
    public MoteurJeu moteurJeu() {
        return new MoteurJeu();
    }
}
