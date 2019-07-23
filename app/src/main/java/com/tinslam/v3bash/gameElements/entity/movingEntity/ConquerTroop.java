package com.tinslam.comic.gameElements.entity.movingEntity;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.tinslam.comic.base.Game;
import com.tinslam.comic.base.GameThread;
import com.tinslam.comic.gameElements.Camera;
import com.tinslam.comic.gameElements.entity.staticEntity.ConquerTower;
import com.tinslam.comic.utils.Utils;

import java.util.ArrayList;

public class ConquerTroop extends MovingEntity{
    private static ArrayList<ConquerTroop> conquerTroops = new ArrayList<>();
    private static final Object conquerTroopsLock = new Object();

    public ConquerTroop(float x, float y, float x2, float y2, int time, Bitmap image, Camera camera) {
        super(x, y, image, camera);

        this.x2 = x2;
        this.y2 = y2;
        this.speed = Utils.distance(x2, y2, x, y) / (time / 1000 * GameThread.getFps());

        synchronized(conquerTroopsLock){
            conquerTroops.add(this);
        }
    }

    @Override
    public void destroyMovingEntity(){
        synchronized(conquerTroopsLock){
            conquerTroops.remove(this);
        }
    }

    @Override
    public void tickMovingEntity() {
        move();
        if(Utils.distance(x, y, x2, y2) < 4 * Game.density()){
            destroy();
        }
    }

    @Override
    public void renderMovingEntity(Canvas canvas) {
        canvas.drawBitmap(image, transformationMatrix, null);
    }

    public static ArrayList<ConquerTroop> getConquerTroops() {
        return conquerTroops;
    }

    public static Object getConquerTroopsLock() {
        return conquerTroopsLock;
    }
}
