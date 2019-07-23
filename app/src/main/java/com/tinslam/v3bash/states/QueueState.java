package com.tinslam.comic.states;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.tinslam.comic.R;
import com.tinslam.comic.UI.graphics.Animation;
import com.tinslam.comic.UI.graphics.Bug;
import com.tinslam.comic.UI.graphics.Images;
import com.tinslam.comic.UI.buttons.Button;
import com.tinslam.comic.networking.Networking;
import com.tinslam.comic.base.Game;

import java.util.ArrayList;

public class QueueState extends GameState {
    private Paint fontPaint = new Paint();
    private Paint logPaintBackground = new Paint();
    private Paint logPaintBorder = new Paint();
    private boolean isConnected = false;
    private Bug bug;
    private ArrayList<Animation> hitAnimations;
    private final Object hitAnimationsLock = new Object();

    @Override
    public void disconnected() {
        Game.lostConnection();
    }

    @Override
    public void connected() {
        isConnected = false;
    }

    @Override
    public void trophyReached() {

    }

    @Override
    public void surfaceDestroyed() {
        Networking.stopFindingMatch();
        Game.setState(new MainMenuState());
    }

    @Override
    public void handleBackPressed() {
        Networking.stopFindingMatch();
        Game.setState(new MainMenuState());
    }

    @Override
    public void handleKeyEvent(KeyEvent event) {

    }

    @Override
    public void startState(){
//        if(!Networking.getSocket().connected()) Game.lostConnection();
        logPaintBackground.setColor(Color.rgb(66, 244, 194));
        logPaintBorder.setStyle(Paint.Style.STROKE);
        logPaintBorder.setStrokeWidth(4 * Game.density());
        logPaintBorder.setColor(Color.BLACK);
        fontPaint.setTextSize(32 * Game.density());
        fontPaint.setTextAlign(Paint.Align.CENTER);
        bug = new Bug((int) (Math.random() * Game.getScreenWidth()), (int) (Math.random() * Game.getScreenHeight()), Images.brush_fill);
        hitAnimations = new ArrayList<>();
    }

    @Override
    public void tick(){
        if(!isConnected) {
            if (Networking.getSocket().connected()) {
                Networking.findMatch();
                isConnected = true;
            }
        }
        bug.tick();
    }

    @Override
    public boolean onActionDown(MotionEvent event) {
        return Button.onActionDown(event, buttons, buttonsLock);
    }

    @Override
    public boolean onActionPointerDown(MotionEvent event) {
        return Button.onActionPointerDown(event, buttons, buttonsLock);
    }

    @Override
    public boolean onActionMove(MotionEvent event) {
        return Button.onActionMove(event, buttons, buttonsLock);
    }

    @Override
    public boolean onActionUp(MotionEvent event) {
        Animation animation = new Animation(Images.bug_hit_animation, 50, 5, 1) {
            @Override
            public void onEnd() {
                synchronized(hitAnimationsLock){
                    hitAnimations.remove(this);
                }
            }

            @Override
            public void onCycleEnd() {

            }
        };
        animation.setX((int) (event.getX() - animation.getWidth() / 2));
        animation.setY((int) (event.getY() - animation.getHeight() / 2));
        synchronized(hitAnimationsLock){
            hitAnimations.add(animation);
        }
        bug.onActionUp(event);
        return Button.onActionUp(event, buttons, buttonsLock);
    }

    @Override
    public boolean onActionPointerUp(MotionEvent event) {
        Animation animation = new Animation(Images.bug_hit_animation, 50, 5, 1) {
            @Override
            public void onEnd() {
                synchronized(hitAnimationsLock){
                    hitAnimations.remove(this);
                }
            }

            @Override
            public void onCycleEnd() {

            }
        };
        animation.setX((int) (event.getX(event.getActionIndex()) - animation.getWidth() / 2));
        animation.setY((int) (event.getY(event.getActionIndex()) - animation.getHeight() / 2));
        synchronized(hitAnimationsLock){
            hitAnimations.add(animation);
        }
        bug.onActionUpPointerUp(event);
        return Button.onActionPointerUp(event, buttons, buttonsLock);
    }

