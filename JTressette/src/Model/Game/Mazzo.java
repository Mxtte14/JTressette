package Model.Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import Model.Audio.AudioManager;

public class Mazzo {
    private final List<Cards> cards;
    private final Random rand = new Random();
    private final AudioManager audioManager = new AudioManager();

    public Mazzo() {
        cards = new ArrayList<>(52);
        for (Cards.Segno s : Cards.Segno.values()) {
            for (Cards.Rank r : Cards.Rank.values()) {
                cards.add(new Cards(s, r));
            }
        }
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
