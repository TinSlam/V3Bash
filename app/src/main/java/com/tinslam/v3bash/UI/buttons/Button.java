package com.tinslam.comic.UI.buttons;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

import com.tinslam.comic.base.Game;
import com.tinslam.comic.interfaces.ButtonInterface;

import java.util.ArrayList;

public abstract class Button implements ButtonInterface {
    protected Bitmap image, imageOnClick, currentImage;
    protected boolean active, manualRender;
    protected int pointerId = -1;
    protected Paint paint = new Paint();
    private static Object buttonsLock = new Object();
    private static ArrayList<Button> buttons = new ArrayList<>();

    public Button(Bitmap image, Bitmap imageOnClick, boolean manualRender){
        this.image = image;
        this.imageOnClick = imageOnClick;
        this.manualRender = manualRender;
        currentImage = image;

        synchronized(buttonsLock){
            buttons.add(this);
        }
        synchronized(Game.getState().getButtonsLock()){
            Game.getState().getButtons().add(this);
        }
        setActive(true);
    }

    public static void renderButtons(Canvas canvas, ArrayList<Button> stateButtons, Object lock){
        synchronized(lock) {
            for (Button x : stateButtons) {
                if(!x.manualRender) x.render(canvas);
            }
        }
    }

    public abstract void render(Canvas canvas);

    public static boolean onActionDown(MotionEvent event, ArrayList<Button> stateButtons, Object lock){
        int mx = (int) event.getX(event.getActionIndex());
        int my = (int) event.getY(event.getActionIndex());

        synchronized(lock) {
            for (Button x : stateButtons) {
                if (x.isTouched(mx, my)){
                    if(x.onDownDefault()){
                        x.pointerId = event.getPointerId(event.getActionIndex());
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean onActionPointerDown(MotionEvent event, ArrayList<Button> stateButtons, Object lock){
        int mx = (int) event.getX(event.getActionIndex());
        int my = (int) event.getY(event.getActionIndex());

        synchronized (lock) {
            for (Button x : stateButtons) {
                if (x.isTouched(mx, my)) {
                    if(x.onDownDefault()) {
                        x.pointerId = event.getPointerId(event.getActionIndex());
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean onActionMove(MotionEvent event, ArrayList<Button> stateButtons, Object lock){
        synchronized (lock) {
            for (Button x : stateButtons) {
                if(x.pointerId != -1){
                    int index = -1;
                    for(int i = 0; i < 5; i++){
                        if(event.getPointerId(i) == x.pointerId){
                            index = i;
                            break;
                        }
                    }
                    int mx = (int) event.getX(index);
                    int my = (int) event.getY(index);
                    if(x.isTouched(mx, my)){
                        x.setCurrentImage(x.getImageOnClick());
                    }else{
                        x.setCurrentImage(x.getImage());
                    }
                }
            }
        }

        return false;
    }

    public static boolean onActionUp(MotionEvent event, ArrayList<Button> stateButtons, Object lock){
        int mx = (int) event.getX(event.getActionIndex());
        int my = (int) event.getY(event.getActionIndex());

        synchronized (lock) {
            for (Button x : stateButtons) {
                if(x.pointerId == event.getPointerId(event.getActionIndex())) {
                    x.pointerId = -1;
                    x.currentImage = x.image;
                    if (x.isTouched(mx, my)) {
                        if(x.onUp()) {
                            return true;
                        }
                    }
                    x.setCurrentImage(x.getImage());
                }
            }
        }

        return false;
    }

    public static boolean onActionPointerUp(MotionEvent event, ArrayList<Button> stateButtons, Object lock){
        int mx = (int) event.getX(event.getActionIndex());
        int my = (int) event.getY(event.getActionIndex());

        synchronized(lock){
            for (Button x : stateButtons) {
                if(x.pointerId == event.getPointerId(event.getActionIndex())) {
                    x.pointerId = -1;
                    x.currentImage = x.image;
                    if (x.isTouched(mx, my)) {
                        if(x.onUp()) {
                            return true;
                        }
                    }
                    x.setCurrentImage(x.getImage());
                }
            }
        }

        return false;
    }

    public boolean onDownDefault(){
        setCurrentImage(imageOnClick);
        return onDown();
    }

    public abstract boolean onDown();

    public abstract boolean onUp();

    public abstract boolean isTouched(int mx, int my);

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public Bitmap getImageOnClick() {
        return imageOnClick;
    }

    public void setImageOnClick(Bitmap imageOnClick) {
        this.imageOnClick = imageOnClick;
    }

    public Bitmap getCurrentImage() {
        return currentImage;
    }

    public void setCurrentImage(Bitmap currentImage) {
        this.currentImage = currentImage;
    }
}
