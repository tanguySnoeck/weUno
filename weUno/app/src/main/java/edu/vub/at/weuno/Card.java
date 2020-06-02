package edu.vub.at.weuno;

import java.lang.reflect.Field;

public class Card{

    public static enum Action {
        a0, a1, a2, a3, a4, a5, a6, a7, a8, a9,
        skip, reverse, plus2, plus4, color
    }

    public static enum Color {
        yellow, red, blue, green, wild
    }


    private Color color;
    private Action action;

    private int id = 0;

    public Card(Color color, Action action) {
        this.color = color;
        this.action = action;
    }

    public Card(Card c) {
        this.color = c.getColor();
        this.action = c.getAction();
    }

    public int getResourceId() {
        if (id != 0)
            return id;

        try {
            Class d = R.drawable.class;
            Field en = d.getDeclaredField(this.toString());
            id = en.getInt(null);

            return id;
        } catch (Exception e) {
            return R.drawable.weuno;
        }
    }

    @Override
    public String toString() {
        return color.toString() + "_" + action.toString();
    }


    public Color getColor() {
        return color;
    }

    public Action getAction() {
        return action;
    }
}
