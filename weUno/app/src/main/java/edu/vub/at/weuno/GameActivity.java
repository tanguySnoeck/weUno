package edu.vub.at.weuno;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import edu.vub.at.IAT;

import static edu.vub.at.weuno.MainActivity.atWeUno;
import static edu.vub.at.weuno.MainActivity.handler;

public class GameActivity extends AppCompatActivity implements HandAction, ATMessages {

    private CardViewAdapter adapter;

    private DrawingView drawingview;
    private Deck cardDeck;
    private TextView txtUno;
    private Button btnUno, btnUnoTop, btnUnoLeft, btnUnoRight, drawBtn;
    private Animation animUnoTop, animUnoBottom, animUnoLeft, animUnoRight;
    private static IAT iat;
    private int playerNb = 0;
    private boolean itsMyTurn = false;
    private Card topCard;
    private String chosenColor = null;
    private boolean isFirst;
    private boolean roundWinner = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isFirst = getIntent().getExtras().getBoolean("IS_FIRST");
        playerNb = getIntent().getExtras().getInt("PLAYER_NB");
        handler.sendMessage(Message.obtain(handler, _UPDATE_GUI_, this));

        cardDeck = new Deck();
        drawingview = findViewById(R.id.drawingview);

        ArrayList<Card> cards = new ArrayList<>();

        RecyclerView handView = findViewById(R.id.playerhand);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(GameActivity.this, LinearLayoutManager.HORIZONTAL, false);
        handView.setLayoutManager(horizontalLayoutManager);
        adapter = new CardViewAdapter(this, cards, this);
        handView.setAdapter(adapter);

