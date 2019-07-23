package com.tinslam.comic.states;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.tinslam.comic.UI.graphics.Animation;
import com.tinslam.comic.UI.graphics.Images;
import com.tinslam.comic.UI.buttons.Button;
import com.tinslam.comic.gameElements.entity.Entity;
import com.tinslam.comic.networking.Networking;
import com.tinslam.comic.base.Game;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class State{
    protected Paint backGroundPaint = new Paint();
    protected ArrayList<Button> buttons = new ArrayList<>();
    protected Object buttonsLock = new Object();
    protected final Object renderLock = new Object();
    protected static boolean loading = false;
    private static Animation loadingAnimation;
    protected boolean ignoresLostConnection = false;
    private int checkForConnectionCounter = 0;
    protected float mapRightEnd = Game.getScreenWidth();
    protected float mapBottomEnd = Game.getScreenHeight();

    public State(){
        loadingAnimation = new Animation(Images.loadingAnimation, 100, 8) {
            @Override
            public void onEnd() {

            }

            @Override
            public void onCycleEnd() {

            }
        };
        loadingAnimation.pause();
    }

    public abstract void disconnected();

    public abstract void connected();

    private void loading(Canvas canvas){
        canvas.drawRect(0, 0, Game.getScreenWidth(), Game.getScreenHeight(), Game.getBlurryPaint());
        loadingAnimation.render(Game.getScreenWidth() / 2 - loadingAnimation.getWidth() / 2, Game.getScreenHeight() / 2 - loadingAnimation.getHeight() / 2, canvas);
    }

    public abstract void surfaceDestroyed();

    public abstract void handleBackPressed();

    public abstract void handleKeyEvent(KeyEvent event);

    private boolean breakDownEvent(MotionEvent event){
        switch(event.getActionMasked()){
            case MotionEvent.ACTION_DOWN :
                return onActionDown(event);

            case MotionEvent.ACTION_POINTER_DOWN :
                return onActionPointerDown(event);

            case MotionEvent.ACTION_MOVE :
                return onActionMove(event);

            case MotionEvent.ACTION_UP :
                return onActionUp(event);

            case MotionEvent.ACTION_POINTER_UP :
                return onActionPointerUp(event);
        }

        return false;
    }

    public abstract void startState();

    public abstract void tick();

    public void tickState(){
//        if(!loading){
//            tick();
//        }
        if(!ignoresLostConnection) {
            checkForConnectionCounter++;
            if (checkForConnectionCounter >= 30 * 3) {
                checkForConnectionCounter = 0;
                if (!Networking.getSocket().connected()) Game.lostConnection();
            }
        }
        tick();
    }

    public abstract void render(Canvas canvas);

    public void renderState(Canvas canvas){
//        if(!loading){
//            render(canvas);
//        }else{
//            loading(canvas);
//        }
        render(canvas);
        if(loading) loading(canvas);
    }

    public boolean onTouchEvent(MotionEvent event){
        if(loading) return true;
        return breakDownEvent(event);
    }

    public abstract boolean onActionDown(MotionEvent event);

    public abstract boolean onActionPointerDown(MotionEvent event);

    public abstract boolean onActionMove(MotionEvent event);

    public abstract boolean onActionUp(MotionEvent event);

    public abstract boolean onActionPointerUp(MotionEvent event);

    public abstract void endState();

    public void end(){
        try{
            loadingAnimation.hardStop();
        }catch(Exception egnored){}
        synchronized(Entity.getEntitiesLock()){
            for (Iterator<Entity> iterator = Entity.getEntities().iterator(); iterator.hasNext(); ) {
                Entity x = iterator.next();
                x.destroy();
            }
        }
        endState();
    }

    public ArrayList<Button> getButtons() {
        return buttons;
    }

    public Object getButtonsLock() {
        return buttonsLock;
    }

    public boolean isLoading() {
        return loading;
    }

    public static void setLoading(boolean loading) {
        if(!loading) loadingAnimation.hardStop();
        State.loading = loading;
        if(loading){
            loadingAnimation.reset();
        }
    }

    public boolean ignoresLostConnectionBoolean(){
        return ignoresLostConnection;
    }

    public Object getRenderLock() {
        return renderLock;
    }

    public float getMapRightEnd() {
        return mapRightEnd;
    }

    public void setMapRightEnd(float mapRightEnd) {
        this.mapRightEnd = mapRightEnd;
    }

    public float getMapBottomEnd() {
        return mapBottomEnd;
    }

    public void setMapBottomEnd(float mapBottomEnd) {
        this.mapBottomEnd = mapBottomEnd;
    }
}