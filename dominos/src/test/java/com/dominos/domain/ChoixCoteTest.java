package com.dominos.domain;

import com.dominos.moteur.EtatPartie;
import com.dominos.moteur.MoteurJeu;
import com.dominos.moteur.ResultatCoup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Choix de côté — règle corrigée")
class ChoixCoteTest {

    // -------------------------------------------------------------------------
    // Tests Plateau.getCotesPossibles()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Plateau.getCotesPossibles()")
    class GetCotesPossibles {

        private Plateau plateau;

        @BeforeEach
        void init() {
            plateau = new Plateau();
            // Construire [1|4][4|2][2|3]
            plateau.poserPremier(new Domino(1, 4));
            plateau.poserACote(new Domino(4, 2), Plateau.Cote.DROITE);
            plateau.poserACote(new Domino(2, 3), Plateau.Cote.DROITE);
            // Plateau final : [1|4][4|2][2|3]
            // gauche=1, droite=3
        }

        @Test
        @DisplayName("plateau [1|4][4|2][2|3] — domino [1|3] → GAUCHE et DROITE possibles")
        void doubleCote() {
            Domino d = new Domino(1, 3);
            Set<Plateau.Cote> cotes = plateau.getCotesPossibles(d);

            assertTrue(cotes.contains(Plateau.Cote.GAUCHE),
                "Devrait pouvoir poser à gauche (1 correspond)");
            assertTrue(cotes.contains(Plateau.Cote.DROITE),
                "Devrait pouvoir poser à droite (3 correspond)");
            assertEquals(2, cotes.size());
        }

        @Test
        @DisplayName("domino [1|2] → seulement GAUCHE")
        void seulementGauche() {
            Domino d = new Domino(1, 2);
            // gauche plateau = 1 → correspond
            // droite plateau = 3 → 1 et 2 ne correspondent pas à 3
            Set<Plateau.Cote> cotes = plateau.getCotesPossibles(d);
            assertTrue(cotes.contains(Plateau.Cote.GAUCHE));
            assertFalse(cotes.contains(Plateau.Cote.DROITE));
        }

        @Test
        @DisplayName("domino [3|5] → seulement DROITE")
        void seulementDroite() {
            Domino d = new Domino(3, 5);
            // droite plateau = 3 → correspond
            // gauche plateau = 1 → 3 et 5 ne correspondent pas à 1
            Set<Plateau.Cote> cotes = plateau.getCotesPossibles(d);
            assertFalse(cotes.contains(Plateau.Cote.GAUCHE));
            assertTrue(cotes.contains(Plateau.Cote.DROITE));
        }

        @Test
        @DisplayName("choixRequis() vrai si deux côtés possibles")
        void choixRequis() {
            assertTrue(plateau.choixRequis(new Domino(1, 3)));
            assertFalse(plateau.choixRequis(new Domino(3, 5)));
        }
    }

    // -------------------------------------------------------------------------
    // Tests Plateau.poserACote()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Plateau.poserACote()")
    class PoserACote {

        private Plateau plateau;

        @BeforeEach
        void init() {
            plateau = new Plateau();
            plateau.poserPremier(new Domino(1, 4));
            plateau.poserACote(new Domino(4, 2), Plateau.Cote.DROITE);
            plateau.poserACote(new Domino(2, 3), Plateau.Cote.DROITE);
        }

        @Test
        @DisplayName("[1|3] posé à GAUCHE → extrémité gauche devient 3")
        void poserAGauche() {
            // [1|3] à gauche : droite du domino (3) == gauche plateau (1)? NON
            // → retourner → [3|1], droite=1 == gauche plateau(1) ✓
            plateau.poserACote(new Domino(1, 3), Plateau.Cote.GAUCHE);
            assertEquals(3, plateau.getExtrémiteGauche().orElseThrow());
            assertEquals(3, plateau.getExtremiteDroite().orElseThrow());
        }

        @Test
        @DisplayName("[1|3] posé à DROITE → extrémité droite devient 1")
        void poserADroite() {
            // [1|3] à droite : gauche du domino (1) == droite plateau (3)? NON
            // → retourner → [3|1], gauche=3 == droite plateau(3) ✓ → droite=1
            plateau.poserACote(new Domino(1, 3), Plateau.Cote.DROITE);
            assertEquals(1, plateau.getExtrémiteGauche().orElseThrow());
            assertEquals(1, plateau.getExtremiteDroite().orElseThrow());
        }

