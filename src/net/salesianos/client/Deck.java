package net.salesianos.client;

import java.util.*;

public class Deck {
    private List<String> cards;

    public Deck() {
        cards = new ArrayList<>();
        String[] colors = {"Rojo", "Azul", "Verde", "Amarillo"};
        String[] values = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

        for (String color : colors) {
            for (String value : values) {
                cards.add(color + " " + value);
                if (!value.equals("0")) {
                    cards.add(color + " " + value);
                }
            }
        }
        Collections.shuffle(cards);
    }

    public String drawCard() {
        return cards.isEmpty() ? "No hay cartas" : cards.remove(0);
    }
}
