package com.tinslam.comic.activities;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tinslam.comic.R;
import com.tinslam.comic.UI.graphics.Images;
import com.tinslam.comic.networking.Networking;
import com.tinslam.comic.base.Game;
import com.tinslam.comic.states.MainMenuState;
import com.tinslam.comic.utils.Consts;

import java.util.Timer;
import java.util.TimerTask;

public class LoginActivity extends Activity {
    private EditText usernameBox;
    private EditText passwordBox;
    private EditText ipBox;
    private ImageButton loginButton;
    private ImageButton registerButton;
    private TextView logBox;
    private static LoginActivity loginActivity;
    private Timer timer = new Timer();
    private Timer connectionTimer = new Timer();

    @Override
    public void onPause(){
        super.onPause();

        logBox.setVisibility(View.INVISIBLE);

        try{
            timer.cancel();
            connectionTimer.cancel();
        }catch(Exception ignored){}
    }

    @Override
    public void onStop(){
        super.onStop();

        logBox.setVisibility(View.INVISIBLE);

        try{
            timer.cancel();
            connectionTimer.cancel();
        }catch(Exception ignored){}
    }

    @Override
    public void onResume(){
        super.onResume();

        runTimers();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoginActivity.loginActivity = this;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        usernameBox = (EditText) findViewById(R.id.login_panel_username_box);
        passwordBox = (EditText) findViewById(R.id.login_panel_password_box);
        ipBox = (EditText) findViewById(R.id.login_activity_ip_address_box);
        ipBox.setText(Consts.SERVER_ADDRESS);
        loginButton = (ImageButton) findViewById(R.id.login_panel_login_button);
        registerButton = (ImageButton) findViewById(R.id.login_panel_register_button);
        logBox = (TextView) findViewById(R.id.login_panel_log);

        loginButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    ((ImageButton) v).setImageBitmap(Images.button_empty_hover);
                }else{
                    ((ImageButton) v).setImageBitmap(Images.button_empty);
                }
            }
        });

        registerButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    ((ImageButton) v).setImageBitmap(Images.button_empty_hover);
                }else{
                    ((ImageButton) v).setImageBitmap(Images.button_empty);
                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Consts.SERVER_ADDRESS = ipBox.getText().toString();
//                Networking.resetIp();
                Networking.login(usernameBox.getText().toString(), passwordBox.getText().toString());
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityManager.changeActivity(loginActivity, RegisterActivity.class);
            }
        });
    }

    private void log(final String string){
        try{
            timer.cancel();
        }catch(Exception ignored){}
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logBox.setText(string);
                logBox.setVisibility(View.VISIBLE);
            }
        });
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logBox.setText("");
                        logBox.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }, Consts.TIME_TO_HIDE_LOG);
    }

    private void runTimers(){
        connectionTimer = new Timer();
        connectionTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Networking.connectFromOtherActivities();
            }
        }, 500, 500);
    }

    public void loginFailed(){
        log(Game.Context().getString(R.string.login_failed));
    }

    public void loginSuccessful(){
        ActivityManager.switchToActivity(this, MainActivity.class);
        Game.setState(new MainMenuState());
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
//        if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
//            Networking.login("tinslam33", "davari246");
//        }
//        if(event.getKeyCode() == KeyEvent.KEYCODE_SPACE){
//            Networking.login("mhell", "123456789");
//        }
        return super.dispatchKeyEvent(event);
    }

    public void noConnection(){
        log(Game.Context().getString(R.string.no_connection_to_server));
    }

    public static LoginActivity loginActivity(){
        return loginActivity;
    }
}
