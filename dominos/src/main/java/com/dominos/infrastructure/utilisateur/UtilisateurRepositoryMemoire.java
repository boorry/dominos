package com.dominos.infrastructure.utilisateur;

import com.dominos.application.utilisateur.UtilisateurRepository;
import com.dominos.domain.utilisateur.StatutUtilisateur;
import com.dominos.domain.utilisateur.Utilisateur;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implémentation en mémoire du repository utilisateur.
 *
 * Utilisée à cette étape pour que tout soit testable sans base de données.
 * Remplacée à l'Étape 4 par UtilisateurRepositoryJpa (Spring Data JPA).
 *
 * ConcurrentHashMap : préparation au contexte multi-thread du matchmaking.
 * Le service ne voit que l'interface — il ne sait pas ce qui est derrière.
 */
public class UtilisateurRepositoryMemoire implements UtilisateurRepository {

    // Stockage principal : id → Utilisateur
    private final Map<String, Utilisateur> store = new ConcurrentHashMap<>();

    // -------------------------------------------------------------------------
    // UtilisateurRepository
    // -------------------------------------------------------------------------

    @Override
    public Utilisateur sauvegarder(Utilisateur utilisateur) {
        store.put(utilisateur.getId(), utilisateur);
        return utilisateur;
    }

    @Override
    public Optional<Utilisateur> trouverParId(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Utilisateur> trouverParPseudo(String pseudo) {
        if (pseudo == null) return Optional.empty();
        return store.values().stream()
            .filter(u -> u.getPseudo().equalsIgnoreCase(pseudo))
            .findFirst();
    }

    @Override
    public Optional<Utilisateur> trouverParEmail(String email) {
        if (email == null) return Optional.empty();
        return store.values().stream()
            .filter(u -> u.getEmail().equalsIgnoreCase(email))
            .findFirst();
    }

    @Override
    public List<Utilisateur> trouverParStatut(StatutUtilisateur statut) {
        return store.values().stream()
            .filter(u -> u.getStatut() == statut)
            .collect(Collectors.toList());
    }

    @Override
    public boolean pseudoExiste(String pseudo) {
        return trouverParPseudo(pseudo).isPresent();
    }

    @Override
    public boolean emailExiste(String email) {
        return trouverParEmail(email).isPresent();
    }

    // -------------------------------------------------------------------------
    // Utilitaire (tests)
    // -------------------------------------------------------------------------

    /** Vide le store — utile entre deux tests. */
    public void vider() {
        store.clear();
    }

    public int compter() {
        return store.size();
    }
}
