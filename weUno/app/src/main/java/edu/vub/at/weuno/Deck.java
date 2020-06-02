package edu.vub.at.weuno;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class Deck {

    public Queue<Card> cards;

    public Deck() {
        LinkedList<Card> c = makeFullDeck();
        Collections.shuffle(c);

        cards = c;
    }

    public Card peekTopCard() {
        final Iterator<Card> itr = cards.iterator();
        Card lastElement = itr.next();
        while(itr.hasNext()) {
            lastElement = itr.next();
        }
        return lastElement;
    }

    public Card drawCard() {
        return cards.poll();
    }

    public static LinkedList<Card> makeFullDeck() {
        LinkedList<Card> deck = new LinkedList<>();

        for (Card.Color c : Card.Color.values())
            for (Card.Action a : Card.Action.values())
                if (c != Card.Color.wild && a != Card.Action.color && a != Card.Action.plus4)
                    if (a == Card.Action.a0)
                        deck.add(new Card(c, a));
                    else
                        Collections.addAll(deck, new Card[]{new Card(c, a), new Card(c, a)});


        for (int i=0; i<4; i++) {
            deck.add(new Card(Card.Color.wild, Card.Action.color));
            deck.add(new Card(Card.Color.wild, Card.Action.plus4));
        }

        return deck;
    }

}
