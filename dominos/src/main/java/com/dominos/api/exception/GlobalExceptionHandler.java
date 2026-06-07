package com.dominos.api.exception;

import com.dominos.application.matchmaking.MatchmakingExceptions;
import com.dominos.application.partie.PartieExceptions;
import com.dominos.application.utilisateur.UtilisateurExceptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UtilisateurExceptions.PseudoDejaPrisException.class)
    public ResponseEntity<Map<String, Object>> handlePseudoDejaPris(UtilisateurExceptions.PseudoDejaPrisException ex) {
        return erreur(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(UtilisateurExceptions.EmailDejaPrisException.class)
    public ResponseEntity<Map<String, Object>> handleEmailDejaPris(UtilisateurExceptions.EmailDejaPrisException ex) {
        return erreur(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(UtilisateurExceptions.AuthentificationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentification(UtilisateurExceptions.AuthentificationException ex) {
        return erreur(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(UtilisateurExceptions.UtilisateurIntrouvableException.class)
    public ResponseEntity<Map<String, Object>> handleIntrouvable(UtilisateurExceptions.UtilisateurIntrouvableException ex) {
        return erreur(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MatchmakingExceptions.JoueurDejaEnAttenteException.class)
    public ResponseEntity<Map<String, Object>> handleDejaEnAttente(MatchmakingExceptions.JoueurDejaEnAttenteException ex) {
        return erreur(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MatchmakingExceptions.JoueurDejaEnPartieException.class)
    public ResponseEntity<Map<String, Object>> handleDejaEnPartie(MatchmakingExceptions.JoueurDejaEnPartieException ex) {
        return erreur(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MatchmakingExceptions.JoueurNonEnAttenteException.class)
    public ResponseEntity<Map<String, Object>> handleNonEnAttente(MatchmakingExceptions.JoueurNonEnAttenteException ex) {
        return erreur(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(PartieExceptions.PartieIntrouvableException.class)
    public ResponseEntity<Map<String, Object>> handlePartieIntrouvable(PartieExceptions.PartieIntrouvableException ex) {
        return erreur(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(PartieExceptions.PasTonTourException.class)
    public ResponseEntity<Map<String, Object>> handlePasTonTour(PartieExceptions.PasTonTourException ex) {
        return erreur(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(PartieExceptions.PartieTermineeException.class)
    public ResponseEntity<Map<String, Object>> handlePartieTerminee(PartieExceptions.PartieTermineeException ex) {
        return erreur(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return erreur(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return erreur(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return erreur(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur inattendue s'est produite");
    }

    private ResponseEntity<Map<String, Object>> erreur(HttpStatus status, String message) {
        Map<String, Object> corps = Map.of(
            "status",    status.value(),
            "erreur",    message,
            "timestamp", LocalDateTime.now().toString()
        );
        return ResponseEntity.status(status).body(corps);
    }
}
