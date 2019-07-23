package com.tinslam.comic.UI.buttons.roundbuttons;

import android.graphics.Bitmap;

import com.tinslam.comic.modes.painting.PaintingCanvas;
import com.tinslam.comic.modes.painting.PaintingPalette;
import com.tinslam.comic.utils.Consts;

public class ColorButton extends RoundButton{
    private byte colorCode;
    private boolean isShown = false;

    public ColorButton(int x, int y, Bitmap image, Bitmap onClickImage, byte colorCode) {
        super(x, y, image, onClickImage, true);

        this.colorCode = colorCode;
    }

    @Override
    public boolean onDown() {
        if(!isShown || PaintingPalette.isHidden()) return false;
        return true;
    }

    @Override
    public boolean onUp() {
        if(!isShown || PaintingPalette.isHidden()) return false;
        if(colorCode == Consts.COLOR_NO_COLOR) return true;
        PaintingCanvas.setColor(colorCode);
        return true;
    }

//    @Override
//    public void render(Canvas canvas){
//        paint.setAlpha(100);
//        if(isShown) canvas.drawCircle(x - radius / 2, y - radius / 2, radius / 2, paint);
//        super.render(canvas);
//    }

    public boolean isShown() {
        return isShown;
    }

    public void setShown(boolean shown) {
        isShown = shown;
    }
}
