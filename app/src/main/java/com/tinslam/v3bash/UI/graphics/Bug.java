package com.tinslam.comic.UI.graphics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.view.MotionEvent;

import com.tinslam.comic.base.Game;
import com.tinslam.comic.utils.Utils;

public class Bug{
    private float angle = (float) Math.toRadians(90);
    private float x, y;
    private Animation animation;
    private int targetCD = 30;
    private int cd = 0;
    private float x2 = 0, y2 = 0;
    private Matrix matrix;
    private Rect rect = new Rect();
    private float speed = 10 * Game.density();

    public Bug(int x, int y, Bitmap image){
        this.x = x;
        this.y = y;
        matrix = new Matrix();
        animation = new Animation(Images.bug, 50, 5) {
            @Override
            public void onEnd() {

            }

            @Override
            public void onCycleEnd() {

            }
        };
        getNewTarget();
//        this.x = 200;
//        this.y = 800;
        matrix.preTranslate(this.x, this.y);
        rect.set(x, y, x + animation.getWidth(), y + animation.getHeight());
//        x2 = 800;
//        y2 = 200;
    }

    public void tick(){
        if(Utils.distance((int) x, (int) y, (int) x2, (int) y2) < 30){
            cd = 0;
            getNewTarget();
        }
        cd++;
        if(cd >= targetCD){
            cd = 0;
            getNewTarget();
        }
        moveTowardsTarget();
    }

    private void moveTowardsTarget(){
        float ang = (float) Math.atan2((float) y2 - y, (float) x2 - x);
        float c = (float) (Math.abs(Math.cos(ang) * speed));
        float s = (float) (Math.abs(Math.sin(ang) * speed));
        ang = (float) ((float) Math.atan2(y2 - y, x2 - x) + Math.PI);
        matrix.preRotate((float) Math.toDegrees(-angle), animation.getWidth() / 2, animation.getHeight() / 2);
        if(Math.abs(Math.toDegrees(ang) % 360 - Math.toDegrees(angle) % 360) > 20){
            if(Math.toDegrees(ang) % 360 > Math.toDegrees(angle) % 360){
                if(Math.abs(Math.toDegrees(ang) % 360 - Math.toDegrees(angle) * 360) <= 180){
                    matrix.preRotate((float) (Math.toDegrees(angle) % 360 - 10), animation.getWidth() / 2, animation.getHeight() / 2);
                    angle-= Math.toRadians(10) % 360;
                }else{
                    matrix.preRotate((float) (Math.toDegrees(angle) % 360 + 10), animation.getWidth() / 2, animation.getHeight() / 2);
                    angle+= Math.toRadians(10) % 360;
                }
            }else{
                if(Math.abs(Math.toDegrees(ang) % 360 - Math.toDegrees(angle) * 360) <= 180){
                    matrix.preRotate((float) (Math.toDegrees(angle) % 360 + 10), animation.getWidth() / 2, animation.getHeight() / 2);
                    angle+= Math.toRadians(10) % 360;
                }else{
                    matrix.preRotate((float) (Math.toDegrees(angle) % 360 - 10), animation.getWidth() / 2, animation.getHeight() / 2);
                    angle-= Math.toRadians(10) % 360;
                }
            }
        }else{
            matrix.preRotate((float) Math.toDegrees(ang), animation.getWidth() / 2, animation.getHeight() / 2);
            angle = ang;
        }
        if(y2 < y){
            y -= s;
            matrix.postTranslate(0, 0 - s);
        }else{
            y += s;
            matrix.postTranslate(0, 0 + s);
        }
        if(x2 > x){
            x += c;
            matrix.postTranslate(0 + c, 0);
        }else{
            x -= c;
            matrix.postTranslate(0 - c, 0);
        }
//        matrix.postRotate(ang);
        // Adjust rect
    }

    private void getNewTarget(){
        x2 = (int) (Math.random() * Game.getScreenWidth()) - animation.getWidth() / 2;
        y2 = (int) (Math.random() * Game.getScreenHeight()) - animation.getHeight() / 2;
    }

    private void clicked(){

    }

    public void render(Canvas canvas){
        animation.render(matrix, canvas);
//        canvas.drawRect(x, y, x + 16, y + 16, new Paint());
//        canvas.drawRect(x2, y2, x2 + 16, y2 + 16, new Paint());
    }

    public void onActionUp(MotionEvent event) {
        int mx = (int) event.getX();
        int my = (int) event.getY();

        if(rect.contains(mx, my)){
            clicked();
        }
    }

    public void onActionUpPointerUp(MotionEvent event) {
        int mx = (int) event.getX(event.getActionIndex());
        int my = (int) event.getY(event.getActionIndex());

        if(rect.contains(mx, my)){
            clicked();
        }
    }
}
