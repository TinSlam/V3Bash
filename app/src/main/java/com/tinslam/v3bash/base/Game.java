package com.tinslam.comic.base;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.tinslam.comic.R;
import com.tinslam.comic.UI.graphics.Images;
import com.tinslam.comic.activities.LoginActivity;
import com.tinslam.comic.activities.MainActivity;
import com.tinslam.comic.activities.RegisterActivity;
import com.tinslam.comic.modes.conquer.ConquerGameState;
import com.tinslam.comic.modes.hideAndSeek.HideAndSeekGameState;
import com.tinslam.comic.modes.maze.MazeGameState;
import com.tinslam.comic.networking.Networking;
import com.tinslam.comic.networking.UdpNetworking;
import com.tinslam.comic.states.MainMenuState;
import com.tinslam.comic.states.RankingState;
import com.tinslam.comic.states.State;

import java.util.Locale;

public class Game extends SurfaceView implements SurfaceHolder.Callback{
    private GameThread thread;
    private static String language = "en";
    private static boolean keyboardShown = false;
    private static Context context;
    private static State state;
    private static int width, height;
    private static Game game = null;
    private static Paint borderPaint = new Paint();
    private static Paint backgroundPaint = new Paint();
    private static Paint fontPaint = new Paint();
    private static boolean noConnection = false, dontShowLostConnection = false;
    private static Paint blurryPaint = new Paint();
    private static boolean lostConnectionFlag = false;
    private static String username = "", password = "";
    private static Rect screenRect;

    public Game(Context context, int width, int height){
        super(context);

        Game.context = context;

        Game.width = width;
        Game.height = height;

        screenRect = new Rect(0, 0, Game.getScreenWidth(), Game.getScreenHeight());

        backgroundPaint.setColor(Color.WHITE);
        borderPaint.setColor(Color.argb(255, 255, 127, 127));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4 * Game.density());
        blurryPaint.setARGB(127, 0, 0, 0);
        fontPaint.setTextSize(24 * Game.density());
        fontPaint.setColor(Color.BLACK);
        fontPaint.setTextAlign(Paint.Align.CENTER);

        getHolder().addCallback(this);

        setFocusable(true);
        thread = new GameThread(getHolder(), this);
        Game.changeLanguage("en");
        setKeepScreenOn(true);
    }

    public static Game game(){
        return game;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (game == null) {
            game = this;
            Images.loadImages();
            state = new MainMenuState();
            state.startState();
            thread.setRunning(true);
            thread.start();
            keyboardShowListener();
        } else {
            keyboardShowListener();
            thread.setRunning(true);
            GameThread.resumeThread();
        }
    }

    public void handleBackPressed(){
        state.handleBackPressed();
    }

    public static void keyboardShowListener(){
        game.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                game.getWindowVisibleDisplayFrame(r);
                int screenHeight = game.getRootView().getHeight();

                // r.bottom is the position above soft keypad or device button.
                // if keypad is shown, the r.bottom is smaller than that before.
                int keypadHeight = screenHeight - r.bottom;

                // 0.15 ratio is perhaps enough to determine keypad height.
                keyboardShown = keypadHeight > screenHeight * 0.15;
            }
        });
    }

    public static void lostConnection(){
        if(noConnection) return;
        if(Networking.start()) return;
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        (new Thread(new Runnable() {
            @Override
            public void run(){
                while(!Networking.start()){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(lostConnectionFlag || state.ignoresLostConnectionBoolean()){
                        lostConnectionFlag = false;
                        break;
                    }
                }
                noConnection = false;
//                GameThread.resumeThread();
            }
        })).start();
//        GameThread.pauseThread();
        noConnection = true;
    }

    public static void lostConnectionFlag(){
        if(!noConnection) return;
        lostConnectionFlag = true;
    }

    public void handleKeyEvent(KeyEvent event){
        state.handleKeyEvent(event);
    }

    public static void showKeyboard(){
        if(!keyboardShown){
            game.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    keyboardShowListener();
                }
            }, 100);
        }
    }

    public static void hideKeyboard(){
        if(keyboardShown){
            Game.game().postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) Game.Context().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    keyboardShowListener();
                    MainActivity.goImmersive();
                }
            }, 100);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        state.surfaceDestroyed();
        hideKeyboard();
        GameThread.pauseThread();
