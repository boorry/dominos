package com.dominos;

import com.dominos.console.ConsoleUI;

/**
 * Point d'entrée de l'application console.
 *
 * Responsabilité unique : instancier ConsoleUI et lancer la session.
 * Aucune logique ici — tout est dans ConsoleUI (I/O) et MoteurJeu (règles).
 */
public class App {
    public static void main(String[] args) {
        new ConsoleUI().demarrer();
    }
}
