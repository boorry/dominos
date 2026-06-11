package com.dominos.infrastructure.tournoi;

import com.dominos.application.tournoi.TournoiRepository;
import com.dominos.domain.tournoi.StatutTournoi;
import com.dominos.domain.tournoi.Tournoi;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implémentation in-memory du TournoiRepository.
 *
 * Pour cette étape, les tournois sont stockés en mémoire.
 * À améliorer avec JPA si la persistance complète est requise.
 */
@Component
public class TournoiRepositoryMemoire implements TournoiRepository {

    private final Map<String, Tournoi> store = new ConcurrentHashMap<>();

    @Override
    public Tournoi sauvegarder(Tournoi tournoi) {
        store.put(tournoi.getId(), tournoi);
        return tournoi;
    }

    @Override
    public Optional<Tournoi> trouverParId(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Tournoi> trouverTous() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Tournoi> trouverEnInscription() {
        return store.values().stream()
            .filter(t -> t.getStatut() == StatutTournoi.INSCRIPTION)
            .collect(Collectors.toList());
    }

    public void vider() { store.clear(); }
}