//        boolean retry = true;
//        while(retry){
//            try{
//                thread.setRunning(false);
//                thread.join();
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//            retry = false;
//        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(!noConnection) state.onTouchEvent(event);
        return true;
    }

    public void update(){
        state.tickState();
    }

    public void draw(Canvas canvas){
        try {
            super.draw(canvas);

            canvas.drawColor(Color.WHITE);

            state.renderState(canvas);

            if (noConnection) drawPaused(canvas);
        }catch (Exception ignored){}
    }

    public void drawPaused(Canvas canvas){
        canvas.drawRect(0, 0, Game.getScreenWidth(), Game.getScreenHeight(), blurryPaint);
        if(dontShowLostConnection) return;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            canvas.drawRoundRect((getScreenWidth() - 256 * Game.density()) / 2, (getScreenHeight() - 64 * Game.density()) / 2, (getScreenWidth() - 256 * Game.density()) / 2 + 256 * Game.density(), (getScreenHeight() - 64 * Game.density()) / 2 + 64 * Game.density(), 10 * Game.density(), 10 * Game.density(), backgroundPaint);
            canvas.drawRoundRect((getScreenWidth() - 256 * Game.density()) / 2, (getScreenHeight() - 64 * Game.density()) / 2, (getScreenWidth() - 256 * Game.density()) / 2 + 256 * Game.density(), (getScreenHeight() - 64 * Game.density()) / 2 + 64 * Game.density(), 10 * Game.density(), 10 * Game.density(), borderPaint);
        }else{
            canvas.drawRect((getScreenWidth() - 256 * Game.density()) / 2, (getScreenHeight() - 64 * Game.density()) / 2, (getScreenWidth() - 256 * Game.density()) / 2 + 256 * Game.density(), (getScreenHeight() - 64 * Game.density()) / 2 + 64 * Game.density(), backgroundPaint);
            canvas.drawRect((getScreenWidth() - 256 * Game.density()) / 2, (getScreenHeight() - 64 * Game.density()) / 2, (getScreenWidth() - 256 * Game.density()) / 2 + 256 * Game.density(), (getScreenHeight() - 64 * Game.density()) / 2 + 64 * Game.density(), borderPaint);
        }
        canvas.drawText(context.getString(R.string.no_connection_to_server), (getScreenWidth()) / 2, (getScreenHeight()) / 2 + 8 * Game.density(), fontPaint);
    }

    public static void changeLanguage(String str){
        try{
            Resources res = context.getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                conf.setLocale(new Locale(str));
            }else{
                conf.locale = new Locale(str);
            }
    // Use conf.locale = new Locale(...) if targeting lower versions
            res.updateConfiguration(conf, dm);
            language = str;
            setState(new MainMenuState());
//
//            res = RegisterActivity.registerActivity().getResources();
//            dm = res.getDisplayMetrics();
//            conf = res.getConfiguration();
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                conf.setLocale(new Locale(str));
//            }else{
//                conf.locale = new Locale(str);
//            }
//    // Use conf.locale = new Locale(...) if targeting lower versions
//            res.updateConfiguration(conf, dm);
//
//            res = LoginActivity.loginActivity().getResources();
//            dm = res.getDisplayMetrics();
//            conf = res.getConfiguration();
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                conf.setLocale(new Locale(str));
//            }else{
//                conf.locale = new Locale(str);
//            }
//    // Use conf.locale = new Locale(...) if targeting lower versions
//            res.updateConfiguration(conf, dm);
        }catch(Exception ignored){}
    }

    public static void closeApp(){
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        context.startActivity(intent);
    }

    public static Context Context() {
        return context;
    }

    public static float density(){ return Game.Context().getResources().getDisplayMetrics().density; }

    public static int getScreenWidth() {
        return width;
    }

    public static int getScreenHeight() {
        return height;
    }

    public static State getState() {
        return state;
    }

    public static void setState(State state) {
        Game.state.end();
        Game.state = state;
        Game.state.startState();
    }

    public static boolean isKeyboardShown() {
        return keyboardShown;
    }

    public static Paint getBlurryPaint() {
        return blurryPaint;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        Game.username = username;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        Game.password = password;
    }

    public static Rect getScreenRect(){
        return screenRect;
    }

    public static String getLanguage() {
        return language;
    }
}