    @Override
    public void render(Canvas canvas){
        canvas.drawBitmap(Images.background_grass, null, Game.getScreenRect(), null);
        Button.renderButtons(canvas, buttons, buttonsLock);
        if(!isConnected){
            int stringLength = (int) fontPaint.measureText(Game.Context().getString(R.string.attempt_to_connect) + 16 * Game.density());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.drawRoundRect(Game.getScreenWidth() / 2 - stringLength / 2, 0, Game.getScreenWidth() / 2 + stringLength / 2, fontPaint.getTextSize() * 7 / 5, 10 * Game.density(), 10 * Game.density(), logPaintBackground);
                canvas.drawRoundRect(Game.getScreenWidth() / 2 - stringLength / 2, 0, Game.getScreenWidth() / 2 + stringLength / 2, fontPaint.getTextSize() * 7 / 5, 10 * Game.density(), 10 * Game.density(), logPaintBorder);
            }else{
                canvas.drawRect(Game.getScreenWidth() / 2 - stringLength / 2, 0, Game.getScreenWidth() / 2 + stringLength / 2, fontPaint.getTextSize() * 7 / 5, logPaintBackground);
                canvas.drawRect(Game.getScreenWidth() / 2 - stringLength / 2, 0, Game.getScreenWidth() / 2 + stringLength / 2, fontPaint.getTextSize() * 7 / 5, logPaintBorder);
            }
            canvas.drawText(Game.Context().getString(R.string.attempt_to_connect), Game.getScreenWidth() / 2, fontPaint.getTextSize(), fontPaint);
        }else{
            int stringLength = (int) fontPaint.measureText(Game.Context().getString(R.string.attempt_to_connect_successful) + 16 * Game.density());
            int stringLength2 = (int) fontPaint.measureText(Game.Context().getString(R.string.waiting_for_players) + 16 * Game.density());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.drawRoundRect(Game.getScreenWidth() / 2 - stringLength2 / 2, fontPaint.getTextSize() * 7 / 5 - 10 * Game.density(), Game.getScreenWidth() / 2 + stringLength2 / 2, fontPaint.getTextSize() * 7 / 5 * 2, 10 * Game.density(), 10 * Game.density(), logPaintBorder);
                canvas.drawRoundRect(Game.getScreenWidth() / 2 - stringLength2 / 2, fontPaint.getTextSize() * 7 / 5 - 10 * Game.density(), Game.getScreenWidth() / 2 + stringLength2 / 2, fontPaint.getTextSize() * 7 / 5 * 2, 10 * Game.density(), 10 * Game.density(), logPaintBackground);
                canvas.drawRoundRect(Game.getScreenWidth() / 2 - stringLength / 2,  - 10 * Game.density(), Game.getScreenWidth() / 2 + stringLength / 2, fontPaint.getTextSize() * 7 / 5, 10 * Game.density(), 10 * Game.density(), logPaintBorder);
                canvas.drawRoundRect(Game.getScreenWidth() / 2 - stringLength / 2,  - 10 * Game.density(), Game.getScreenWidth() / 2 + stringLength / 2, fontPaint.getTextSize() * 7 / 5, 10 * Game.density(), 10 * Game.density(), logPaintBackground);
                canvas.drawRect(Game.getScreenWidth() / 2 - stringLength2 / 2, fontPaint.getTextSize() * 7 / 5 - 10 * Game.density(), Game.getScreenWidth() / 2 + stringLength2 / 2, fontPaint.getTextSize() * 7 / 5 + 4 * Game.density(), logPaintBackground);
            }else{
                canvas.drawRect(Game.getScreenWidth() / 2 - stringLength / 2, 0, Game.getScreenWidth() / 2 + stringLength / 2, fontPaint.getTextSize() * 7 / 5, logPaintBackground);
                canvas.drawRect(Game.getScreenWidth() / 2 - stringLength / 2, 0, Game.getScreenWidth() / 2 + stringLength / 2, fontPaint.getTextSize() * 7 / 5, logPaintBorder);
                canvas.drawRect(Game.getScreenWidth() / 2 - stringLength2 / 2, fontPaint.getTextSize() * 7 / 5, Game.getScreenWidth() / 2 + stringLength2 / 2, fontPaint.getTextSize() * 7 / 5 * 2, logPaintBackground);
                canvas.drawRect(Game.getScreenWidth() / 2 - stringLength2 / 2, fontPaint.getTextSize() * 7 / 5, Game.getScreenWidth() / 2 + stringLength2 / 2, fontPaint.getTextSize() * 7 / 5 * 2, logPaintBorder);
                canvas.drawRect(Game.getScreenWidth() / 2 - stringLength2 / 2 + 2 * Game.density(), fontPaint.getTextSize() * 7 / 5 - 10 * Game.density(), Game.getScreenWidth() / 2 + stringLength2 / 2 - 2 * Game.density(), fontPaint.getTextSize() * 7 / 5 + 4 * Game.density(), logPaintBackground);
            }
            canvas.drawText(Game.Context().getString(R.string.attempt_to_connect_successful), Game.getScreenWidth() / 2, fontPaint.getTextSize(), fontPaint);
            canvas.drawText(Game.Context().getString(R.string.waiting_for_players), Game.getScreenWidth() / 2, fontPaint.getTextSize() * 7 / 5 + fontPaint.getTextSize(), fontPaint);
        }
        synchronized(hitAnimationsLock){
            for(Animation x : hitAnimations){
                x.render(canvas);
            }
        }
        bug.render(canvas);
    }

    @Override
    public void endState() {
        buttons.clear();
    }
}
