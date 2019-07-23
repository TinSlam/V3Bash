package com.tinslam.comic.modes.painting;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.tinslam.comic.R;
import com.tinslam.comic.UI.graphics.Images;
import com.tinslam.comic.UI.buttons.rectanglebuttons.RectangleButton;
import com.tinslam.comic.base.Game;
import com.tinslam.comic.utils.TextRenderer;

class PaintingSettingsPanel {
    private static Paint fontPaint = new Paint();
    private static RectangleButton paletteLayoutButton, canvasBackgroundButton, gridButton;
    private static boolean hide = true;
    private static int leftPadding = (int) (8 * Game.density());
    private static TextRenderer gridTR, backgroundTR, paletteLayoutTR;

    void prepare(){
        fontPaint.setTextSize(16 * Game.density());
        fontPaint.setTextAlign(Paint.Align.CENTER);
        fontPaint.setARGB(255, 0, 0, 0);
        int width = (int) (128 * Game.density());
        int xOffset = (int) (Game.getScreenWidth() - 8 * Game.density() - width);
        int yOffset = (int) (64 * Game.density());
        Bitmap image = Images.resizeImage(Images.button_empty, 128 * Game.density(), 32 * Game.density());
        canvasBackgroundButton = new RectangleButton(xOffset, yOffset, image, image, true) {
            @Override
            public boolean onDown() {
                return !hide;
            }

            @Override
            public boolean onUp() {
                if(hide) return false;
                PaintingCanvas.setBackgroundColor(PaintingCanvas.getColor());
                return true;
            }
        };
        paletteLayoutButton = new RectangleButton(xOffset, (int) (yOffset + 64 * Game.density()), image, image, true) {
            @Override
            public boolean onDown() {
                return !hide;
            }

            @Override
            public boolean onUp() {
                if(hide) return false;
                PaintingPalette.toggleLayout();
                return true;
            }
        };
        gridButton = new RectangleButton(xOffset, (int) (yOffset + 128 * Game.density()), image, image, true) {
            @Override
            public boolean onDown() {
                return !hide;
            }

            @Override
            public boolean onUp() {
                if(hide) return false;
                PaintingCanvas.toggleGrid();
                return true;
            }
        };
        backgroundTR = new TextRenderer(Game.Context().getString(R.string.painting_setting_change_bg), xOffset + image.getWidth() / 2, yOffset + canvasBackgroundButton.getImage().getHeight() / 2, Game.getScreenWidth() - xOffset - 2 * leftPadding, Game.getScreenHeight() / 20, Paint.Align.CENTER);
        paletteLayoutTR = new TextRenderer(Game.Context().getString(R.string.painting_setting_palette_layout), xOffset + image.getWidth() / 2, (int) (yOffset + 64 * Game.density()) + paletteLayoutButton.getImage().getHeight() / 2, Game.getScreenWidth() - xOffset - 2 * leftPadding, Game.getScreenHeight() / 20, Paint.Align.CENTER);
        gridTR = new TextRenderer(Game.Context().getString(R.string.painting_setting_grid), xOffset + image.getWidth() / 2, (int) (yOffset + 128 * Game.density()) + gridButton.getImage().getHeight() / 2, Game.getScreenWidth() - xOffset - 2 * leftPadding, Game.getScreenHeight() / 20, Paint.Align.CENTER);
    }

    public void render(Canvas canvas){
        if(hide) return;
        canvasBackgroundButton.render(canvas);
        paletteLayoutButton.render(canvas);
        gridButton.render(canvas);
        backgroundTR.drawText(canvas);
        gridTR.drawText(canvas);
        paletteLayoutTR.drawText(canvas);
    }

    static void toggleHide(){
        PaintingSettingsPanel.hide = !PaintingSettingsPanel.hide;
    }

    public static void reset() {
        hide = true;
    }
}
