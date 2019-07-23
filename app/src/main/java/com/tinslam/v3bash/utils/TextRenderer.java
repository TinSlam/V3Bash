package com.tinslam.comic.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.tinslam.comic.base.Game;

public class TextRenderer{
    private Paint paint = new Paint();
    private String text;
    private float yOffset = 0;
    private float x, y;
    private float width;

    public TextRenderer(String text, float x, float y, float maxWidth, float maxPaintSize, Paint.Align align){
        this.text = text;
        this.x = x;
        this.y = y;
        paint.setTextAlign(align);
        paint.setTextSize(maxPaintSize);
        float paintSize = paint.getTextSize();
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        while(bounds.width() > maxWidth){
            float size = paint.getTextSize() - 1;
            if(size <= 5 * Game.density()){
                paint.setTextSize(paintSize);
                text = "Won't fit";
                paint.getTextBounds(text, 0, text.length(), bounds);
                break;
            }
            paint.setTextSize(size);
            paint.getTextBounds(text, 0, text.length(), bounds);
        }
        yOffset = bounds.height() / 4;
        width = bounds.width();
    }

    public void drawText(Canvas canvas){
        canvas.drawText(text, x, y + yOffset, paint);
    }

    public float getWidth() {
        return width;
    }
}
