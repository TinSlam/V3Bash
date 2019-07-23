package com.tinslam.comic.modes.painting;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.tinslam.comic.UI.graphics.Images;
import com.tinslam.comic.UI.buttons.rectanglebuttons.RectangleButton;
import com.tinslam.comic.base.Game;

public class PaintingToolsPanel {
    private static Paint borderPaint = new Paint();
    private static Paint backgroundPaint = new Paint();
    private static int xOffset, yOffset, width, height;
    private static RectangleButton brushButton, paletteButton, undoButton, redoButton, settingsButton;

    public void prepare(){
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setColor(Color.WHITE);
        width = (int) (5 * 48 * Game.density());
        height = (int) (48 * Game.density());
        xOffset = Game.getScreenWidth() - width;
        yOffset = 0;
        brushButton = new RectangleButton(xOffset, yOffset, Images.brush, Images.brush, true) {
            @Override
            public boolean onDown() {
                return true;
            }

            @Override
            public boolean onUp() {
                PaintingBrushPanel.toggleHide();
                return true;
            }
        };
        paletteButton = new RectangleButton((int) (xOffset + 48 * Game.density()), yOffset, Images.palette, Images.palette, true) {
            @Override
            public boolean onDown() {
                return true;
            }

            @Override
            public boolean onUp() {
                PaintingPalette.toggleHide();
                return true;
            }
        };
        undoButton = new RectangleButton((int) (xOffset + 96 * Game.density()), yOffset, Images.undo, Images.undo, true) {
            @Override
            public boolean onDown() {
                return true;
            }

            @Override
            public boolean onUp() {
                PaintingCanvas.undo();
                return true;
            }
        };
        redoButton = new RectangleButton((int) (xOffset + (48 + 96) * Game.density()), yOffset, Images.redo, Images.redo, true) {
            @Override
            public boolean onDown() {
                return true;
            }

            @Override
            public boolean onUp() {
                PaintingCanvas.redo();
                return true;
            }
        };
        settingsButton = new RectangleButton((int) (xOffset + (96 + 96) * Game.density()), yOffset, Images.settings, Images.settings, true) {
            @Override
            public boolean onDown() {
                return true;
            }

            @Override
            public boolean onUp() {
                PaintingGameState.settings();
                return true;
            }
        };
    }

    public void render(Canvas canvas){
        canvas.drawRect(xOffset, yOffset, xOffset + width, yOffset + height, backgroundPaint);
        paletteButton.render(canvas);
        brushButton.render(canvas);
        undoButton.render(canvas);
        redoButton.render(canvas);
        settingsButton.render(canvas);
        canvas.drawRect(xOffset, yOffset, xOffset + 48 * Game.density(), yOffset + height, borderPaint);
        canvas.drawRect(xOffset  + 48 * Game.density(), yOffset, xOffset + (96) * Game.density(), yOffset + height, borderPaint);
        canvas.drawRect(xOffset  + 96 * Game.density(), yOffset, xOffset + (96 + 48) * Game.density(), yOffset + height, borderPaint);
        canvas.drawRect(xOffset  + (96 + 48) * Game.density(), yOffset, xOffset + (96 + 96) * Game.density(), yOffset + height, borderPaint);
        canvas.drawRect(xOffset  + (96 + 96) * Game.density(), yOffset, xOffset + (96 + 96 + 48) * Game.density(), yOffset + height, borderPaint);
    }

    public static void reset() {

    }
}
