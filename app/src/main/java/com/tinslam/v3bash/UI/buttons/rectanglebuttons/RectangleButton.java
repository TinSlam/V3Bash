package com.tinslam.comic.UI.buttons.rectanglebuttons;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.tinslam.comic.UI.graphics.Images;
import com.tinslam.comic.UI.buttons.Button;

public abstract class RectangleButton extends Button {
    protected Rect rect;
    protected String string = "";
    protected Paint fontPaint = new Paint();

    public RectangleButton(int x, int y, Bitmap image, Bitmap imageOnClick, boolean manualRender) {
        super(image, imageOnClick, manualRender);

        rect = new Rect(x, y, x + image.getWidth(), y + image.getHeight());
    }

    public RectangleButton(int x, int y, int x2, int y2) {
        super(null, null, false);

        rect = new Rect(x, y, x2, y2);
    }

    public RectangleButton(Rect rect){
        super(null, null, false);

        this.rect = new Rect(0, 0, 0, 0);
        this.rect.set(rect);
    }

    public RectangleButton(int x, int y, Bitmap image, Bitmap imageOnClick, String string, boolean manualRender) {
        super(image, imageOnClick, manualRender);

        fontPaint.setColor(Color.BLACK);
        fontPaint.setTextSize(image.getHeight() / 2);
        fontPaint.setTextAlign(Paint.Align.CENTER);
        this.string = string;
        rect = new Rect(x, y, x + image.getWidth(), y + image.getHeight());
    }

    @Override
    public boolean isTouched(int mx, int my){
        return rect.contains(mx, my);
    }

    @Override
    public void render(Canvas canvas){
        canvas.drawBitmap(currentImage, rect.left, rect.top, paint);
        if(!string.equals("")) canvas.drawText(string, rect.centerX(), rect.centerY() + fontPaint.getTextSize() / 4, fontPaint);
    }

    public void resizeImage(int width, int height){
        int hTranslation = (image.getWidth() - width) / 2;
        int vTranslation = (image.getHeight() - height) / 2;
        rect.set(rect.left + hTranslation, rect.top + vTranslation, rect.left + width + hTranslation, rect.top + height + vTranslation);
        int t = 2;
        if(currentImage == image){
            t = 1;
        }
        setImage(Images.resizeImage(image, width, height));
        setImageOnClick(Images.resizeImage(imageOnClick, width, height));
        if(t == 1){
            currentImage = image;
        }else{
            currentImage = imageOnClick;
        }
        fontPaint.setTextSize(image.getHeight() / 2);
    }

    public void reposition(int x, int y){
        System.out.println(x + " " + y + " " + image.getWidth() + " " + image.getHeight());
        rect.set(x, y, x + image.getWidth(), y + image.getHeight());
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public Paint getFontPaint() {
        return fontPaint;
    }
}
