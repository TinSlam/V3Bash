package com.tinslam.comic.modes.painting;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.tinslam.comic.UI.buttons.Button;
import com.tinslam.comic.base.Game;
import com.tinslam.comic.states.GameState;
import com.tinslam.comic.states.MainMenuState;
import com.tinslam.comic.utils.Utils;

public class PaintingPracticeState extends GameState {
    private static Paint rankingFontPaint = new Paint();
    private static PaintingPalette palette;
    private static PaintingBrushPanel brushPanel;
    private static PaintingToolsPanel toolsPanel;
    private static PaintingCanvas paintingCanvas;
    private static PaintingSettingsPanel paintingSettingsPanel;
    private static int[] points = new int[6];

    @Override
    public void disconnected() {

    }

    @Override
    public void connected() {

    }

    @Override
    public void trophyReached() {

    }

    @Override
    public void surfaceDestroyed() {

    }

    @Override
    public void handleBackPressed() {
        Game.setState(new MainMenuState());
    }

    @Override
    public void handleKeyEvent(KeyEvent event) {

    }

    @Override
    public void startState() {
        ignoresLostConnection = true;
        for(int i = 0; i < 6; i++) points[i] = 0;
        paintingCanvas = new PaintingCanvas();
        brushPanel = new PaintingBrushPanel();
        palette = new PaintingPalette();
        toolsPanel = new PaintingToolsPanel();
        paintingSettingsPanel = new PaintingSettingsPanel();
        backGroundPaint.setARGB(100, 200, 200, 100);
        rankingFontPaint.setTextSize(Utils.heightPercentage(5));
        rankingFontPaint.setTextAlign(Paint.Align.LEFT);
        prepareDrawingStage();
    }

    private static void prepareDrawingStage(){
        PaintingCanvas.prepare();
        palette.prepare();
        brushPanel.prepare();
        toolsPanel.prepare();
        paintingSettingsPanel.prepare();
    }

    @Override
    public void tick() {

    }

    @Override
    public void render(Canvas canvas) {
        canvas.drawRect(0, 0, Game.getScreenWidth(), Game.getScreenHeight(), backGroundPaint);
        Button.renderButtons(canvas, buttons, buttonsLock);
        paintingCanvas.render(canvas);
        palette.render(canvas);
        brushPanel.render(canvas);
        toolsPanel.render(canvas);
        paintingSettingsPanel.render(canvas);
    }

    @Override
    public boolean onActionDown(MotionEvent event) {
        return Button.onActionDown(event, buttons, buttonsLock) || PaintingCanvas.onActionDown(event);
    }

    @Override
    public boolean onActionPointerDown(MotionEvent event) {
        return Button.onActionPointerDown(event, buttons, buttonsLock) || PaintingCanvas.onActionPointerDown(event);

    }

    @Override
    public boolean onActionMove(MotionEvent event) {
        return PaintingCanvas.onActionMove(event);
    }

    @Override
    public boolean onActionUp(MotionEvent event) {
        return Button.onActionUp(event, buttons, buttonsLock) || PaintingCanvas.onActionUp(event);

    }

    @Override
    public boolean onActionPointerUp(MotionEvent event) {
        return Button.onActionPointerUp(event, buttons, buttonsLock) || PaintingCanvas.onActionPointerUp(event);

    }

    @Override
    public void endState() {
        buttons.clear();
        PaintingCanvas.reset();
        PaintingBrushPanel.reset();
        PaintingSettingsPanel.reset();
        PaintingPalette.reset();
        PaintingToolsPanel.reset();
    }
}