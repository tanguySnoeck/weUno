package edu.vub.at.weuno;

import java.util.Queue;

//java methods callable from at code
interface JWeUno {
    void topUnoAnimation();
    void leftUnoAnimation();
    void rightUnoAnimation();

    void setTopPlayerCardCount(int n);
    void setLeftPlayerCardCount(int n);
    void setRightPlayerCardCount(int n);

    void setPlayerNb(int playerNb);
    void startGame();
    void makeToast(String message);
    //boolean compareStartTimes(String stringDate);
    boolean compareStartTimes(String[] timeStamps);
    void setDeck(Card[] cards);

    void foreignCardPlayed(Card card);
    void setTurn();

    void setColor(String color);
}
