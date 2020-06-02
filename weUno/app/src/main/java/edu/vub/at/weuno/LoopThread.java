package edu.vub.at.weuno;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


import static edu.vub.at.weuno.MainActivity.atWeUno;

public class LoopThread extends Thread implements ATMessages{
    public static Handler handler = new Handler() {
        public void handleMessage(Message message) {
            if (atWeUno == null) return;

            switch (message.what) {
                case _START_GAME_: {
                    atWeUno.addStartGameRequest();
                    break;
                }
                case _BROADCAST_DECK_: {
                    atWeUno.broadCastDeck((Deck)message.obj);
                    break;
                }
                case _UPDATE_GUI_: {
                    atWeUno.updateGUI((Activity)message.obj);
                    break;
                }
                case _CARD_PLAYED_: {
                    atWeUno.playCard((Card)message.obj);
                    break;
                }
                case _SKIP_TURN_: {
                    atWeUno.skipTurn();
                    break;
                }
                case _DRAW_CARDS_: {
                    atWeUno.makeNextPlayerDrawCards((int)message.obj);
                    break;
                }
                case _SEND_NOTIFICATION_ : {
                    atWeUno.sendNotification((String)message.obj);
                    break;
                }
                case _SET_COLOR_: {
                    atWeUno.setColor((String)message.obj);
                    break;
                }
                case _UNO_: {
                    atWeUno.callUno();
                    break;
                }
                case _CHECK_UNO_: {
                    atWeUno.checkUno((String)message.obj);
                    break;
                }
                case _END_ROUND_: {
                    atWeUno.endRound();
                    break;
                }
                case _START_NEW_ROUND_: {
                    atWeUno.addStartNewRoundReq((boolean)message.obj);
                    break;
                }
                case _SET_IP_VALUE_: {
                    atWeUno.setIpValue((int)message.obj);
                    break;
                }
            }
        }
    };

    public void run() {
        Looper.prepare();
        Looper.loop();
    }
}