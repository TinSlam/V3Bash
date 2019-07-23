package com.tinslam.comic.modes.painting;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;

import com.tinslam.comic.UI.buttons.Node;
import com.tinslam.comic.base.Game;
import com.tinslam.comic.networking.Networking;
import com.tinslam.comic.utils.Consts;
import com.tinslam.comic.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public class PaintingPainting {
    private Paint backgroundPaint = new Paint();
    private Paint drawingPaint = new Paint();
    private Paint borderPaint = new Paint();
    private Paint fontPaint = new Paint();
    private byte vote = 0;
    private HashMap<String, Node> hashMap;
    private String subject;
    private int order = -1;
    private byte player;
    private float xOffset, yOffset, elementWidth, elementHeight;
    private Rect rect = new Rect(0, 0, 0, 0);
    private int points = 0;
    private double ratio;
    private String username = "";

    public PaintingPainting(HashMap<String, Node> hashMap, String username, String subject, int backgroundColor, double ratio){
        this.username = username;
        player = Networking.getPlayerIndex(username);
        this.hashMap = hashMap;
        this.subject = subject;
        this.ratio = ratio;
        fontPaint.setColor(Color.BLACK);
        fontPaint.setTextSize(16 * Game.density());
        backgroundPaint.setColor(backgroundColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.BLACK);
        borderPaint.setTextSize(16 * Game.density());
        elementWidth = (((Game.getScreenWidth() - 32 * Game.density()) / 3) / 128);
        elementHeight = (float) (elementWidth / ratio);
//        elementHeight = (((Game.getScreenHeight() - 24 * Game.density()) / 2) / 64);
    }

    public void renderResults(Canvas canvas){
        if(order == -1) return;

        canvas.drawRect(xOffset, yOffset, xOffset + 128 * elementWidth, yOffset + 64 * elementHeight, backgroundPaint);
        renderNodes(canvas);
        canvas.drawRect(xOffset, yOffset, xOffset + 128 * elementWidth, yOffset + 64 * elementHeight, borderPaint);
        canvas.drawCircle(xOffset + 64 * elementWidth, yOffset + 32 * elementHeight, 32 * Game.density(), borderPaint);
        canvas.drawText(points + "", xOffset + 64 * elementWidth - 64, yOffset + 64 * elementHeight, fontPaint);
        canvas.drawText(username, xOffset + 64 * elementWidth, yOffset + 64 * elementHeight + 8 * Game.density(), fontPaint);
    }

    public void render(Canvas canvas){
        if(order == -1) return;

        canvas.drawRect(xOffset, yOffset, xOffset + 128 * elementWidth, yOffset + 64 * elementHeight, backgroundPaint);
        renderNodes(canvas);
        canvas.drawRect(xOffset, yOffset, xOffset + 128 * elementWidth, yOffset + 64 * elementHeight, borderPaint);
        canvas.drawCircle(xOffset + 64 * elementWidth, yOffset + 32 * elementHeight, 32 * Game.density(), borderPaint);
        if(vote != 0) canvas.drawText(vote + "", xOffset + 64 * elementWidth, yOffset + 64 * elementHeight, fontPaint);
    }

    private void renderNodes(Canvas canvas){
        for(Node x : hashMap.values()){
            drawingPaint.setColor(Utils.getColor(x.getColorCode()));
            switch(x.getBrushStyle()){
                case Consts.BRUSH_STYLE_RECT :
                    canvas.drawRect((xOffset + x.getX() * elementWidth), (yOffset + x.getY() * elementHeight),
                            (xOffset + (x.getX() + 1) * elementWidth), (yOffset + (x.getY() + 1) * elementHeight), drawingPaint);
                    break;

//                    case Consts.BRUSH_STYLE_OVAL :
//                        canvas.drawCircle((int) (xOffset + x.getX() * elementWidth + elementWidth / 2), (int) (yOffset + x.getY() * elementHeight + elementHeight / 2),
//                                elementWidth, drawingPaint);
//                        break;
            }
        }
    }

    public static boolean onActionDown(MotionEvent event, ArrayList<PaintingPainting> paintings, Object lock){
        int i = (int) event.getX();
        int j = (int) event.getY();
        synchronized(lock){
            for(PaintingPainting x : paintings){
                if(x.isTouched(i, j)){
                    return x.onDown();
                }
            }
        }
        return false;
    }

    public static boolean onActionPointerDown(MotionEvent event, ArrayList<PaintingPainting> paintings, Object lock){
        int i = (int) event.getX(event.getActionIndex());
        int j = (int) event.getY(event.getActionIndex());
        synchronized(lock){
            for(PaintingPainting x : paintings){
                if(x.isTouched(i, j)){
                    return x.onDown();
                }
            }
        }
        return false;
    }

    public static boolean onActionUp(MotionEvent event, ArrayList<PaintingPainting> paintings, Object lock){
        int i = (int) event.getX();
        int j = (int) event.getY();
        synchronized(lock){
            for(PaintingPainting x : paintings){
                if(x.isTouched(i, j)){
                    return x.onUp();
                }
            }
        }
        return false;
    }

    public static boolean onActionPointerUp(MotionEvent event, ArrayList<PaintingPainting> paintings, Object lock){
        int i = (int) event.getX(event.getActionIndex());
        int j = (int) event.getY(event.getActionIndex());
        synchronized(lock){
            for(PaintingPainting x : paintings){
                if(x.isTouched(i, j)){
                    return x.onUp();
                }
            }
        }
        return false;
    }

    private boolean isTouched(int x, int y) {
        return order != -1 && rect.contains(x, y);
    }

    public boolean onDown(){
        return true;
    }

    public boolean onUp(){
        if(vote == 0){
            if(username.equals(Networking.getUsername())) return true;
            vote = PaintingGameState.getVote();
            PaintingGameState.setVote((byte) (PaintingGameState.getVote() + 1));
        }else{
            PaintingGameState.setVote(vote);
            synchronized(PaintingGameState.getPaintingsLock()){
                for(PaintingPainting x : PaintingGameState.getPaintings()){
                    if(x.getOrder() != -1){
                        if(x.getVote() > vote){
                            x.setVote((byte) 0);
                        }
                    }
                }
            }
            vote = 0;
        }
        return true;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;

        switch(order){
            case 0 :
                xOffset = (int) (8 * Game.density());
                yOffset = (int) (8 * Game.density());
                break;

            case 1 :
                xOffset = (int) (8 * Game.density() + 128 * elementWidth + 8 * Game.density());
                yOffset = (int) (8 * Game.density());
                break;

            case 2 :
                xOffset = (int) (8 * Game.density() + 128 * elementWidth + 8 * Game.density() + 128 * elementWidth + 8 * Game.density());
                yOffset = (int) (8 * Game.density());
                break;

            case 3 :
                xOffset = (int) (8 * Game.density());
                yOffset = (int) (16 * Game.density() + 64 * elementHeight);
                break;

            case 4 :
                xOffset = (int) (8 * Game.density() + 128 * elementWidth + 8 * Game.density());
                yOffset = (int) (16 * Game.density() + 64 * elementHeight);
                break;

            case 5 :
                xOffset = (int) (8 * Game.density() + 128 * elementWidth + 8 * Game.density() + 128 * elementWidth + 8 * Game.density());
                yOffset = (int) (16 * Game.density() + 64 * elementHeight);
                break;
        }

        rect.set((int) xOffset, (int) yOffset, (int) (xOffset + elementWidth * 128), (int) (yOffset + elementHeight * 64));
    }

    public byte getVote() {
        return vote;
    }

    public void setVote(byte vote) {
        this.vote = vote;
    }

    public byte getPlayer() {
        return player;
    }

    public void setPlayer(byte player) {
        this.player = player;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void addPoints(int points){
        this.points += points;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSubject() {
        return subject;
    }
}