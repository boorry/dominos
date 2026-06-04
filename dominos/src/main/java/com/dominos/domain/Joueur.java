package com.dominos.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Représente l'identité d'un participant.
 *
 * Responsabilité unique : qui est le joueur (id + pseudo).
 *
 * Changements vs version originale :
 *  - Toute la logique de main (ajouter, retirer, jouables, score, placement)
 *    a été extraite dans MainJoueur
 *  - id est un UUID généré automatiquement (préparation au contexte persistant)
 *  - Joueur est immutable : pas de setters
 *
 * Dans une partie, on associe Joueur ↔ MainJoueur via Partie.
 */
public final class Joueur {

    private final String id;
    private final String pseudo;

    /**
     * Crée un joueur avec un identifiant UUID généré automatiquement.
     */
    public Joueur(String pseudo) {
        this(UUID.randomUUID().toString(), pseudo);
    }

    /**
     * Crée un joueur avec un identifiant explicite.
     * Utile pour la persistance (recharger depuis la base de données).
     */
    public Joueur(String id, String pseudo) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id requis");
        if (pseudo == null || pseudo.isBlank()) throw new IllegalArgumentException("pseudo requis");
        this.id = id;
        this.pseudo = pseudo;
    }

    public String getId() { return id; }
    public String getPseudo() { return pseudo; }

    @Override
    public String toString() {
        return pseudo;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Joueur other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
