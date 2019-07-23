package com.tinslam.comic.gameElements.entity.staticEntity;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.tinslam.comic.gameElements.Camera;
import com.tinslam.comic.gameElements.entity.Entity;

import java.util.ArrayList;

public abstract class StaticEntity extends Entity {
    private static ArrayList<StaticEntity> staticEntities = new ArrayList<>();
    private static final Object staticEntitiesLock = new Object();

    public StaticEntity(float x, float y, Bitmap image, Camera camera) {
        super(x, y, image, camera);

        synchronized(staticEntitiesLock){
            staticEntities.add(this);
        }
    }

    @Override
    public void destroyEntity(){
        destroyStaticEntity();

        synchronized(staticEntitiesLock){
            staticEntities.remove(this);
        }
    }

    public abstract void destroyStaticEntity();

    public abstract void tickStaticEntity();

    public abstract void renderStaticEntity(Canvas canvas);

    @Override
    public void tickEntity(){
        tickStaticEntity();
    }

    @Override
    public void renderEntity(Canvas canvas){
        renderStaticEntity(canvas);
    }
}
