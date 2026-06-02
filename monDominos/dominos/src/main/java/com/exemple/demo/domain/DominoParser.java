package com.dominos.domain;

/**
 * Utilitaire de parsing de dominos depuis une chaîne de caractères.
 *
 * Extrait de l'ancienne classe Domino pour respecter le principe
 * de responsabilité unique (SRP) : Domino est un Value Object,
 * la logique de parsing n'a pas sa place dedans.
 *
 * Formats acceptés :
 *  - [x|y]  ex. [3|5]
 *  - x,y    ex. 3,5 ou 3, 5
 */
public final class DominoParser {

    private DominoParser() {
        // Classe utilitaire — pas d'instanciation
    }

    public static Domino parse(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Entrée vide ou nulle");
        }
        String[] valeurs = extraireValeurs(input.trim());
        return construire(valeurs);
    }

    // -------------------------------------------------------------------------
    // Privé
    // -------------------------------------------------------------------------

    private static String[] extraireValeurs(String input) {
        if (estFormatCrochet(input)) {
            return input.replaceAll("[\\[\\]]", "").split("\\|");
        }
        if (estFormatVirgule(input)) {
            return input.split("\\s*,\\s*");
        }
        throw new IllegalArgumentException(
            "Format invalide : '" + input + "' — attendu [x|y] ou x,y"
        );
    }

    private static Domino construire(String[] valeurs) {
        try {
            int gauche = Integer.parseInt(valeurs[0].trim());
            int droite = Integer.parseInt(valeurs[1].trim());
            return new Domino(gauche, droite);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valeurs non numériques : " + e.getMessage());
        }
    }

    private static boolean estFormatCrochet(String input) {
        return input.matches("\\[\\d\\|\\d\\]");
    }

    private static boolean estFormatVirgule(String input) {
        return input.matches("\\d\\s*,\\s*\\d");
    }
}
