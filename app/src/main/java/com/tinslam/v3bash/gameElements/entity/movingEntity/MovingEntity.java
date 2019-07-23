package com.tinslam.comic.gameElements.entity.movingEntity;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.tinslam.comic.gameElements.Camera;
import com.tinslam.comic.gameElements.entity.Entity;
import com.tinslam.comic.utils.Consts;
import com.tinslam.comic.utils.Utils;

import java.util.ArrayList;
import java.util.Timer;

public abstract class MovingEntity extends Entity {
    private static ArrayList<MovingEntity> movingEntities = new ArrayList<>();
    private static final Object movingEntitiesLock = new Object();
    protected float speed = 0;
    protected float x2, y2;

    public MovingEntity(float x, float y, Bitmap image, Camera camera) {
        super(x, y, image, camera);

        x2 = x;
        y2 = y;

        synchronized(movingEntitiesLock){
            movingEntities.add(this);
        }
    }

    @Override
    public void destroyEntity(){
        destroyMovingEntity();

        synchronized(movingEntitiesLock){
            movingEntities.remove(this);
        }
    }

    public abstract void destroyMovingEntity();

    public abstract void tickMovingEntity();

    public abstract void renderMovingEntity(Canvas canvas);

    public void move(int dx, int dy){
        addX(dx);
        addY(dy);
    }

    public void move(int time){
        float angle = (float) Math.atan2(y2 - y, x2 - x);
        float distance = Utils.distance(x, y, x2, y2);
        float speed = distance / time;
        float s = (float) Math.sin(angle);
        float c = (float) Math.cos(angle);
        setRotation((float) Math.toDegrees(angle) + 90);
        setHardX(x + c * speed);
        setHardY(y + s * speed);
    }

    public void move(){
        float angle = (float) Math.atan2(y2 - y, x2 - x);
        float s = (float) Math.sin(angle);
        float c = (float) Math.cos(angle);
        setRotation((float) Math.toDegrees(angle) + 90);
        setHardX(x + c * speed);
        setHardY(y + s * speed);
    }

    @Override
    public void tickEntity() {
        updateCollisionBox();
        tickMovingEntity();
    }

    @Override
    public void renderEntity(Canvas canvas) {
        renderMovingEntity(canvas);
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getX2() {
        return x2;
    }

    public void setX2(float x2) {
        this.x2 = x2;
    }

    public float getY2() {
        return y2;
    }

    public void setY2(float y2) {
        this.y2 = y2;
    }
}
