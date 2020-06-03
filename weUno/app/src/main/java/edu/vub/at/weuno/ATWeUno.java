package edu.vub.at.weuno;

import android.app.Activity;

import java.util.Date;
import java.util.Queue;

public interface ATWeUno {
    void addStartGameRequest();
    void setIpValue(int value);
    void broadCastDeck(Deck deck);
    void updateGUI(Activity newActivity);
    void playCard(Card card);
    void skipTurn();
    void makeNextPlayerDrawCards(int nbOfCards);
    void drawCards(int nbOfCards);
    void sendNotification(String message);
    void setColor(String color);
    void callUno();
    void checkUno(String position);
    void endRound();
    void addStartNewRoundReq(boolean continues);
    void setUno(boolean calledUno);
}
