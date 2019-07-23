package com.tinslam.comic.gameElements.entity.movingEntity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.tinslam.comic.base.Game;
import com.tinslam.comic.gameElements.Camera;
import com.tinslam.comic.gameElements.Map;
import com.tinslam.comic.networking.Networking;
import com.tinslam.comic.utils.Circle;

public class Player extends MovingEntity{
    private Circle circle;
    private static Player player;
    private String username = "";
    private static Paint fontPaint = new Paint();
    static{
        fontPaint.setTextAlign(Paint.Align.CENTER);
        fontPaint.setTextSize(16 * Game.density());
    }

    public Player(String username, float x, float y, Bitmap image, Camera camera, Map map) {
        super(x, y, image, camera);
        setSpeed(4 * Game.density());

        this.username = username;
        if(username.equals(Networking.getUsername()) || username.equals("")){
            Player.player = this;
        }
        circle = new Circle(0, 0, 0);
        this.map = map;
    }

    @Override
    public void destroyMovingEntity(){

    }

    @Override
    public boolean canMove(float x, float y){
        if(map == null) return true;

        if(x < 0 || y < 0 || x > Game.getState().getMapRightEnd() - image.getWidth() || y > Game.getState().getMapBottomEnd() - image.getHeight()) return false;

        return !map.isSolidCircle(x + image.getWidth() / 2, y + image.getHeight() / 2, image.getWidth() / 2);
    }

    @Override
    public void tickMovingEntity() {

    }

    @Override
    public void renderMovingEntity(Canvas canvas) {
        if(!followedByCamera){
            canvas.drawBitmap(image, transformationMatrix, null);
        }else{
            transformationMatrix.reset();
            float px = Game.getState().getMapRightEnd() - (float) (Game.getScreenWidth() + image.getWidth()) / 2;
            float py = Game.getState().getMapBottomEnd() - (float) (Game.getScreenHeight() + image.getHeight()) / 2;
            transformationMatrix.preRotate(rotation, image.getWidth() / 2, image.getHeight() / 2);
            if(x >= px && y >= py){
                px = (float) Game.getScreenWidth() + x - Game.getState().getMapRightEnd();
                py = (float) Game.getScreenHeight() + y - Game.getState().getMapBottomEnd();
                transformationMatrix.postTranslate(px, py);
            }else if(y >= py && x < px){
                py = (float) Game.getScreenHeight() + y - Game.getState().getMapBottomEnd();
                transformationMatrix.postTranslate(Math.min(x, (float) (Game.getScreenWidth() - image.getWidth()) / 2), py);
            }else if(y < py && x >= px){
                px = (float) Game.getScreenWidth() + x - Game.getState().getMapRightEnd();
                transformationMatrix.postTranslate(px, Math.min(y, (float) (Game.getScreenHeight() - image.getHeight()) / 2));
            }else{
                transformationMatrix.postTranslate(Math.min(x, (float) (Game.getScreenWidth() - image.getWidth()) / 2), Math.min(y, (float) (Game.getScreenHeight() - image.getHeight()) / 2));
            }
            camera.follow();
            canvas.drawBitmap(image, transformationMatrix, null);
        }
        canvas.drawText(username, x + camera.getX() + image.getWidth() / 2, y + camera.getY() - image.getHeight() / 2, fontPaint);
    }

    public Circle getCircle(){
        circle.setX(x + image.getWidth() / 2);
        circle.setY(y + image.getHeight() / 2);
        circle.setRadius(image.getWidth() / 2);

        return circle;
    }

    public static Player getPlayer() {
        return player;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}