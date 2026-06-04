package com.dominos.application.utilisateur;

import com.dominos.domain.utilisateur.MotDePasseService;
import com.dominos.domain.utilisateur.StatutUtilisateur;
import com.dominos.domain.utilisateur.Utilisateur;

import java.util.List;

/**
 * Service applicatif — orchestration des cas d'usage utilisateur.
 *
 * Responsabilités :
 *  - inscrire(pseudo, email, motDePasse)
 *  - authentifier(pseudo, motDePasse)
 *  - changerStatut(id, statut)
 *  - consulter(id)
 *
 * Ce service ne connaît ni HTTP, ni console, ni WebSocket.
 * Il dépend d'interfaces (UtilisateurRepository, MotDePasseService)
 * et non d'implémentations concrètes — testable unitairement sans BDD.
 */
public class UtilisateurService {

    private final UtilisateurRepository repository;
    private final MotDePasseService     motDePasseService;

    public UtilisateurService(UtilisateurRepository repository,
                              MotDePasseService motDePasseService) {
        this.repository        = repository;
        this.motDePasseService = motDePasseService;
    }

    // -------------------------------------------------------------------------
    // Inscription
    // -------------------------------------------------------------------------

    /**
     * Inscrit un nouvel utilisateur.
     *
     * Étapes :
     *  1. Vérifie que le pseudo n'est pas pris
     *  2. Vérifie que l'email n'est pas pris
     *  3. Hache le mot de passe
     *  4. Crée et persiste l'utilisateur
     *
     * @throws UtilisateurExceptions.PseudoDejaPrisException si pseudo pris
     * @throws UtilisateurExceptions.EmailDejaPrisException  si email pris
     * @throws IllegalArgumentException si les données sont invalides (délégué à Utilisateur)
     */
    public Utilisateur inscrire(String pseudo, String email, String motDePasse) {
        if (repository.pseudoExiste(pseudo)) {
            throw new UtilisateurExceptions.PseudoDejaPrisException(pseudo);
        }
        if (repository.emailExiste(email)) {
            throw new UtilisateurExceptions.EmailDejaPrisException(email);
        }

        String motDePasseHache = motDePasseService.hacher(motDePasse);
        Utilisateur utilisateur = new Utilisateur(pseudo, email, motDePasseHache);
        return repository.sauvegarder(utilisateur);
    }

    // -------------------------------------------------------------------------
    // Authentification
    // -------------------------------------------------------------------------

    /**
     * Authentifie un utilisateur par pseudo + mot de passe.
     *
     * @throws UtilisateurExceptions.AuthentificationException si échec
     */
    public Utilisateur authentifier(String pseudo, String motDePasse) {
        Utilisateur utilisateur = repository.trouverParPseudo(pseudo)
            .orElseThrow(UtilisateurExceptions.AuthentificationException::new);

        if (!motDePasseService.verifier(motDePasse, utilisateur.getMotDePasseHache())) {
            throw new UtilisateurExceptions.AuthentificationException();
        }

        return utilisateur;
    }

    // -------------------------------------------------------------------------
    // Gestion du statut
    // -------------------------------------------------------------------------

    /**
     * Change le statut d'un utilisateur.
     * Utilisé par le matchmaking (Étape 3).
     *
     * @throws UtilisateurExceptions.UtilisateurIntrouvableException si id inconnu
     * @throws IllegalStateException si la transition de statut est invalide
     */
    public Utilisateur changerStatut(String id, StatutUtilisateur nouveauStatut) {
        Utilisateur utilisateur = repository.trouverParId(id)
            .orElseThrow(() -> new UtilisateurExceptions.UtilisateurIntrouvableException(id));

        utilisateur.changerStatut(nouveauStatut);
        return repository.sauvegarder(utilisateur);
    }

    // -------------------------------------------------------------------------
    // Consultation
    // -------------------------------------------------------------------------

    /**
     * Retourne un utilisateur par son id.
     *
     * @throws UtilisateurExceptions.UtilisateurIntrouvableException si id inconnu
     */
    public Utilisateur consulter(String id) {
        return repository.trouverParId(id)
            .orElseThrow(() -> new UtilisateurExceptions.UtilisateurIntrouvableException(id));
    }

    /**
     * Retourne tous les utilisateurs EN_ATTENTE.
     * Utilisé par le matchmaking (Étape 3).
     */
    public List<Utilisateur> getUtilisateursEnAttente() {
        return repository.trouverParStatut(StatutUtilisateur.EN_ATTENTE);
    }
}
