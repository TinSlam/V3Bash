package com.tinslam.comic.UI.graphics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.tinslam.comic.interfaces.AnimationInterface;

import java.util.ArrayList;

public abstract class Animation implements AnimationInterface {
    private int index = 0;
    private Bitmap rawImage;
    private long interval = 0;
    private int width = 0;
    private ArrayList<Bitmap> images = new ArrayList<>();
    private int size = 0;
    private int repeat = -1;
    private Thread thread;
    private Object lock = new Object();
    private boolean stop = false;
    private boolean pause = false;
    private boolean softStop = false;
    private boolean threadDone = false;
    private int x = 0, y = 0;

    public Animation(Bitmap rawImage, long interval, int size) {
        this.rawImage = rawImage;
        this.interval = interval;
        this.size = size;

        processImage();
        startThread();
    }

    public Animation(Bitmap rawImage, long interval, int size, int repeat){
        this.rawImage = rawImage;
        this.interval = interval;
        this.size = size;
        this.repeat = repeat;

        processImage();
        startThread();
    }

    public void processImage(){
        width = rawImage.getWidth() / size;
        for(int i = 0; i < size; i++){
            images.add(Bitmap.createBitmap(rawImage, i * width, 0, width, rawImage.getHeight()));
        }
    }

    public void startThread(){
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if(repeat == -1){
                    while(true){
                        if(stop) break;
                        if(pause) pauseThread();
                        try {
                            Thread.sleep(interval);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(stop) break;
                        if(pause) pauseThread();
                        if(index < images.size() - 1){
                            index++;
                        }else{
                            index = 0;
                            onCycleEnd();
                            if(softStop){
                                onEnd();
                                break;
                            }
                        }
                    }
                }else{
                    int cycle = 0;
                    while(true){
                        if(stop) break;
                        if(pause) pauseThread();
                        try {
                            Thread.sleep(interval);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(stop) break;
                        if(pause) pauseThread();
                        if(index < images.size() - 1){
                            index++;
                        }else{
                            cycle++;
                            index = 0;
                            onCycleEnd();
                            if(softStop){
                                onEnd();
                                break;
                            }
                            if(cycle == repeat){
                                onEnd();
                                break;
                            }
                        }
                    }
                }
                cleanUp();
                threadDone = true;
            }
        });
        thread.start();
    }

    private void pauseThread(){
        synchronized(lock){
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void pause(){
        pause = true;
    }

    public void resume(){
        pause = false;
        synchronized(lock){
            lock.notify();
        }
    }

    public void hardStop(){
        if(pause) resume();
        stop = true;
    }

    public void softStop(){
        if(pause) resume();
        softStop = true;
    }

    public void reset(){
        if(pause) resume();
        hardStop();
        (new Thread(new Runnable() {
            @Override
            public void run() {
                while(!threadDone){
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                threadDone = false;
                cleanUp();
                startThread();
            }
        })).start();
    }

    private void cleanUp(){
        softStop = false;
        stop = false;
        pause = false;
        index = 0;
    }

    public void resetIndex(){
        index = 0;
    }

    public void render(int x, int y, Canvas canvas){
        canvas.drawBitmap(images.get(index), x, y, null);
    }

    public void render(Matrix matrix, Canvas canvas){
        canvas.drawBitmap(images.get(index), matrix, null);
    }

    public void render(Canvas canvas){
        canvas.drawBitmap(images.get(index), x, y, null);
    }

    public abstract void onEnd();

    public abstract void onCycleEnd();

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return rawImage.getHeight();
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}