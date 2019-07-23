package com.tinslam.comic.gameElements;

import android.graphics.Rect;

import com.tinslam.comic.base.Game;
import com.tinslam.comic.gameElements.entity.Entity;
import com.tinslam.comic.gameElements.entity.movingEntity.MovingEntity;
import com.tinslam.comic.utils.Consts;
import com.tinslam.comic.utils.Utils;

public class Camera{
    private MovingEntity followObject = null;
    private float x, y;
    private float rotation = 0;
    private float scale = 1;

    public void tick(){
        follow();
    }

    public void follow(){
        if(followObject != null){
            float distance = Utils.distance(-x, -y, followObject.getX() - (Game.getScreenWidth() - followObject.getImage().getWidth()) / 2, followObject.getY() - (Game.getScreenHeight() - followObject.getImage().getHeight()) / 2);
            if(distance < 4){
                setX(-followObject.getX() + (Game.getScreenWidth() - followObject.getImage().getWidth()) / 2);
                setY(-followObject.getY() + (Game.getScreenHeight() - followObject.getImage().getHeight()) / 2);
                return;
            }
            float angle = (float) Math.atan2(followObject.getY() - (Game.getScreenHeight() - followObject.getImage().getHeight()) / 2 + y, followObject.getX() - (Game.getScreenWidth() - followObject.getImage().getWidth()) / 2 + x);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            addX((cos * (distance / Consts.CAMERA_MOVE_TIME * 100)));
            addY((sin * (distance / Consts.CAMERA_MOVE_TIME) * 100));
        }
    }

    public boolean isOnScreen(Rect collisionBox) {
        Rect rect = new Rect((int) -x, (int) -y, (int) (-x + Game.getScreenWidth()), (int) (-y + Game.getScreenHeight()));
        return rect.intersect(collisionBox);
    }

    public void move(float dx, float dy){
        synchronized(Game.getState().getRenderLock()){
            if(dx >= 0){
                if(x - dx >= -Game.getState().getMapRightEnd() + Game.getScreenWidth()) x -= dx; else x = -Game.getState().getMapRightEnd() + Game.getScreenWidth();
            }else{
                if(x - dx <= 0) x -= dx; else x = 0;
            }
            if(dy >= 0){
                if(y - dy >= -Game.getState().getMapBottomEnd() + Game.getScreenHeight()) y -= dy; else y = -Game.getState().getMapBottomEnd() + Game.getScreenHeight();
            }else{
                if(y - dy <= 0) y -= dy; else y = 0;
            }
            Entity.resetAllPositions();
        }
    }

    public void jump(float x, float y){
        synchronized(Game.getState().getRenderLock()){
            if(x <= 0 && x >= -Game.getState().getMapRightEnd() + Game.getScreenWidth()) this.x = x;
            if(y <= 0 && y >= -Game.getState().getMapBottomEnd() + Game.getScreenHeight()) this.y = y;
            Entity.resetAllPositions();
        }
    }

    public void addX(float dx){
        synchronized(Game.getState().getRenderLock()) {
            if(dx >= 0){
                if(x - dx >= -Game.getState().getMapRightEnd() + Game.getScreenWidth()) x -= dx; else x = -Game.getState().getMapRightEnd() + Game.getScreenWidth();
            }else{
                if(x - dx <= 0) x -= dx; else x = 0;
            }
            Entity.resetAllPositions();
        }
    }

    public void addY(float dy){
        synchronized(Game.getState().getRenderLock()) {
            if(dy >= 0){
                if(y - dy >= -Game.getState().getMapBottomEnd() + Game.getScreenHeight()) y -= dy; else y = -Game.getState().getMapBottomEnd() + Game.getScreenHeight();
            }else{
                if(y - dy <= 0) y -= dy; else y = 0;
            }
            Entity.resetAllPositions();
        }
    }

    public MovingEntity getFollowObject() {
        return followObject;
    }

    public void setFollowObject(MovingEntity followObject) {
        this.followObject = followObject;
        followObject.setFollowedByCamera(true);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        synchronized(Game.getState().getRenderLock()) {
            if(x <= 0 && x >= -Game.getState().getMapRightEnd() + Game.getScreenWidth()) this.x = x;
            else if(x > 0) x = 0;
            else x = -Game.getState().getMapRightEnd() + Game.getScreenWidth();
            Entity.resetAllPositions();
        }
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        synchronized(Game.getState().getRenderLock()) {
            if(y <= 0 && y >= -Game.getState().getMapBottomEnd() + Game.getScreenHeight()) this.y = y;
            else if(y > 0) y = 0;
            else y = -Game.getState().getMapBottomEnd() + Game.getScreenHeight();
            Entity.resetAllPositions();
        }
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        synchronized(Game.getState().getRenderLock()) {
            this.rotation = rotation;
            Entity.resetAllPositions();
        }
    }

    public void addRotation(float rotation){
        synchronized(Game.getState().getRenderLock()) {
            this.rotation -= rotation;
            Entity.resetAllPositions();
        }
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        synchronized(Game.getState().getRenderLock()) {
            this.scale = scale;
            Entity.resetAllPositions();
        }
    }

    public void zoom(float zoom){
        synchronized(Game.getState().getRenderLock()) {
            this.scale *= zoom;
            Entity.resetAllPositions();
        }
    }
}