package com.dominos.application.tournoi;

import com.dominos.domain.tournoi.Tournoi;

import java.util.List;
import java.util.Optional;

/**
 * Port de persistance pour les tournois.
 */
public interface TournoiRepository {

    Tournoi sauvegarder(Tournoi tournoi);

    Optional<Tournoi> trouverParId(String id);

    List<Tournoi> trouverTous();

    List<Tournoi> trouverEnInscription();
}