        MoveAndPlaceHelper mh = new MoveAndPlaceHelper(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(mh);
        touchHelper.attachToRecyclerView(handView);

        // setup animations
        animUnoTop    = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.uno_top);
        animUnoBottom = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.uno_bottom);
        animUnoLeft   = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.uno_left);
        animUnoRight  = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.uno_right);

        txtUno = findViewById(R.id.txtUno);
        btnUno = findViewById(R.id.btnUno);
        btnUnoTop   = findViewById(R.id.btnUnoTop);
        btnUnoLeft  = findViewById(R.id.btnUnoLeft);
        btnUnoRight = findViewById(R.id.btnUnoRight);
        drawBtn = findViewById(R.id.drawBtn);

        drawBtn.setOnClickListener(view -> {
            skipTurn();
        });

        btnUno.setOnClickListener(v -> {
            if (adapter.getItemCount() > 1) {
                makeToast("You can't call uno if you have more than one card");
            } else {
                handler.sendMessage(Message.obtain(handler, _UNO_));
                startUnoAnimation(animUnoBottom);
            }
        });

        drawingview.setLeftPlayerCount(0);
        drawingview.setRightPlayerCount(0);
        drawingview.setTopPlayerCount(0);

        // TODO: currently clicking on a player stack shows the uno animation, but it should behah changed to check if the player called uno in time
        btnUnoTop.setOnClickListener(v -> {
            handler.sendMessage(Message.obtain(handler, _CHECK_UNO_, "top"));
        });
        btnUnoLeft.setOnClickListener(v -> {
            handler.sendMessage(Message.obtain(handler, _CHECK_UNO_, "left"));
        });
        btnUnoRight.setOnClickListener(v -> { handler.sendMessage(Message.obtain(handler, _CHECK_UNO_, "right")); });

        drawCards(3);

        if (isFirst) {
            Log.i("test", "is first");
            makeToast("You start !");
            itsMyTurn = true;
            handler.sendMessage(Message.obtain(handler, _BROADCAST_DECK_, cardDeck));
            setDeck(null);
        }
    }

    private void skipTurn() {
        if (!itsMyTurn) {
            makeToast("You can't skip, it's not your turn !");
        } else {
            drawCards(1);
            handler.sendMessage(Message.obtain(handler, _SKIP_TURN_));
            itsMyTurn = false;
        }
    }

    public void startUnoAnimation(Animation animation){
        runOnUiThread(() -> {
            txtUno.setVisibility(View.VISIBLE);
            txtUno.startAnimation(animation);
        });
    }

    //TODO: call this whenever the player has to draw cards
    public void drawCards(int n) {
        runOnUiThread(() -> {
            for (int i = 0; i < n; i++)
                adapter.addCard(cardDeck.drawCard());
        });

        btnUno.setVisibility(adapter.getItemCount() < 2 ? View.VISIBLE : View.INVISIBLE);
    }

    public void setFirst(boolean isFirst) {
        this.isFirst = isFirst;
    }

    //TODO: call these methods from AmbientTalk indicating that another player has said Uno
    public void topUnoAnimation() {
        startUnoAnimation(animUnoTop);
    }
    public void leftUnoAnimation() {
        startUnoAnimation(animUnoLeft);
    }
    public void rightUnoAnimation() {
        startUnoAnimation(animUnoRight);
    }

    //TODO: call these methods from AmbientTalk to set the number of cards for the other players
    public void setTopPlayerCardCount(int n) {
        runOnUiThread(() -> {
            drawingview.setTopPlayerCount(n);
            drawingview.invalidate();
        });
    }
    public void setLeftPlayerCardCount(int n) {
        runOnUiThread(() -> {
            drawingview.setLeftPlayerCount(n);
            drawingview.invalidate();
        });
    }
    public void setRightPlayerCardCount(int n) {
        runOnUiThread(() -> {
            drawingview.setRightPlayerCount(n);
            drawingview.invalidate();
        });
    }

    public void makeToast(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(GameActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setDeck(Card[] cards) {
        if (cards != null) {
            Log.i("set deck", "cards not null");
            cardDeck.cards = (Queue<Card>) new LinkedList<>(Arrays.asList(cards));
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                drawingview = findViewById(R.id.drawingview);

                do {
                    topCard = cardDeck.peekTopCard();
                    Log.i("DEBUG", topCard.getAction().toString());
                }while(!topCard.getAction().toString().equals("skip") || !topCard.getAction().toString().equals("reverse"));
                    //} while (topCard.getColor().toString().equals("wild"));

                String action = topCard.getAction().toString();
                drawingview.playCard(topCard);

                if ((action.equals("skip") || action.equals("reverse") || action.equals("plus2")) && isFirst)
                    cardPlayed(topCard);

                drawingview.invalidate();
            }
        });
    }

    public void setPlayersCount(String myPosition) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (myPosition.equals("bottom")) {
                    drawingview.setLeftPlayerCount(3);

                    if (playerNb >= 2)
                        drawingview.setTopPlayerCount(3);

                    if (playerNb == 3)
                        drawingview.setRightPlayerCount(3);
                } else if (myPosition.equals("left")) {
                    drawingview.setRightPlayerCount(3);

                    if (playerNb >= 2)
                        drawingview.setLeftPlayerCount(3);

                    if (playerNb == 3)
                        drawingview.setTopPlayerCount(3);
                } else if (myPosition.equals("top")) {
                    drawingview.setTopPlayerCount(3);
                    drawingview.setRightPlayerCount(3);

                    if (playerNb == 3)
                        drawingview.setLeftPlayerCount(3);
                } else {
                    drawingview.setTopPlayerCount(3);
                    drawingview.setRightPlayerCount(3);
                    drawingview.setLeftPlayerCount(3);
                }

                drawingview.invalidate();
            }
        });
    }

    private void makeNextPlayerDraw(int nbOfCards) {
        handler.sendMessage(Message.obtain(handler, _DRAW_CARDS_, nbOfCards));
        foreignDrawCards(4);
    }

    public void foreignCardPlayed(Card card) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                topCard = card;
                drawingview.playCard(card);
                drawingview.invalidate();
            }
        });
    }

    public Card[] getCards() {
        return adapter.getCards();
    }

    public void foreignDrawCards(int nb) {
        for (int i = 0; i < nb; i++) {
            cardDeck.drawCard();
        }
    }

    public void setTurn(boolean itsMyTurn) {
        this.itsMyTurn = itsMyTurn;
        Log.i("DEBUG", "IT S MY TURN ");
        makeToast("It's your turn !");
    }

    public void setColor(String color) {
        this.chosenColor = color;
    }

    // this method is called when a user plays a card
    // you should check if this is valid, if not you shouldn't update the drawingview and return false
    @Override
    public boolean cardPlayed(Card card) {
        String action = card.getAction().toString();
        String color = card.getColor().toString();
        int cardNb;

        boolean canPlayCard = ((card.getColor() == this.topCard.getColor()) || (action.equals(this.topCard.getAction().toString())) || (card.getColor() == Card.Color.wild) || (this.topCard.getColor().toString().equals("wild") && color.equals(chosenColor)));

        if (card.getColor().toString().equals("wild")) {
            Popup popup = new Popup();

            popup.showPopupWindow(drawingview).setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    chosenColor = popup.getChosenColor();

                    if ((action.equals("plus4") && playerNb > 1) || action.equals("color"))
                        handler.sendMessage(Message.obtain(handler, _SEND_NOTIFICATION_, "You must play a " + chosenColor + " card"));

                    handler.sendMessage(Message.obtain(handler, _SET_COLOR_, chosenColor));
                    handler.sendMessage(Message.obtain(handler, _CARD_PLAYED_, card));

                    if (action.equals("plus4"))
                        makeNextPlayerDraw(4);
                }
            });
        }

        if(!itsMyTurn && !action.equals("plus4") && !card.getColor().toString().equals("wild")) {
            Toast.makeText(getApplicationContext(), "It's not your turn !", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!canPlayCard) {
            Toast.makeText(getApplicationContext(), "Invalid card !", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (card.getAction().toString() == "plus2")
            makeNextPlayerDraw(2);

        if (!color.equals("wild"))
            handler.sendMessage(Message.obtain(handler, _CARD_PLAYED_, card));

        this.topCard = card;

        drawingview.playCard(card);
        cardNb = adapter.getItemCount();

        if (cardNb == 0) {
            roundWinner = true;
            handler.sendMessage(Message.obtain(handler, _END_ROUND_));
        }

        btnUno.setVisibility(adapter.getItemCount() < 2 ? View.VISIBLE : View.INVISIBLE);
        drawingview.invalidate();
        Log.i("DEBUG", "CARD PLAYED");

        itsMyTurn = false;
        chosenColor = null;
        return true;
    }

    public void startNewRound() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Card> cards = new ArrayList<>();
                adapter = new CardViewAdapter(getApplicationContext(), cards, GameActivity.this);
                ((RecyclerView)findViewById(R.id.playerhand)).setAdapter(adapter);
                drawCards(3);

                do {
                    topCard = cardDeck.peekTopCard();
                } while (topCard.getColor().toString().equals("wild"));
                drawingview.playCard(topCard);

                if (isFirst) {
                    Log.i("test", "is first");
                    makeToast("You start !");
                    itsMyTurn = true;
                    handler.sendMessage(Message.obtain(handler, _BROADCAST_DECK_, cardDeck));
                }
            }
        });

    }

    public void goBackToLobby() {
        startActivity(new Intent(this, MainActivity.class));
    }

    public void askToContinue(String message) {
        //change message if the method is called from the winner of from other players
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ContinueGameDialog dialog = new ContinueGameDialog();
                dialog.setMessage(message);
                dialog.setActivity(GameActivity.this);
                dialog.showNow(getSupportFragmentManager(), "DialogFragment");

                dialog.getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        Log.i("on dismiss", "continue: " + dialog.continues());
                        handler.sendMessage(Message.obtain(handler, _START_NEW_ROUND_, dialog.continues()));
                    }
                });
            }
        });
    }
}
