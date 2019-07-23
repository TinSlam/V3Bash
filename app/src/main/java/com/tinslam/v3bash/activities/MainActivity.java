package com.tinslam.comic.activities;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.tinslam.comic.base.Game;

public class MainActivity extends Activity {
    private static Object lock = new Object();
    private static boolean show;
    private static MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.mainActivity = this;

        mainActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mainActivity.requestWindowFeature(Window.FEATURE_NO_TITLE);

        goImmersive();

        Display d = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        final Point size = new Point();
        d.getSize(size);

//        if (Build.VERSION.SDK_INT >= 17) {
//            d.getRealSize(size);
//        } else if (Build.VERSION.SDK_INT >= 14) {
//            try {
//                size.x = (Integer) Display.class.getMethod("getRawWidth").invoke(d);
//                size.y = (Integer) Display.class.getMethod("getRawHeight").invoke(d);
//            } catch (IllegalAccessException e) {} catch (InvocationTargetException e) {} catch (NoSuchMethodException e) {}
//        }

        setContentView(new Game(mainActivity, size.x, size.y));
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    }

    public static void goImmersive(){
//        mainActivity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                    int UI_OPTIONS = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
//                    mainActivity.getWindow().getDecorView().setSystemUiVisibility(UI_OPTIONS);
//                }
//            }
//        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch(event.getAction()){
            case KeyEvent.ACTION_UP :
                try{
                    Game.game().handleKeyEvent(event);
                }catch(Exception e){

                }
                break;
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        MainActivity.goImmersive();
        Game.game().handleBackPressed();
    }

    public static MainActivity mainActivity(){
        return mainActivity;
    }
}