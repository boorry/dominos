package com.dominos.domain.utilisateur;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service de hachage et vérification des mots de passe.
 *
 * Responsabilité unique : tout ce qui touche à la sécurité des mots de passe.
 *
 * Implémentation :
 *  On utilise SHA-256 + sel aléatoire (salt) pour rester sans dépendance
 *  externe à cette étape. À l'Étape 5 (Spring Boot), on remplacera par
 *  BCryptPasswordEncoder de Spring Security — l'interface ne changera pas.
 *
 * Format du hash stocké : "sel$hash" (sel en Base64 + $ + hash en Base64)
 * Exemple : "aBcD1234$XyZ9876..."
 *
 * Règles :
 *  - Mot de passe minimum 8 caractères
 *  - Mot de passe maximum 128 caractères
 *  - On ne stocke JAMAIS le mot de passe en clair
 */
public class MotDePasseService {

    private static final int    LONGUEUR_SEL_BYTES = 16;
    private static final int    MIN_LONGUEUR       = 8;
    private static final int    MAX_LONGUEUR       = 128;
    private static final String SEPARATEUR         = "$";

    private final SecureRandom random = new SecureRandom();

    // -------------------------------------------------------------------------
    // API publique
    // -------------------------------------------------------------------------

    /**
     * Valide et hache un mot de passe.
     *
     * @param motDePasse le mot de passe en clair (jamais stocké)
     * @return la chaîne "sel$hash" à stocker
     * @throws IllegalArgumentException si le mot de passe ne respecte pas les règles
     */
    public String hacher(String motDePasse) {
        validerMotDePasse(motDePasse);
        byte[] sel = genererSel();
        String hash = calculerHash(motDePasse, sel);
        return Base64.getEncoder().encodeToString(sel) + SEPARATEUR + hash;
    }

    /**
     * Vérifie qu'un mot de passe en clair correspond au hash stocké.
     *
     * @param motDePasse   le mot de passe saisi par l'utilisateur
     * @param hashStocke   la valeur "sel$hash" stockée en base
     * @return true si le mot de passe correspond
     */
    public boolean verifier(String motDePasse, String hashStocke) {
        if (motDePasse == null || hashStocke == null) return false;
        try {
            String[] parties = hashStocke.split("\\" + SEPARATEUR, 2);
            if (parties.length != 2) return false;

            byte[] sel  = Base64.getDecoder().decode(parties[0]);
            String hash = calculerHash(motDePasse, sel);
            return hashConstantTime(hash, parties[1]);
        } catch (Exception e) {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Privé
    // -------------------------------------------------------------------------

    private void validerMotDePasse(String motDePasse) {
        if (motDePasse == null || motDePasse.length() < MIN_LONGUEUR) {
            throw new IllegalArgumentException(
                "Le mot de passe doit contenir au moins " + MIN_LONGUEUR + " caractères");
        }
        if (motDePasse.length() > MAX_LONGUEUR) {
            throw new IllegalArgumentException(
                "Le mot de passe ne peut pas dépasser " + MAX_LONGUEUR + " caractères");
        }
    }

    private byte[] genererSel() {
        byte[] sel = new byte[LONGUEUR_SEL_BYTES];
        random.nextBytes(sel);
        return sel;
    }

    private String calculerHash(String motDePasse, byte[] sel) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(sel);
            byte[] hash = digest.digest(motDePasse.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 non disponible", e);
        }
    }

    /**
     * Comparaison en temps constant pour éviter les attaques par timing.
     * Ne pas remplacer par String.equals().
     */
    private boolean hashConstantTime(String a, String b) {
        if (a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }
}
