package com.tinslam.comic.states;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.tinslam.comic.base.Game;

public abstract class GameState extends State{
    private static Paint timeBackgroundPaint = new Paint();
    private static Paint timeBorderPaint = new Paint();
    private static Paint timeFontPaint = new Paint();
    protected int time = 0;
    private Thread timeThread;

    static{
        timeBackgroundPaint.setColor(Color.WHITE);
        timeBorderPaint.setColor(Color.BLACK);
        timeBorderPaint.setStyle(Paint.Style.STROKE);
        timeBorderPaint.setStrokeWidth(2 * Game.density());
        timeFontPaint.setTextAlign(Paint.Align.CENTER);
        timeFontPaint.setTextSize(16 * Game.density());
    }

    public abstract void trophyReached();

    public void startTimer(int time){
        this.time = time;
        final GameState gameState = this;
        timeThread = new Thread(new Runnable(){
            @Override
            public void run(){
                while(gameState.time > 0){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    gameState.time--;
                }
            }
        });
        timeThread.start();
    }

    public void showTime(Canvas canvas){
        canvas.drawCircle(Game.getScreenWidth() / 2, 0, 32 * Game.density(), timeBackgroundPaint);
        canvas.drawCircle(Game.getScreenWidth() / 2, 0, 32 * Game.density(), timeBorderPaint);
        canvas.drawText(time + "", Game.getScreenWidth() / 2, 16 * Game.density(), timeFontPaint);
    }
}
