package com.tinslam.comic.states;

import android.graphics.Canvas;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.tinslam.comic.interfaces.LoadingStateInterface;
import com.tinslam.comic.networking.Networking;
import com.tinslam.comic.base.Game;

public abstract class LoadingState extends State implements LoadingStateInterface {
    public State state = new MainMenuState();

    public LoadingState(State state){
        this.state = state;
    }

    @Override
    public void disconnected() {
        Game.lostConnection();
    }

    @Override
    public void connected() {
        action();
        Game.setState(state);
    }

    @Override
    public void surfaceDestroyed() {

    }

    @Override
    public void handleBackPressed() {

    }

    @Override
    public void handleKeyEvent(KeyEvent event) {

    }

    @Override
    public void startState() {
        if(!Networking.getSocket().connected()){
            Game.lostConnection();
        }else{
            connected();
        }
    }

    @Override
    public void tick() {

    }

    @Override
    public void render(Canvas canvas) {

    }

    @Override
    public boolean onActionDown(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onActionPointerDown(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onActionMove(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onActionUp(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onActionPointerUp(MotionEvent event) {
        return false;
    }

    @Override
    public void endState() {

    }
}
