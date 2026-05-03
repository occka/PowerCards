package com.yourname.cardboost.card;

import com.yourname.cardboost.card.impl.ShieldCard;
import com.yourname.cardboost.card.impl.SpeedCard;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class CardRegistry {
    private static final Map<String, Card> CARDS = new LinkedHashMap<>();

    public static void init() {
        register(new SpeedCard());
        register(new ShieldCard());
        // register(new JumpCard());
    }

    public static void register(Card card) {
        CARDS.put(card.getId(), card);
    }

    public static Card getById(String id) { return CARDS.get(id); }
    public static Collection<Card> getAll() { return CARDS.values(); }
}
