package com.dominos.domain.partie;

import com.dominos.domain.utilisateur.Utilisateur;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Représente une partie prête à démarrer — les 3 joueurs ont été trouvés.
 */
public class PartieEnAttente {

    private final String            id;
    private final List<Utilisateur> joueurs;
    private final LocalDateTime     creeLe;

    public PartieEnAttente(List<Utilisateur> joueurs) {
        if (joueurs == null || joueurs.size() != 3) {
            throw new IllegalArgumentException(
                "Une partie requiert exactement 3 joueurs");
        }
        this.id      = UUID.randomUUID().toString();
        this.joueurs = Collections.unmodifiableList(List.copyOf(joueurs));
        this.creeLe  = LocalDateTime.now();
    }

    public String getId()                 { return id; }
    public List<Utilisateur> getJoueurs() { return joueurs; }
    public LocalDateTime getCreeLe()      { return creeLe; }

    @Override
    public String toString() {
        return "PartieEnAttente{id='" + id + "', joueurs="
            + joueurs.stream().map(Utilisateur::getPseudo).toList() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PartieEnAttente other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
