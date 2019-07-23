package com.tinslam.comic.UI.buttons.roundbuttons;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.tinslam.comic.UI.graphics.Images;
import com.tinslam.comic.UI.buttons.Button;
import com.tinslam.comic.base.Game;
import com.tinslam.comic.utils.TextRenderer;
import com.tinslam.comic.utils.Utils;

public abstract class RoundButton extends Button {
    protected int x, y, radius;
    protected TextRenderer textRenderer = null;

    public RoundButton(int x, int y, Bitmap image, Bitmap onClickImage, boolean manualRender){
        super(image, onClickImage, manualRender);

        this.x = x;
        this.y = y;
        radius = image.getWidth() / 2;
    }

    public RoundButton(int x, int y, Bitmap image, Bitmap onClickImage, String text, boolean manualRender){
        super(image, onClickImage, manualRender);

        this.x = x;
        this.y = y;
        radius = image.getWidth() / 2;
        textRenderer = new TextRenderer(text, x , y - image.getHeight() * (float) 3 / 100, image.getWidth() - 2 * Game.density() * 4, image.getHeight() / 2, Paint.Align.CENTER);
    }

    @Override
    public boolean isTouched(int mx, int my){
        if(Utils.distance(mx, my, x, y) < radius) return true;

        return false;
    }

    @Override
    public void render(Canvas canvas){
//        canvas.drawCircle(x, y, radius, paint);
        canvas.drawBitmap(currentImage, x - radius, y - radius, paint);
        if(textRenderer != null){
            textRenderer.drawText(canvas);
        }
    }

    public void resizeImage(int radius){
        this.radius = radius;
        int t = 2;
        if(currentImage == image){
            t = 1;
        }
        setImage(Images.resizeImage(image, radius * 2, radius * 2));
        setImageOnClick(Images.resizeImage(imageOnClick, radius * 2, radius * 2));
        if(t == 1){
            currentImage = image;
        }else{
            currentImage = imageOnClick;
        }
    }

    public int getX(){
        return x;
    }

    public void setX(int x){
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
