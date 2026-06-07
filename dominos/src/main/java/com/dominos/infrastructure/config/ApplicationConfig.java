package com.dominos.infrastructure.config;

import com.dominos.application.matchmaking.MatchmakingRepository;
import com.dominos.application.matchmaking.MatchmakingService;
import com.dominos.application.stats.StatsRepository;
import com.dominos.application.stats.StatsService;
import com.dominos.application.utilisateur.UtilisateurRepository;
import com.dominos.application.utilisateur.UtilisateurService;
import com.dominos.domain.utilisateur.MotDePasseService;
import com.dominos.moteur.MoteurJeu;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Spring — déclaration des beans de la couche application.
 */
@Configuration
public class ApplicationConfig {

    @Bean
    public MotDePasseService motDePasseService() {
        return new MotDePasseService();
    }

    @Bean
    public UtilisateurService utilisateurService(UtilisateurRepository repository,
                                                  MotDePasseService motDePasseService) {
        return new UtilisateurService(repository, motDePasseService);
    }

    @Bean
    public MatchmakingService matchmakingService(UtilisateurRepository utilisateurRepository,
                                                  MatchmakingRepository matchmakingRepository) {
        return new MatchmakingService(utilisateurRepository, matchmakingRepository);
    }

    @Bean
    public MoteurJeu moteurJeu() {
        return new MoteurJeu();
    }

    /**
     * Bean StatsService.
     * Spring injecte UtilisateurRepository et StatsRepository
     * (implémentation : StatsRepositoryJpa).
     */
    @Bean
    public StatsService statsService(UtilisateurRepository utilisateurRepository,
                                      StatsRepository statsRepository) {
        return new StatsService(utilisateurRepository, statsRepository);
    }
}
