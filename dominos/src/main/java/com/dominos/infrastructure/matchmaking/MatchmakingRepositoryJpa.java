package com.dominos.infrastructure.matchmaking;

import com.dominos.application.matchmaking.MatchmakingRepository;
import com.dominos.application.utilisateur.UtilisateurRepository;
import com.dominos.domain.utilisateur.StatutUtilisateur;
import com.dominos.domain.utilisateur.Utilisateur;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implémentation JPA de la file d'attente matchmaking.
 *
 * Contrairement à l'implémentation mémoire (étape 3), cette version
 * utilise la base de données comme source de vérité.
 *
 * La "file d'attente" est simplement la liste des utilisateurs
 * dont le statut est EN_ATTENTE dans la table utilisateur.
 *
 * Avantage : si le serveur redémarre, les joueurs EN_ATTENTE
 * sont toujours dans la file — la donnée est persistante.
 *
 * Ordre FIFO : on trie par dateInscription (proxy d'ordre d'arrivée).
 * À l'Étape 6 (WebSocket), on pourra ajouter une colonne date_mise_en_attente.
 */
@Component
public class MatchmakingRepositoryJpa implements MatchmakingRepository {

    private final UtilisateurRepository utilisateurRepository;

    public MatchmakingRepositoryJpa(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public void ajouterEnAttente(Utilisateur utilisateur) {
        // Le statut EN_ATTENTE est déjà positionné par MatchmakingService
        // avant d'appeler cette méthode — rien à faire ici.
        // La persistance est gérée par UtilisateurRepository.sauvegarder()
    }

    @Override
    public void retirerDeAttente(Utilisateur utilisateur) {
        // Idem — le changement de statut est géré par MatchmakingService
    }

    @Override
    public List<Utilisateur> getPremiersEnAttente(int nombre) {
        return utilisateurRepository
            .trouverParStatut(StatutUtilisateur.EN_ATTENTE)
            .stream()
            .limit(nombre)
            .toList();
    }

    @Override
    public boolean estEnAttente(String utilisateurId) {
        return utilisateurRepository.trouverParId(utilisateurId)
            .map(Utilisateur::estEnAttente)
            .orElse(false);
    }

    @Override
    public int nombreEnAttente() {
        return utilisateurRepository
            .trouverParStatut(StatutUtilisateur.EN_ATTENTE)
            .size();
    }
}
