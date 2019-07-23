package com.tinslam.comic.activities;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tinslam.comic.R;
import com.tinslam.comic.UI.graphics.Images;
import com.tinslam.comic.base.Game;
import com.tinslam.comic.networking.Networking;
import com.tinslam.comic.utils.Consts;

import java.util.Timer;
import java.util.TimerTask;

public class RegisterActivity extends Activity {
    private EditText usernameBox;
    private EditText passwordBox, password2Box;
    private EditText emailBox, email2Box;
    private EditText verifyBox;
    private ImageButton registerButton;
    private TextView verifyBoxLabel;
    private TextView logBox;
    private TextView verifylabel;
    private ImageButton verifyButton;
    private static RegisterActivity registerActivity;
    private Timer timer = new Timer();
    private Timer clipboardTimer = new Timer();
    private Timer connectionTimer = new Timer();
    private String verifyCode = "Brave Shine";
    private boolean resumeVerifyTimer = false;

    @Override
    public void onPause(){
        super.onPause();

        try{
            timer.cancel();
            clipboardTimer.cancel();
            connectionTimer.cancel();
        }catch(Exception ignored){}
    }

    @Override
    public void onStop(){
        super.onStop();

        try{
            timer.cancel();
            clipboardTimer.cancel();
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
        RegisterActivity.registerActivity = this;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        usernameBox = (EditText) findViewById(R.id.register_panel_username_box);
        passwordBox = (EditText) findViewById(R.id.register_panel_password_box);
        password2Box = (EditText) findViewById(R.id.register_panel_password2_box);
        emailBox = (EditText) findViewById(R.id.register_panel_email_box);
        email2Box = (EditText) findViewById(R.id.register_panel_email2_box);
        verifyBox = (EditText) findViewById(R.id.register_panel_verify_box);
        registerButton = (ImageButton) findViewById(R.id.register_panel_register_button);
        verifyBoxLabel = (TextView) findViewById(R.id.register_panel_verify_label);
        logBox = (TextView) findViewById(R.id.register_panel_log);
        verifylabel = (TextView) findViewById(R.id.register_panel_verify_button_label);
        verifyButton = (ImageButton) findViewById(R.id.register_panel_verify_button);

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

        verifyButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    ((ImageButton) v).setImageBitmap(Images.button_empty_hover);
                }else{
                    ((ImageButton) v).setImageBitmap(Images.button_empty);
                }
            }
        });

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Networking.verify(verifyBox.getText().toString());
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkFormValidity()){
                    Networking.register(usernameBox.getText().toString(), passwordBox.getText().toString(), emailBox.getText().toString());

                }
            }
        });
    }

    private boolean checkFormValidity(){
        String username = usernameBox.getText().toString();
        String password = passwordBox.getText().toString();
        String password2 = password2Box.getText().toString();
        String email = emailBox.getText().toString();
        String email2 = email2Box.getText().toString();

        if(username.length() < Consts.MIN_CHARS_USERNAME){
            log(Game.Context().getString(R.string.min_chars_username));
            return false;
        }

        if(username.length() > Consts.MAX_CHARS_USERNAME){
            log(Game.Context().getString(R.string.username_max_chars_reached));
            return false;
        }

        if(password.length() < Consts.MIN_CHARS_PASSWORD){
            log(Game.Context().getString(R.string.min_chars_password));
            return false;
        }

        if(password.length() > Consts.MAX_CHARS_PASSWORD){
            log(Game.Context().getString(R.string.password_max_chars_reached));
            return false;
        }

        if(email.length() > Consts.MAX_CHARS_EMAIL){
            log(Game.Context().getString(R.string.email_max_chars_reached));
            return false;
        }

        for(int i = 0; i < username.length(); i++){
            if(!charAllowedInUsername(username.charAt(i))){
                log(getString(R.string.username_invalid_character_p1) + "" + username.charAt(i) + "" + getString(R.string.username_invalid_character_p2));
                return false;
            }
        }

        for(int i = 0; i < password.length(); i++){
            if(!charAllowedInPassword(password.charAt(i))){
                log(getString(R.string.password_invalid_character_p1) + "" + username.charAt(i) + "" + getString(R.string.password_invalid_character_p2));
                return false;
            }
        }

        if(!password.equals(password2)){
            log(Game.Context().getString(R.string.passwords_mismatch));
            return false;
        }

        if(!email.equals(email2)){
            log(Game.Context().getString(R.string.emails_mismatch));
            return false;
        }

        return true;
    }

    private void runTimers(){
        if(resumeVerifyTimer){
            clipboardTimer = new Timer();
            clipboardTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    checkClipboard();
                }
            }, 500, 500);
        }

        connectionTimer = new Timer();
        connectionTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Networking.connectFromOtherActivities();
            }
        }, 3000, 3000);
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

    private boolean charAllowedInPassword(int code){
        return code >= 33 && code <= 126;
    }

    private boolean charAllowedInUsername(int code){
        return (code >= 48 && code <= 57) ||
                (code >= 65 && code <= 90) ||
                (code >= 97 && code <= 122) ||
                code == 95;
    }

    public void checkClipboard(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    String text = clipboard.getPrimaryClip().getItemAt(0).getText().toString();
                    if (text.equals(verifyCode)) {
                        Networking.verify(verifyCode);
                        clipboardTimer.cancel();
                    }
                }catch(Exception ignored){}
            }
        });
    }

    public void registerFailed(){
        log(Game.Context().getString(R.string.register_failed));
    }

    public void verify(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try{
                    clipboardTimer.cancel();
                }catch(Exception egnored){}
                clipboardTimer = new Timer();
                clipboardTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        checkClipboard();
                    }
                }, 500, 500);
                resumeVerifyTimer = true;
                verifylabel.setVisibility(View.VISIBLE);
                verifyButton.setVisibility(View.VISIBLE);
                verifyBox.setVisibility(View.VISIBLE);
                verifyBoxLabel.setVisibility(View.VISIBLE);
            }
        });
        log(Game.Context().getString(R.string.verify));
    }

    public void registerSuccessful(){
        log(Game.Context().getString(R.string.register_successful));
    }

    public void noConnection(){
        log(Game.Context().getString(R.string.no_connection_to_server));
    }

    public void verifyFailed(){
        log(Game.Context().getString(R.string.verify_failed));
    }

    public void verifyCodeInvalid() {
        log(Game.Context().getString(R.string.verify_code_invalid));
    }

    public void setVerifyCode(String verifyCode){
        this.verifyCode = verifyCode;
    }

    public static RegisterActivity registerActivity(){
        return registerActivity;
    }
}