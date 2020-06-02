package edu.vub.at.weuno;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

//Class and layout based on this article's implementation https://medium.com/@evanbishop/popupwindow-in-android-tutorial-6e5a18f49cc7
public class Popup extends PopupWindow{

    private String chosenColor;
    public static final String _GREEN_ = "green";
    public static final String _BLUE_ = "blue";
    public static final String _RED_ = "red";
    public static final String _YELLOW_ = "yellow";

    public PopupWindow showPopupWindow(final View view) {
        //Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popupview, null);

        //Specify the length and width through constants
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        //Make Inactive Items Outside Of PopupWindow
        boolean focusable = true;

        //Create a window with our parameters
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        //Set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        popupView.findViewById(R.id.greenBtn).setOnClickListener(view1 -> {
            chosenColor = _GREEN_;
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.redBtn).setOnClickListener(view1 -> {
            chosenColor = _RED_;
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.blueBtn).setOnClickListener(view1 -> {
            chosenColor = _BLUE_;
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.yellowBtn).setOnClickListener(view1 -> {
            chosenColor = _YELLOW_;
            popupWindow.dismiss();
        });

        return popupWindow;
    }

    public String getChosenColor() {
        return chosenColor;
    }

}