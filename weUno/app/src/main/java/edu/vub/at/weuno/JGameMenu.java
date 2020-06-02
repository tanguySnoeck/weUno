package edu.vub.at.weuno;

import java.util.Date;

public interface JGameMenu {
    void increasePlayerNb();
    void startGame();
    void notifyStartReq(int id);
    boolean compareStartTimes(String stringDate);
}
