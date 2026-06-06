package com.dominos.infrastructure.partie;

import com.dominos.moteur.EtatPartie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Interface Spring Data JPA pour la table "partie".
 *
 * Méthodes fournies automatiquement par JpaRepository :
 *  - save(entity)      → INSERT ou UPDATE
 *  - findById(id)      → SELECT par id
 *  - findAll()         → SELECT toutes les parties
 *  - deleteById(id)    → DELETE
 *
 * Méthodes déclarées ici (SQL généré automatiquement) :
 *  - findByEtat(etat)
 *    → SELECT * FROM partie WHERE etat = ?
 *
 *  - findByJoueurs_UtilisateurId(id)
 *    → SELECT p.* FROM partie p
 *       JOIN joueur_partie jp ON jp.partie_id = p.id
 *       WHERE jp.utilisateur_id = ?
 *    (navigation dans la relation OneToMany via "_")
 */
@Repository
public interface PartieJpaRepository extends JpaRepository<PartieEntity, String> {

    List<PartieEntity> findByEtat(EtatPartie etat);

    List<PartieEntity> findByJoueurs_UtilisateurId(String utilisateurId);
}
