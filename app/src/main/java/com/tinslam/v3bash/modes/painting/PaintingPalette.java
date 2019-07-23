package com.tinslam.comic.modes.painting;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.tinslam.comic.UI.graphics.Images;
import com.tinslam.comic.UI.buttons.rectanglebuttons.RectangleButton;
import com.tinslam.comic.UI.buttons.roundbuttons.ColorButton;
import com.tinslam.comic.base.Game;
import com.tinslam.comic.utils.Consts;

import java.util.ArrayList;

public class PaintingPalette {
    private static ArrayList<ColorButton> colors = new ArrayList<>();
    private static Object colorsLock = new Object();
    private static Paint borderPaint = new Paint();
    private static Paint backgroundPaint = new Paint();
    private static byte layout = Consts.LAYOUT_PALETTE_ONE_ROW;
    private static int maxColors = 48, spots;
    private static int xOffset = (int) (64 * Game.density()), yOffset = (int) (8 * Game.density()); // Gets changed in constructor !
    private static int width;
    private static int index = 0;
    private static boolean hide = true;
    private static RectangleButton leftButton, rightButton;

    public void prepare(){
        int x = Game.getScreenWidth() - 2 * xOffset;
        spots = (int) (x / (64 * Game.density()));
        width = (int) (spots * 64 * Game.density());
        xOffset = ((Game.getScreenWidth() - width) / 2);
//        xOffset = (int) (Game.getScreenWidth() - width - PaintingCanvas.getRightOffset() - 32 * Game.density());
        leftButton = new RectangleButton((int) (xOffset - 32 * Game.density()), (int) (Game.getScreenHeight() - yOffset - 64 * Game.density())
                , Images.palette_left_button, Images.palette_left_button, true) {
            @Override
            public boolean onDown() {
                if(hide) return false;

                return true;
            }

            @Override
            public boolean onUp() {
                if(hide) return false;
                switch(layout){
                    case Consts.LAYOUT_PALETTE_ONE_ROW :
                        if(index <= 0){
                            index = 0;
                        }else{
                            index--;
                            synchronized(colorsLock){
                                colors.get(index + spots).setShown(false);
                            }
                        }
                        break;

                    case Consts.LAYOUT_PALETTE_TWO_ROW :
                        if(index <= 0){
                            index = 0;
                        }else{
                            index -= 2;
                            synchronized(colorsLock){
                                colors.get(index + spots * 4).setShown(false);
                                colors.get(index + spots * 4 + 1).setShown(false);
                            }
                        }
                        break;
                }
                setColorPositions();
                return true;
            }
        };
        rightButton = new RectangleButton((int) (width + xOffset), (int) (Game.getScreenHeight() - yOffset - 64 * Game.density())
                , Images.palette_right_button, Images.palette_right_button, true) {
            @Override
            public boolean onDown() {
                if(hide) return false;

                return true;
            }

            @Override
            public boolean onUp() {
                if(hide) return false;
                switch(layout){
                    case Consts.LAYOUT_PALETTE_ONE_ROW :
                        if(index >= colors.size() - spots){
                            index = colors.size() - spots;
                        }else{
                            index++;
                            synchronized(colorsLock){
                                colors.get(index - 1).setShown(false);
                            }
                        }
                        break;

                    case Consts.LAYOUT_PALETTE_TWO_ROW :
                        if(index >= colors.size() - spots * 4){
                            index = colors.size() - spots * 4;
                        }else{
                            index += 2;
                            synchronized(colorsLock){
                                colors.get(index - 1).setShown(false);
                                colors.get(index - 2).setShown(false);
                            }
                        }
                        break;
                }
                setColorPositions();
                return true;
            }
        };
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setARGB(255, 0, 0, 0);
        backgroundPaint.setARGB(80, 0, 0, 0);
        synchronized(colorsLock){
            colors.add(new ColorButton(0, 0, Images.color_black, Images.color_black, Consts.COLOR_BLACK));
            colors.add(new ColorButton(0, 0, Images.color_white, Images.color_white, Consts.COLOR_WHITE));
            colors.add(new ColorButton(0, 0, Images.color_red, Images.color_red, Consts.COLOR_RED));
            colors.add(new ColorButton(0, 0, Images.color_green, Images.color_green, Consts.COLOR_GREEN));
            colors.add(new ColorButton(0, 0, Images.color_blue, Images.color_blue, Consts.COLOR_BLUE));
            colors.add(new ColorButton(0, 0, Images.color_yellow, Images.color_yellow, Consts.COLOR_YELLOW));
            int length = colors.size();
            for(int i = length; i < maxColors; i++){
                colors.add(new ColorButton(0, 0, Images.color_locked, Images.color_locked, Consts.COLOR_NO_COLOR));
            }
        }
        index = 0;
        setColorPositions();
        switch(layout){
            case Consts.LAYOUT_PALETTE_ONE_ROW :
                synchronized(colorsLock){
                    for(int i = 0; i < colors.size(); i++){
                        colors.get(i).resizeImage((int) (32 * Game.density()));
                    }
                }
                break;

            case Consts.LAYOUT_PALETTE_TWO_ROW :
                synchronized(colorsLock){
                    for(int i = 0; i < colors.size(); i++){
                        colors.get(i).resizeImage((int) (16 * Game.density()));
                    }
                }
                break;
        }
    }

