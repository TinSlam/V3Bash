package com.tinslam.comic.gameElements.entity.staticEntity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;

import com.tinslam.comic.UI.graphics.Images;
import com.tinslam.comic.base.Game;
import com.tinslam.comic.gameElements.Camera;
import com.tinslam.comic.utils.Consts;
import com.tinslam.comic.utils.Utils;

import java.util.ArrayList;

public class ConquerTower extends StaticEntity{
    private byte team;
    private byte side;
    private byte size;
    private int troops = 0;
    private static float textboxHeight = 16 * Game.density();
    private static float textboxWidth = 32 * Game.density();
    private static Paint fontPaint = new Paint();
    private static Paint textboxBackgroundPaint = new Paint();
    private static Paint textboxBorderPaint = new Paint();
    private static final Object conquerTowersLock = new Object();
    private static ArrayList<ConquerTower> conquerTowers = new ArrayList<>();

    static{
        fontPaint.setTextAlign(Paint.Align.CENTER);
        fontPaint.setTextSize(3 * textboxHeight / 4);
        textboxBackgroundPaint.setColor(Color.WHITE);
        textboxBorderPaint.setStyle(Paint.Style.STROKE);
        textboxBorderPaint.setStrokeWidth(2 * Game.density());
    }

    public ConquerTower(float x, float y, Camera camera, byte size, byte team, byte side) {
        super(x, y, assignImage(team, size), camera);
        this.side = side;
        this.team = team;
        this.size = size;

        if(size == Consts.CONQUER_TOWER_SIZE_BIG){
            troops = Consts.CONQUER_TOWER_TROOPS_BIG;
        }else{
            troops = Consts.CONQUER_TOWER_TROOPS_SMALL;
        }

        synchronized(conquerTowersLock){
            conquerTowers.add(this);
        }
    }

    @Override
    public void destroyStaticEntity(){
        synchronized(conquerTowersLock){
            conquerTowers.remove(this);
        }
    }

    public static Bitmap assignImage(byte team, byte size){
        switch(team){
            case Consts.CONQUER_TEAM_ALLY :
                if(size == Consts.CONQUER_TOWER_SIZE_BIG){
                    return Images.conquer_tower_red;
                }else{
                    return Images.conquer_tower_red_small;
                }

            case Consts.CONQUER_TEAM_ENEMY :
                if(size == Consts.CONQUER_TOWER_SIZE_BIG){
                    return Images.conquer_tower_blue;
                }else{
                    return Images.conquer_tower_blue_small;
                }

            default :
                if(size == Consts.CONQUER_TOWER_SIZE_BIG){
                    return Images.conquer_tower_neutral;
                }else{
                    return Images.conquer_tower_neutral_small;
                }
        }
    }

    @Override
    public void tickStaticEntity() {

    }

    @Override
    public void renderStaticEntity(Canvas canvas) {
        if(side == Consts.SIDE_LEFT) transformationMatrix.postScale(-1, 1, x + image.getWidth() / 2, y + image.getHeight() / 2);
        canvas.drawBitmap(image, transformationMatrix, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(getCollisionBox().centerX() - textboxWidth / 2, getCollisionBox().bottom + 4 * Game.density(), collisionBox.centerX() + textboxWidth / 2, getCollisionBox().bottom + 4 * Game.density() + textboxHeight, 2 * Game.density(), 2 * Game.density(), textboxBackgroundPaint);
            canvas.drawRoundRect(getCollisionBox().centerX() - textboxWidth / 2, getCollisionBox().bottom + 4 * Game.density(), collisionBox.centerX() + textboxWidth / 2, getCollisionBox().bottom + 4 * Game.density() + textboxHeight, 2 * Game.density(), 2 * Game.density(), textboxBorderPaint);
        }else{
            canvas.drawRect(getCollisionBox().centerX() - textboxWidth / 2, getCollisionBox().bottom + 4 * Game.density(), collisionBox.centerX() + textboxWidth / 2, getCollisionBox().bottom + 4 * Game.density() + textboxHeight, textboxBackgroundPaint);
            canvas.drawRect(getCollisionBox().centerX() - textboxWidth / 2, getCollisionBox().bottom + 4 * Game.density(), collisionBox.centerX() + textboxWidth / 2, getCollisionBox().bottom + 4 * Game.density() + textboxHeight, textboxBorderPaint);
        }
        Utils.drawTextDynamicSize("" + troops, getCollisionBox().centerX(), getCollisionBox().bottom + 4 * Game.density() + textboxHeight / 2, textboxWidth - 4 * Game.density(), fontPaint, canvas);
    }

    public static int getTeam2TowerIndex(ConquerTower ct){
        synchronized(conquerTowersLock){
            switch(conquerTowers.indexOf(ct)){
                case 0 :
                    return 2;

                case 9 :
                    return 8;

                case 7 :
                    return 6;

                case 1 :
                    return 3;

                case 5 :
                    return 4;

                case 10 :
                    return 11;

                case 2 :
                    return 0;

                case 8 :
                    return 9;

                case 6 :
                    return 7;

                case 3 :
                    return 1;

                case 4 :
                    return 5;

                case 11 :
                    return 10;

                default :
                    return 0;
            }
        }
    }

    public static ConquerTower getTeam2Tower(int index){
        synchronized(conquerTowersLock){
            switch(index){
                case 0 :
                    return conquerTowers.get(2);

                case 9 :
                    return conquerTowers.get(8);

                case 7 :
                    return conquerTowers.get(6);

                case 1 :
                    return conquerTowers.get(3);

                case 5 :
                    return conquerTowers.get(4);

                case 10 :
                    return conquerTowers.get(11);

                case 2 :
                    return conquerTowers.get(0);

                case 8 :
                    return conquerTowers.get(9);

                case 6 :
                    return conquerTowers.get(7);

                case 3 :
                    return conquerTowers.get(1);

                case 4 :
                    return conquerTowers.get(5);

                case 11 :
                    return conquerTowers.get(10);

                default :
                    return conquerTowers.get(0);
            }
        }
    }

    public static Object getConquerTowersLock() {
        return conquerTowersLock;
    }

    public static ArrayList<ConquerTower> getConquerTowers() {
        return conquerTowers;
    }

    public byte getTeam() {
        return team;
    }

    public void setTeam(byte team) {
        this.team = team;
        image = assignImage(team, size);
    }

    public byte getSide() {
        return side;
    }

    public void setSide(byte side) {
        this.side = side;
    }

    public float getVisualXCenter(){
        return getCollisionBox().centerX() + 2 * Game.density();
    }

    public float getVisualYCenter(){
        return getCollisionBox().bottom - getCollisionBox().height() / 5;
    }

    public byte getSize() {
        return size;
    }

    public void setSize(byte size) {
        this.size = size;
    }

    public int getTroops() {
        return troops;
    }

    public void setTroops(int troops) {
        this.troops = troops;
    }
}
