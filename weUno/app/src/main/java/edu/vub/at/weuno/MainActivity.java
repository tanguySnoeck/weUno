package edu.vub.at.weuno;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import edu.vub.at.IAT;
import edu.vub.at.android.util.IATAndroid;

public class MainActivity extends AppCompatActivity implements ATMessages{
    private int playerNb = 0;
    public static Handler handler;
    public static ATWeUno atWeUno;
    private SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy");
    private boolean isFirst = false;
    private int ip_value;

    private static IAT iat;
    private String myPosition;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_menu);

        ((TextView)findViewById(R.id.player_nb)).setText(playerNb + "player(s) connected");

        if (iat == null) {
            Intent i = new Intent(this, WeUnoAssetInstaller.class);
            startActivityForResult(i, _ASSET_INSTALLER_);
        }

        findViewById(R.id.start_button).setOnClickListener(v -> {
            if (playerNb < 1) {
                Toast.makeText(MainActivity.this, "You must wait for other players to join !", Toast.LENGTH_SHORT).show();
            } else {
                handler.sendMessage(Message.obtain(handler, _START_GAME_));
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v("WeUno", "Return of Asset Installer activity");
        switch (requestCode) {
            case (_ASSET_INSTALLER_):
                if (resultCode == Activity.RESULT_OK) {
                    LoopThread loopThread = new LoopThread();
                    loopThread.start();
                    handler = loopThread.handler;

                    new MainActivity.StartIATTask().execute((Void)null);
                }
                break;
        }
    }

    public Activity registerATApp(ATWeUno atwu) {
        atWeUno = atwu;
        ip_value = getIp();
        handler.sendMessage(Message.obtain(handler, _SET_IP_VALUE_, ip_value));

        return this;
    }

    private int getIp() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        int value = 0;

        for (String part : ipAddress.split("\\.")) {
            value += Integer.valueOf(part);
        }

        return value;
    }

    public void setPlayerNb(int playerNb) {
        this.playerNb = playerNb;

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.player_nb)).setText(playerNb + "player(s) connected");
            }
        });
    }

    public void setMyPosition(String myPosition) {
        this.myPosition = myPosition;
        Log.i("setmyposition", myPosition);
    }

    public void startGame() {
        Log.i("startgame", "loading new intent");
        Intent intent = new Intent(this, GameActivity.class).putExtra("IS_FIRST", isFirst).putExtra("PLAYER_NB", playerNb);
        startActivity(intent);
    }

    public void makeToast(String message) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean compareIpValues(int[] ipValues) {

        for (int value : ipValues) {
            if (ip_value < value) {
                return false;
            }
        }

        isFirst = true;
        return true;
    }

    public class StartIATTask extends AsyncTask<Void, String, Void> {

        private ProgressDialog pd;

        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            pd.setMessage(values[0]);
        }

        protected void onPreExecute() {
            super.onPreExecute();
            pd = ProgressDialog.show(MainActivity.this, "weUno", "Starting AmbientTalk");
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pd.dismiss();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                iat = IATAndroid.create(MainActivity.this);

                this.publishProgress("Loading weUno code");
                iat.evalAndPrint("import /.demo.weUno.weUno.makeWeUno()", System.err);
            } catch (Exception e) {
                Log.e("AmbientTalk", "Could not start IAT", e);
            }
            return null;
        }

    }
}