    public static void toggleLayout(){
        if(layout == Consts.LAYOUT_PALETTE_ONE_ROW) layout = Consts.LAYOUT_PALETTE_TWO_ROW;
        else if(layout == Consts.LAYOUT_PALETTE_TWO_ROW) layout = Consts.LAYOUT_PALETTE_ONE_ROW;
        index = 0;
        switch(layout){
            case Consts.LAYOUT_PALETTE_ONE_ROW :
                synchronized(colorsLock){
                    for(int i = 0; i < colors.size(); i++){
                        colors.get(i).resizeImage((int) (32 * Game.density()));
                    }
                }
                break;

            case Consts.LAYOUT_PALETTE_TWO_ROW :
                synchronized(colorsLock){
                    for(int i = 0; i < colors.size(); i++){
                        colors.get(i).resizeImage((int) (16 * Game.density()));
                    }
                }
                break;
        }
        synchronized(colorsLock){
            for(ColorButton x : colors){
                x.setShown(false);
            }
        }
        setColorPositions();
    }

    public static void setColorPositions(){
        switch(layout){
            case Consts.LAYOUT_PALETTE_ONE_ROW :
                synchronized(colorsLock){
                    for(int i = index; i < index + spots; i++){
                        colors.get(i).setShown(true);
                        colors.get(i).setX((int) (64 * Game.density() * (i + 1 - index) + xOffset - 32 * Game.density()));
                        colors.get(i).setY((int) (Game.getScreenHeight() - yOffset - 32 * Game.density()));
                    }
                }
                break;

            case Consts.LAYOUT_PALETTE_TWO_ROW :
                synchronized(colorsLock){
                    for(int i = index; i < index + 4 * spots; i += 2){
                        colors.get(i).setShown(true);
                        colors.get(i).setX((int) (16 * Game.density() * (i + 1 - index) + xOffset));
                        colors.get(i).setY((int) (Game.getScreenHeight() - (32) * Game.density() - yOffset - 16 * Game.density()));
                        colors.get(i + 1).setShown(true);
                        colors.get(i + 1).setX((int) (16 * Game.density() * (i + 1 - index) + xOffset));
                        colors.get(i + 1).setY((int) (Game.getScreenHeight() - yOffset - 16 * Game.density()));
                    }
                }
                break;
        }
    }

    public void render(Canvas canvas){
        if(hide){
            return;
        }else{
            canvas.drawRect(xOffset, Game.getScreenHeight() - yOffset - 64 * Game.density(), xOffset + width, Game.getScreenHeight() - yOffset, backgroundPaint);
            canvas.drawRect(xOffset, Game.getScreenHeight() - yOffset - 64 * Game.density(), xOffset + width, Game.getScreenHeight() - yOffset, borderPaint);
            renderColors(canvas);
            leftButton.render(canvas);
            rightButton.render(canvas);
        }
    }


    public void renderColors(Canvas canvas){
        switch(layout){
            case Consts.LAYOUT_PALETTE_ONE_ROW :
                synchronized(colorsLock){
                    for(int i = index; i < index + spots; i++){
                        colors.get(i).render(canvas);
                    }
                }
                break;

            case Consts.LAYOUT_PALETTE_TWO_ROW :
                synchronized(colorsLock){
                    for(int i = index; i < index + 4 * spots; i += 2){
                        colors.get(i).render(canvas);
                        colors.get(i + 1).render(canvas);
                    }
                }
                break;
        }
    }

    public static void toggleHide(){
        hide = !hide;
    }

    public static boolean isHidden(){
        return hide;
    }

    public static void reset() {
        colors.clear();
        hide = true;
        index = 0;
    }
}
