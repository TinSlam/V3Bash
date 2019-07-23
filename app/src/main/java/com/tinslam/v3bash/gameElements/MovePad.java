package com.tinslam.comic.gameElements;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

import com.tinslam.comic.UI.graphics.Images;
import com.tinslam.comic.base.Game;
import com.tinslam.comic.gameElements.entity.movingEntity.MovingEntity;

public class MovePad{
    private MovingEntity entity;
    private Bitmap imageBig = Images.move_pad_big, imageSmall = Images.move_pad_small;
    private int x = (int) (8 * Game.density()), y = (int) (Game.getScreenHeight() - 8 * Game.density() - imageBig.getHeight());
    private Paint paint = new Paint();
    private int smallX = x  + (imageBig.getWidth() - imageSmall.getWidth()) / 2, smallY = y + (imageBig.getHeight() - imageSmall.getHeight()) / 2;
    private int xc = x + imageBig.getWidth() / 2, yc = y + imageBig.getHeight() / 2;
    private int radius = imageBig.getWidth() / 2;
    private int pointerId = -1;
    private boolean touchingMovePad = false;

    public MovePad(MovingEntity entity){
        this.entity = entity;
    }

    public void render(Canvas canvas){
        canvas.drawBitmap(imageBig, x, y, paint);
        canvas.drawBitmap(imageSmall, smallX, smallY, paint);
    }

    public void move(float mx, float my){
        smallX = (int) mx - imageSmall.getWidth() / 2;
        smallY = (int) my - imageSmall.getWidth() / 2;
    }

    public void move(){
        if(!touchingMovePad) return;
        float tx = (smallX - x  - (float) (imageBig.getWidth() - imageSmall.getWidth()) / 2) / radius * entity.getSpeed();
        float ty = (smallY - y  - (float) (imageBig.getHeight() - imageSmall.getHeight()) / 2) / radius * entity.getSpeed();
        float angle = (float) Math.toDegrees(Math.atan2(ty, tx));
        entity.setRotation(angle + 90);
//        if(tx > 0 && Math.abs(tx) > Math.abs(ty)){
//            entity.setAnimation(entity.getAnimationRight());
//        }else if(tx < 0 && Math.abs(tx) > Math.abs(ty)){
//            entity.setAnimation(entity.getAnimationLeft());
//        }else if(ty > 0 && Math.abs(tx) < Math.abs(ty)){
//            entity.setAnimation(entity.getAnimationDown());
//        }else if(ty < 0 && Math.abs(tx) < Math.abs(ty)){
//            entity.setAnimation(entity.getAnimationUp());
//        }

//        if(tx > 0) if(Math.abs(ty) <= Math.abs(tx)) entity.setDirection(Consts.DIRECTION_RIGHT);
//        if(tx < 0) if(Math.abs(ty) <= Math.abs(tx)) entity.setDirection(Consts.DIRECTION_LEFT);
//        if(ty > 0) if(Math.abs(ty) > Math.abs(tx)) entity.setDirection(Consts.DIRECTION_DOWN);
//        if(ty < 0) if(Math.abs(ty) > Math.abs(tx)) entity.setDirection(Consts.DIRECTION_UP);

        for(int i = (int) Math.abs(tx); i > 0; i--) {
            if(tx > 0) {
                entity.move(i, 0);
            }else{
                entity.move(-i, 0);
            }
        }
        for(int i = (int) Math.abs(ty); i > 0; i--) {
            if(ty > 0) {
                entity.move(0, i);
            }else{
                entity.move(0, -i);
            }
        }
    }

    public void processMoving(float mx, float my){
        if(Math.sqrt(Math.pow(mx - xc, 2) + Math.pow(my - yc, 2)) <= radius){
            move(mx, my);
        }else{
            int tx, ty;
            if(mx > xc) tx = (int) (radius / Math.sqrt(1 + Math.pow((my - yc) / (mx - xc), 2)) + xc);
            else tx = -(int) (radius / Math.sqrt(1 + Math.pow((my - yc) / (mx - xc), 2)) - xc);
            if(my > yc) ty = (int) (Math.sqrt(Math.pow(radius, 2) - Math.pow(tx - xc, 2)) + yc);
            else ty = -(int) (Math.sqrt(Math.pow(radius, 2) - Math.pow(tx - xc, 2)) - yc);
            move(tx, ty);
        }
    }

    public boolean isTouched(float mx, float my){
        if(Math.sqrt(Math.pow(mx - xc, 2) + Math.pow(my - yc, 2)) <= radius){
            return true;
        }

        return false;
    }

    public void reset(){
        smallX = x  + (imageBig.getWidth() - imageSmall.getHeight()) / 2;
        smallY = y + (imageBig.getHeight() - imageSmall.getHeight()) / 2;

//        if(entity.getAnimation() != null) if(entity.getAnimation().getType() == Consts.ANIMATION_MOVE) entity.setAnimation(null);
    }

    public void actionPointerDown(MotionEvent event){
        if(!touchingMovePad){
            touchingMovePad = true;
            move(event.getX(event.getPointerId(event.getActionIndex())), event.getY(event.getPointerId(event.getActionIndex())));
            setPointerId(event.getPointerId(event.getActionIndex()));
        }
    }

    public void actionDown(MotionEvent event){
        touchingMovePad = true;
        move(event.getX(), event.getY());
        setPointerId(event.getPointerId(0));
    }

    public void actionPointerUp(MotionEvent event){
        touchingMovePad = false;
        reset();
        setPointerId(-1);
    }

    public void actionUp(MotionEvent event){
        touchingMovePad = false;
        reset();
        setPointerId(-1);
    }

    public void actionMove(MotionEvent event, int index){
        if(touchingMovePad){
            processMoving(event.getX(index), event.getY(index));
        }else if(isTouched(event.getX(index), event.getY(index))){
            touchingMovePad = true;
            move(event.getX(index), event.getY(index));
        }
    }

    public int getSmallX() {
        return smallX;
    }

    public void setSmallX(int smallX) {
        this.smallX = smallX;
    }

    public int getSmallY() {
        return smallY;
    }

    public void setSmallY(int smallY) {
        this.smallY = smallY;
    }

    public void addSmallX(int x){
        smallX += x;
    }

    public void addSmallY(int y){
        smallY += y;
    }

    public int getPointerId() {
        return pointerId;
    }

    public void setPointerId(int pointerId) {
        this.pointerId = pointerId;
    }
}
