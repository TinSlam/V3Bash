package com.tinslam.comic.modes.conquer;

import android.graphics.Canvas;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.tinslam.comic.UI.graphics.Images;
import com.tinslam.comic.base.Game;
import com.tinslam.comic.states.State;

public class ConquerWaitingState extends State {
    @Override
    public void disconnected() {

    }

    @Override
    public void connected() {

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
        setLoading(true);
    }

    @Override
    public void tick() {

    }

    @Override
    public void render(Canvas canvas) {
        canvas.drawBitmap(Images.conquer_background_grass, null, Game.getScreenRect(), null);
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
