package Model.Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import Model.Audio.AudioManager;

/**
 * Rappresenta il mazzo completo di carte da gioco italiane.
 * Il mazzo contiene 40 carte (10 per ogni seme: DENARA, SPADE, BASTONI, COPPE).
 * Fornisce metodi per mescolare, pescare carte e ottenere informazioni sullo stato del mazzo.
 */
public class Mazzo {
    /** Lista delle carte contenute nel mazzo */
    private final List<Cards> cards;
    
    /** Generatore di numeri casuali per mescolare il mazzo */
    private final Random rand = new Random();
    
    /** Gestore audio per riprodurre suoni relativi alle operazioni sul mazzo */
    private final AudioManager audioManager = new AudioManager();

    /**
     * Costruttore del mazzo.
     * Crea un mazzo completo di 40 carte utilizzando tutti i semi e tutti i valori disponibili.
     * Le carte vengono generate usando Stream API per combinare semi e valori.
     */
    public Mazzo() {
        // Usa Streams per creare il mazzo
        cards = Arrays.stream(Cards.Segno.values())
                .flatMap(segno -> Arrays.stream(Cards.Rank.values())
                        .map(rank -> new Cards(segno, rank)))
                .collect(Collectors.toCollection(() -> new ArrayList<>(52)));
    }

    /**
     * Mescola casualmente le carte nel mazzo.
     * Utilizza l'algoritmo di Fisher-Yates tramite Collections.shuffle.
     */
    public void shuffle() {
        Collections.shuffle(cards, rand);
    }

    /**
     * Verifica se il mazzo è vuoto.
     * 
     * @return true se non ci sono più carte nel mazzo, false altrimenti
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * Restituisce il numero di carte rimanenti nel mazzo.
     * 
     * @return numero di carte ancora presenti nel mazzo
     */
    public int size() { return cards.size(); }

    /**
     * Pesca una carta dalla cima del mazzo.
     * La carta viene rimossa dal mazzo dopo essere stata pescata.
     * 
     * @return la carta pescata, o null se il mazzo è vuoto
     */
    public Cards draw() {
        if (cards.isEmpty()) return null;
        return cards.remove(cards.size() - 1);
    }

    /**
     * Restituisce una copia immutabile delle carte attualmente nel mazzo.
     * Utile per visualizzare lo stato del mazzo senza modificarlo.
     * 
     * @return lista immutabile delle carte nel mazzo
     */
    public List<Cards> snapshot() {
        return List.copyOf(cards);
    }

    /**
     * Restituisce il numero di carte rimanenti nel mazzo.
     * Metodo alias di size() per maggiore chiarezza semantica.
     * 
     * @return numero di carte ancora presenti nel mazzo
     */
    public int remaining() {
        return cards.size();
    }
}