package com.dominos.application.utilisateur;

import com.dominos.domain.utilisateur.StatutUtilisateur;
import com.dominos.domain.utilisateur.Utilisateur;

import java.util.List;
import java.util.Optional;

/**
 * Port de persistance pour les utilisateurs.
 *
 * C'est une INTERFACE — elle déclare ce dont l'application a besoin
 * sans savoir comment c'est implémenté.
 *
 * À cette étape : implémentée par UtilisateurRepositoryMemoire (in-memory).
 * À l'Étape 4   : implémentée par UtilisateurRepositoryJpa (PostgreSQL).
 *
 * Le service ne change pas — seule l'implémentation est swappée.
 * C'est le principe d'inversion de dépendances (DIP — SOLID).
 */
public interface UtilisateurRepository {

    /**
     * Persiste un nouvel utilisateur ou met à jour un existant.
     */
    Utilisateur sauvegarder(Utilisateur utilisateur);

    /**
     * Recherche par identifiant unique.
     */
    Optional<Utilisateur> trouverParId(String id);

    /**
     * Recherche par pseudo (insensible à la casse).
     */
    Optional<Utilisateur> trouverParPseudo(String pseudo);

    /**
     * Recherche par email (insensible à la casse).
     */
    Optional<Utilisateur> trouverParEmail(String email);

    /**
     * Retourne tous les utilisateurs ayant le statut donné.
     * Utilisé par le matchmaking (Étape 3) pour trouver les EN_ATTENTE.
     */
    List<Utilisateur> trouverParStatut(StatutUtilisateur statut);

    /**
     * Vérifie l'existence d'un pseudo (pour la validation à l'inscription).
     */
    boolean pseudoExiste(String pseudo);

    /**
     * Vérifie l'existence d'un email (pour la validation à l'inscription).
     */
    boolean emailExiste(String email);
}
