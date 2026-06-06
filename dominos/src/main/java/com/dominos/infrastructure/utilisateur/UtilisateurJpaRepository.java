package com.dominos.infrastructure.utilisateur;

import com.dominos.domain.utilisateur.StatutUtilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Interface Spring Data JPA pour la table "utilisateur".
 *
 * Comment ça fonctionne ?
 *
 *   Spring Data JPA lit le nom des méthodes et génère le SQL automatiquement.
 *   Vous n'écrivez pas de SQL — vous déclarez ce que vous voulez.
 *
 *   Exemples de méthodes et leur SQL généré :
 *
 *   findByPseudoIgnoreCase("Alice")
 *   → SELECT * FROM utilisateur WHERE LOWER(pseudo) = LOWER('Alice')
 *
 *   findByEmail("alice@example.com")
 *   → SELECT * FROM utilisateur WHERE email = 'alice@example.com'
 *
 *   existsByPseudoIgnoreCase("Alice")
 *   → SELECT COUNT(*) > 0 FROM utilisateur WHERE LOWER(pseudo) = LOWER('Alice')
 *
 *   JpaRepository<UtilisateurEntity, String> fournit déjà :
 *   - save(entity)         → INSERT ou UPDATE
 *   - findById(id)         → SELECT par clé primaire
 *   - findAll()            → SELECT tous
 *   - deleteById(id)       → DELETE
 *   - count()              → COUNT(*)
 *
 * @Repository indique à Spring que c'est une couche d'accès aux données.
 */
@Repository
public interface UtilisateurJpaRepository extends JpaRepository<UtilisateurEntity, String> {

    Optional<UtilisateurEntity> findByPseudoIgnoreCase(String pseudo);

    Optional<UtilisateurEntity> findByEmailIgnoreCase(String email);

    List<UtilisateurEntity> findByStatut(StatutUtilisateur statut);

    boolean existsByPseudoIgnoreCase(String pseudo);

    boolean existsByEmailIgnoreCase(String email);
}
