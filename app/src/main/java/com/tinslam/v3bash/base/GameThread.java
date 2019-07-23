package com.tinslam.comic.base;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class GameThread extends Thread{
    private static final int maxFps = 30;
    private double averageFps;
    private SurfaceHolder surfaceHolder;
    private Game game;
    private boolean running;
    private static Canvas canvas;
    private static final Object lock = new Object();
    private static GameThread gameThread;
    private static boolean paused = false;

    public GameThread(SurfaceHolder surfaceHolder, Game game){
        super();
        this.surfaceHolder = surfaceHolder;
        this.game = game;
        GameThread.gameThread = this;
    }

    public static void pauseThread(){
        paused = true;
    }

    public static void resumeThread(){
        paused = false;
        synchronized(lock){
            lock.notify();
        }
    }

    @Override
    public void run(){
        long startTime;
        long timeMillis;
        long waitTime;
        int frameCount = 0;
        long totalTime = 0;
        long targetTime = 1000/maxFps;

        while(running){
            if(paused){
                synchronized(lock){
                    try{
                        lock.wait();
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
            startTime = System.nanoTime();
            canvas = null;

            try{
                canvas = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder){
                    game.update();
                    game.draw(canvas);
                }
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                if(canvas != null){
                    try{
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
            timeMillis = (System.nanoTime() - startTime) / 1000000;
            waitTime = targetTime - timeMillis;
            try{
                if(waitTime > 0){
                    sleep(waitTime);
                }
            }catch(Exception e){
                e.printStackTrace();
            }

            totalTime += System.nanoTime() - startTime;
            frameCount++;

            if(frameCount == maxFps){
                averageFps = 1000 / ((totalTime / frameCount) / 1000000);
                frameCount = 0;
                totalTime = 0;
                System.out.println("Average FPS : " + averageFps);
            }
        }
    }

    public void setRunning(boolean val){
        running = val;
    }

    public static int getFps(){
        return maxFps;
    }
}