        @Test
        @DisplayName("poser côté impossible → IllegalArgumentException")
        void coteImpossible() {
            // [5|6] ne correspond ni à 1 ni à 3
            assertThrows(IllegalArgumentException.class,
                () -> plateau.poserACote(new Domino(5, 6), Plateau.Cote.GAUCHE));
        }
    }

    // -------------------------------------------------------------------------
    // Tests MoteurJeu — CHOIX_REQUIS
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("MoteurJeu — CHOIX_REQUIS")
    class MoteurChoixRequis {

        private MoteurJeu moteur;
        private Partie    partie;
        private Joueur    alice, bob, carl;

        @BeforeEach
        void setUp() {
            moteur = new MoteurJeu();
            alice  = new Joueur("id-alice", "Alice");
            bob    = new Joueur("id-bob",   "Bob");
            carl   = new Joueur("id-carl",  "Carl");
            partie = new Partie(List.of(alice, bob, carl));

            // Monter un plateau [1|4][4|2][2|3] manuellement
            // et donner à Alice le domino [1|3]
            MainJoueur mainAlice = partie.getMain(alice);
            mainAlice.vider();
            mainAlice.ajouter(new Domino(1, 3));
            mainAlice.ajouter(new Domino(0, 0)); // dominos de remplissage
            mainAlice.ajouter(new Domino(5, 6));

            Plateau p = partie.getPlateau();
            p.poserPremier(new Domino(1, 4));
            p.poserACote(new Domino(4, 2), Plateau.Cote.DROITE);
            p.poserACote(new Domino(2, 3), Plateau.Cote.DROITE);

            partie.setJoueurCourantIndex(0); // Alice
        }

        @Test
        @DisplayName("jouerCoup [1|3] retourne CHOIX_REQUIS")
        void retourneChoixRequis() {
            ResultatCoup r = moteur.jouerCoup(partie, new Domino(1, 3));

            assertEquals(EtatPartie.CHOIX_REQUIS, r.etat());
            assertFalse(r.aJoue());
            assertNotNull(r.cotesPossibles());
            assertEquals(2, r.cotesPossibles().size());
        }

        @Test
        @DisplayName("le domino n'est PAS retiré de la main après CHOIX_REQUIS")
        void dominoResteDansMain() {
            moteur.jouerCoup(partie, new Domino(1, 3));
            assertTrue(partie.getMain(alice).contient(new Domino(1, 3)));
        }

        @Test
        @DisplayName("jouerCoupAvecCote GAUCHE pose correctement")
        void jouerAvecCoteGauche() {
            moteur.jouerCoup(partie, new Domino(1, 3)); // → CHOIX_REQUIS
            ResultatCoup r = moteur.jouerCoupAvecCote(
                partie, new Domino(1, 3), Plateau.Cote.GAUCHE);

            assertTrue(r.aJoue());
            assertNotEquals(EtatPartie.CHOIX_REQUIS, r.etat());
            assertFalse(partie.getMain(alice).contient(new Domino(1, 3)));
        }

        @Test
        @DisplayName("jouerCoupAvecCote DROITE pose correctement")
        void jouerAvecCoteDroite() {
            moteur.jouerCoup(partie, new Domino(1, 3)); // → CHOIX_REQUIS
            ResultatCoup r = moteur.jouerCoupAvecCote(
                partie, new Domino(1, 3), Plateau.Cote.DROITE);

            assertTrue(r.aJoue());
            assertFalse(partie.getMain(alice).contient(new Domino(1, 3)));
        }

        @Test
        @DisplayName("domino à un seul côté → pas de CHOIX_REQUIS")
        void pasDambiguite() {
            // [3|5] → seulement à droite (droite plateau = 3)
            MainJoueur mainAlice = partie.getMain(alice);
            mainAlice.ajouter(new Domino(3, 5));

            ResultatCoup r = moteur.jouerCoup(partie, new Domino(3, 5));
            assertNotEquals(EtatPartie.CHOIX_REQUIS, r.etat());
            assertTrue(r.aJoue());
        }
    }
}
