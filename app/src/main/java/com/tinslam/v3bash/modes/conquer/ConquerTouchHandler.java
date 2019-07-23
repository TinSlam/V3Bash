package com.tinslam.comic.modes.conquer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import com.tinslam.comic.base.Game;
import com.tinslam.comic.gameElements.entity.staticEntity.ConquerTower;
import com.tinslam.comic.networking.Networking;
import com.tinslam.comic.utils.Utils;

import java.util.ArrayList;

public class ConquerTouchHandler{
    private ConquerTower src = null, dst = null;
    private int x2, y2;
    private static Paint paint = new Paint();
    private int pointerId = -1;
    private static final Object conquerTouchHandlersLock = new Object();
    private static ArrayList<ConquerTouchHandler> conquerTouchHandlers = new ArrayList<>();

    static{
        paint.setARGB(160, 0, 0, 0);
    }

    public ConquerTouchHandler(ConquerTower src, int pointerId){
        this.pointerId = pointerId;
        this.src = src;
        this.x2 = (int) src.getVisualXCenter();
        this.y2 = (int) src.getVisualYCenter();

        synchronized(conquerTouchHandlersLock){
            for(ConquerTouchHandler x : conquerTouchHandlers){
                if(x.getPointerId() == pointerId){
                    return;
                }
            }
            conquerTouchHandlers.add(this);
        }
    }

    public void perform(){
        synchronized(ConquerTower.getConquerTowersLock()){
            Networking.conquerSendTroops(ConquerTower.getConquerTowers().indexOf(src), (int) (src.getTroops() * ConquerGameState.getArmyRatio()), ConquerTower.getConquerTowers().indexOf(dst));
        }
//        for(int i = 0; i < src.getTroops() * ConquerGameState.getArmyRatio(); i++){
//            int tx = (int) (Math.random() * 64 * Game.density());
//            int ty = (int) (Math.random() * 16 * Game.density());
//
//            int x = (int) (src.getVisualXCenter() - 64 * Game.density() + tx);
//            int y = (int) (src.getCollisionBox().bottom + ty);
//            new ConquerTroop(x, y, dst.getVisualXCenter(), dst.getVisualYCenter() + ty, Images.conquer_troops, ConquerGameState.getCamera());
//        }
        remove(pointerId);
    }

    public static ConquerTouchHandler getConquerTouchHandler(int pointerId){
        synchronized(conquerTouchHandlersLock){
            for(ConquerTouchHandler x : conquerTouchHandlers){
                if(x.getPointerId() == pointerId){
                    return x;
                }
            }
        }

        return null;
    }

    public static void remove(int pointerId){
        synchronized(conquerTouchHandlersLock){
            for(ConquerTouchHandler x : conquerTouchHandlers){
                if(x.getPointerId() == pointerId){
                    conquerTouchHandlers.remove(x);
                }
            }
        }
    }

    public void render(Canvas canvas){
        float srcX = src.getVisualXCenter();
        float srcY = src.getVisualYCenter();
        if(Utils.distance(x2, y2, srcX, srcY) < src.getImage().getWidth() / 2) return;

        float angle = (float) (Math.atan2(y2 - srcY, x2 - srcX));
        angle += Math.toRadians(90);
        float s = (float) Math.sin(angle);
        float c = (float) Math.cos(angle);
        float rectWidth = 8 * Game.density();
        float arrowStartBeforePoint = 12 * Game.density();
        float arrowMaxWidth = 16 * Game.density();

        Path p = new Path();
        p.moveTo(srcX + rectWidth / 2 * c, srcY + rectWidth / 2 * s);
        p.lineTo(x2 + rectWidth / 2 * c - arrowStartBeforePoint * s, y2 + rectWidth / 2 * s + arrowStartBeforePoint * c);
        p.lineTo(x2 - rectWidth / 2 * c - arrowStartBeforePoint * s, y2 - rectWidth / 2 * s + arrowStartBeforePoint * c);
        p.lineTo(srcX - rectWidth / 2 * c, srcY - rectWidth / 2 * s);
        p.close();

        Path p2 = new Path();
        p2.moveTo(x2 + arrowMaxWidth * c - arrowStartBeforePoint * s, y2 + arrowMaxWidth * s + arrowStartBeforePoint * c);
        p2.lineTo(x2, y2);
        p2.lineTo(x2 - arrowMaxWidth * c - arrowStartBeforePoint * s, y2 - arrowMaxWidth * s + arrowStartBeforePoint * c);
        p2.close();

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        canvas.drawPath(p, paint);
        canvas.drawPath(p2, paint);
        canvas.drawCircle(srcX, srcY, rectWidth / 2, paint);
    }

    public ConquerTower getSrc() {
        return src;
    }

    public void setSrc(ConquerTower src) {
        this.src = src;
    }

    public ConquerTower getDst() {
        return dst;
    }

    public void setDst(ConquerTower dst) {
        this.dst = dst;
    }

    public int getPointerId() {
        return pointerId;
    }

    public void setPointerId(int pointerId) {
        this.pointerId = pointerId;
    }

    public static Object getConquerTouchHandlersLock() {
        return conquerTouchHandlersLock;
    }

    public static ArrayList<ConquerTouchHandler> getConquerTouchHandlers() {
        return conquerTouchHandlers;
    }

    public int getX2() {
        return x2;
    }

    public void setX2(int x2) {
        this.x2 = x2;
    }

    public int getY2() {
        return y2;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }
}
