package com.tinslam.comic.gameElements.entity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;

import com.tinslam.comic.base.Game;
import com.tinslam.comic.gameElements.Camera;
import com.tinslam.comic.gameElements.Map;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class Entity{
    protected float x, y;
    protected Bitmap image;
    protected boolean followedByCamera = false;
    protected Matrix transformationMatrix = new Matrix();
    protected Rect collisionBox = new Rect();
    protected boolean isSolid = true;
    protected boolean canDie = true;
    protected Camera camera;
    protected float rotation = 0;
    private static ArrayList<Entity> entities = new ArrayList<>();
    private static final Object entitiesLock = new Object();
    protected Map map;
    protected boolean isDead = false;

    public Entity(float x, float y, Bitmap image, Camera camera){
        this.x = x;
        this.y = y;
        this.image = image;
        this.camera = camera;
        collisionBox.set((int) x, (int) y, (int) (x + image.getWidth()), (int) (y + image.getHeight()));
        synchronized(entitiesLock){
            entities.add(this);
        }
    }

    public void destroy(){
        final Entity ent = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                destroyEntity();

                synchronized(entitiesLock){
                    entities.remove(ent);
                }
            }
        }).start();
    }

    public abstract void destroyEntity();

    public abstract void tickEntity();

    public abstract void renderEntity(Canvas canvas);

    public void tick(){
        resetPositions();
        tickEntity();
    }

    public void updateCollisionBox(){
        collisionBox.set((int) x, (int) y, (int) (x + image.getWidth()), (int) (y + image.getHeight()));
    }

    public static void resetAllPositions(){
        synchronized(entitiesLock){
            for(Entity x : entities){
                x.resetPositions();
            }
        }
    }

    public void resetPositions(){
        synchronized(Game.getState().getRenderLock()){
            transformationMatrix.reset();
            transformationMatrix.preRotate(camera.getRotation() + rotation, image.getWidth() / 2, image.getHeight() / 2);
            transformationMatrix.postTranslate(camera.getX() + x, camera.getY() + y);
        }
    }

    public boolean canMove(float x, float y){
        if(map == null) return true;

        if(x < 0 || y < 0 || x > Game.getState().getMapRightEnd() - image.getWidth() || y > Game.getState().getMapBottomEnd() - image.getHeight()) return false;

        return !map.isSolid(collisionBox);
    }

    public void render(Canvas canvas){
        renderEntity(canvas);
    }

    public Rect getCollisionBox() {
        return collisionBox;
    }

    public void setCollisionBox(Rect collisionBox) {
        this.collisionBox = collisionBox;
    }

    public boolean isSolid() {
        return isSolid;
    }

    public void setSolid(boolean solid) {
        isSolid = solid;
    }

    public boolean isCanDie() {
        return canDie;
    }

    public void setCanDie(boolean canDie) {
        this.canDie = canDie;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        if(canMove(x, y)) this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        if(canMove(x, y)) this.y = y;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public void addX(float dx){
        if(canMove(x + dx, y)) x += dx;
    }

    public void addY(float dy){
        if(canMove(x, y + dy)) y += dy;
    }

    public void setHardX(float x){
        this.x = x;
    }

    public void setHardY(float y){
        this.y = y;
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public static ArrayList<Entity> getEntities() {
        return entities;
    }

    public static Object getEntitiesLock() {
        return entitiesLock;
    }

    public boolean isFollowedByCamera() {
        return followedByCamera;
    }

    public void setFollowedByCamera(boolean followedByCamera) {
        this.followedByCamera = followedByCamera;
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public boolean isDead() {
        return isDead;
    }

    public void setDead(boolean dead) {
        isDead = dead;
    }
}