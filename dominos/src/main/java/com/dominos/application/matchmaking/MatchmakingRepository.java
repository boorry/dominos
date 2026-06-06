package com.dominos.application.matchmaking;

import com.dominos.domain.utilisateur.Utilisateur;

import java.util.List;

/**
 * Port de persistance pour la file d'attente du matchmaking.
 */
public interface MatchmakingRepository {

    void ajouterEnAttente(Utilisateur utilisateur);

    void retirerDeAttente(Utilisateur utilisateur);

    List<Utilisateur> getPremiersEnAttente(int nombre);

    boolean estEnAttente(String utilisateurId);

    int nombreEnAttente();
}
