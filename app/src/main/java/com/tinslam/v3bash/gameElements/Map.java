package com.tinslam.comic.gameElements;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

public abstract class Map{
    protected byte[][] tiles;
    protected Camera camera;

    public Map(Camera camera){
        this.camera = camera;
    }

    public abstract boolean isSolid(Rect collisionBox);
    public abstract boolean isSolidCircle(float x, float y, float radius);
    public abstract void setTiles(byte[][] tiles);
    public abstract void initTiles();
    public abstract void render(Canvas canvas);

    protected void drawBitmap(Bitmap bitmap, int x, int y, float rot, float scale, Canvas canvas){
        Matrix matrix = new Matrix();
        matrix.preRotate(rot + camera.getRotation(), bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        matrix.postTranslate(x + camera.getX(), y + camera.getY());
//        matrix.postScale(scale * camera.getScale(), scale * camera.getScale());
        canvas.drawBitmap(bitmap, matrix, null);
    }

    protected void drawRect(int x, int y, int x2, int y2, Paint paint, Canvas canvas){
        canvas.drawRect(x + camera.getX(), y + camera.getY(), x2 + camera.getX(), y2 + camera.getY(), paint);
    }
}
