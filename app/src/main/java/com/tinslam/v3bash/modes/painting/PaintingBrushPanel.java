package com.tinslam.comic.modes.painting;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.tinslam.comic.UI.graphics.Images;
import com.tinslam.comic.UI.buttons.rectanglebuttons.RectangleButton;
import com.tinslam.comic.base.Game;
import com.tinslam.comic.utils.Consts;

public class PaintingBrushPanel {
    private static Paint borderPaint = new Paint();
    private static Paint backgroundPaint = new Paint();
    private int xOffset = (int) (16 * Game.density()), yOffset = (int) (16 * Game.density());
    private int width = (int) (64 * Game.density()), height = (int) (6 * 32 * Game.density());
    private static boolean hide = true;
    private RectangleButton fillButton, styleRectButton, styleOvalButton;

    public void prepare(){
//        yOffset = (Game.getScreenHeight() - height) / 2;
        xOffset = 0;
        yOffset = (int) (32 * Game.density());
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setARGB(255, 0, 0, 0);
        backgroundPaint.setARGB(150, 200, 200, 200);
        fillButton = new RectangleButton((int) (xOffset + 8 * Game.density()), (int) (yOffset + 8 * Game.density()), Images.brush_fill, Images.brush_fill, true){
            @Override
            public boolean onDown() {
                if(hide) return false;

                return true;
            }

            @Override
            public boolean onUp() {
                if(hide) return false;
                PaintingCanvas.setBrushStyle(Consts.BRUSH_FILL);
                return true;
            }
        };
        styleRectButton = new RectangleButton((int) (xOffset + 8 * Game.density()), (int) (yOffset + (8 + 48 + 8) * Game.density()), Images.brush_style_rect, Images.brush_style_rect, true){
            @Override
            public boolean onDown() {
                if(hide) return false;

                return true;
            }

            @Override
            public boolean onUp() {
                if(hide) return false;
                PaintingCanvas.setBrushStyle(Consts.BRUSH_STYLE_RECT);
                return true;
            }
        };
        styleOvalButton = new RectangleButton((int) (xOffset + 8 * Game.density()), (int) (yOffset + (8 + 48 + 8 + 48 + 8) * Game.density()), Images.brush_style_oval, Images.brush_style_oval, true){
            @Override
            public boolean onDown() {
                if(hide) return false;

                return true;
            }

            @Override
            public boolean onUp() {
                if(hide) return false;

                return true;
            }
        };
    }

    public void render(Canvas canvas){
        if(hide){
            return;
        }else{
            canvas.drawRect(xOffset, yOffset, xOffset + width, yOffset + height, backgroundPaint);
            canvas.drawRect(xOffset, yOffset, xOffset + width, yOffset + height, borderPaint);
            fillButton.render(canvas);
            styleOvalButton.render(canvas);
            styleRectButton.render(canvas);
        }
    }

    public static void toggleHide(){
        hide = !hide;
    }

    public static void reset() {
        hide = true;
    }
}
