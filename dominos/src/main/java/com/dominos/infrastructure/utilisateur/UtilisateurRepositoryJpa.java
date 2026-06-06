package com.dominos.infrastructure.utilisateur;

import com.dominos.application.utilisateur.UtilisateurRepository;
import com.dominos.domain.utilisateur.StatutUtilisateur;
import com.dominos.domain.utilisateur.Utilisateur;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Implémentation JPA du port UtilisateurRepository.
 *
 * Cette classe fait le pont entre :
 *   - l'interface métier (UtilisateurRepository) que le service connaît
 *   - Spring Data JPA (UtilisateurJpaRepository) qui parle à PostgreSQL
 *
 * Flux de sauvegarde :
 *   Utilisateur (domaine) → UtilisateurEntity (JPA) → table PostgreSQL
 *
 * Flux de lecture :
 *   table PostgreSQL → UtilisateurEntity (JPA) → Utilisateur (domaine)
 *
 * @Component indique à Spring de créer automatiquement une instance
 * de cette classe et de l'injecter partout où UtilisateurRepository
 * est demandé. C'est l'injection de dépendances en action.
 *
 * Remplace : UtilisateurRepositoryMemoire (étapes 1-3)
 */
@Component
public class UtilisateurRepositoryJpa implements UtilisateurRepository {

    private final UtilisateurJpaRepository jpa;

    /**
     * Spring injecte automatiquement UtilisateurJpaRepository ici.
     * Pas besoin de "new" — Spring gère le cycle de vie.
     */
    public UtilisateurRepositoryJpa(UtilisateurJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Utilisateur sauvegarder(Utilisateur utilisateur) {
        UtilisateurEntity entity = UtilisateurEntity.depuisDomaine(utilisateur);
        UtilisateurEntity sauvegarde = jpa.save(entity);
        return sauvegarde.versDomaine();
    }

    @Override
    public Optional<Utilisateur> trouverParId(String id) {
        return jpa.findById(id)
                  .map(UtilisateurEntity::versDomaine);
    }

    @Override
    public Optional<Utilisateur> trouverParPseudo(String pseudo) {
        return jpa.findByPseudoIgnoreCase(pseudo)
                  .map(UtilisateurEntity::versDomaine);
    }

    @Override
    public Optional<Utilisateur> trouverParEmail(String email) {
        return jpa.findByEmailIgnoreCase(email)
                  .map(UtilisateurEntity::versDomaine);
    }

    @Override
    public List<Utilisateur> trouverParStatut(StatutUtilisateur statut) {
        return jpa.findByStatut(statut)
                  .stream()
                  .map(UtilisateurEntity::versDomaine)
                  .toList();
    }

    @Override
    public boolean pseudoExiste(String pseudo) {
        return jpa.existsByPseudoIgnoreCase(pseudo);
    }

    @Override
    public boolean emailExiste(String email) {
        return jpa.existsByEmailIgnoreCase(email);
    }
}
