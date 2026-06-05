package com.dominos.infrastructure.matchmaking;

import com.dominos.application.matchmaking.MatchmakingRepository;
import com.dominos.domain.utilisateur.Utilisateur;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Implémentation en mémoire de la file d'attente du matchmaking.
 *
 * Utilise une LinkedList comme file FIFO (First In, First Out) :
 * le premier joueur arrivé est le premier dans la partie.
 *
 * Remplacée à l'Étape 4 par une implémentation persistante
 * (base de données ou Redis pour le temps réel).
 */
public class MatchmakingRepositoryMemoire implements MatchmakingRepository {

    // File FIFO — ordre d'arrivée garanti
    private final Queue<Utilisateur> file = new LinkedList<>();

    @Override
    public void ajouterEnAttente(Utilisateur utilisateur) {
        if (!estEnAttente(utilisateur.getId())) {
            file.add(utilisateur);
        }
    }

    @Override
    public void retirerDeAttente(Utilisateur utilisateur) {
        file.removeIf(u -> u.getId().equals(utilisateur.getId()));
    }

    @Override
    public List<Utilisateur> getPremiersEnAttente(int nombre) {
        List<Utilisateur> resultat = new ArrayList<>();
        int count = 0;
        for (Utilisateur u : file) {
            if (count >= nombre) break;
            resultat.add(u);
            count++;
        }
        return resultat;
    }

    @Override
    public boolean estEnAttente(String utilisateurId) {
        return file.stream().anyMatch(u -> u.getId().equals(utilisateurId));
    }

    @Override
    public int nombreEnAttente() {
        return file.size();
    }

    /** Utilitaire pour les tests. */
    public void vider() {
        file.clear();
    }
}
