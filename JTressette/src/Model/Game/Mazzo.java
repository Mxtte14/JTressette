package Model.Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import Model.Audio.AudioManager;

public class Mazzo {
    private final List<Cards> cards;
    private final Random rand = new Random();
    private final AudioManager audioManager = new AudioManager();

    public Mazzo() {
        // Usa Streams per creare il mazzo
        cards = Arrays.stream(Cards.Segno.values())
            .flatMap(segno -> Arrays.stream(Cards.Rank.values())
                .map(rank -> new Cards(segno, rank)))
            .collect(Collectors.toCollection(() -> new ArrayList<>(52)));
    }

    public void shuffle() {
        Collections.shuffle(cards, rand);
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int size() { return cards.size(); }

    public Cards draw() {
        if (cards.isEmpty()) return null;
        return cards.remove(cards.size() - 1);
    }

    public List<Cards> snapshot() {
        return List.copyOf(cards);
    }

    public int remaining() {
        return cards.size();
    }
}
