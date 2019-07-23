package com.tinslam.comic.modes.painting;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.tinslam.comic.base.Game;
import com.tinslam.comic.utils.TextRenderer;

public class PaintingInfoPanel {
    private static int sec = 90;
    private static String subject = "";
    private static int xOffset = 0, yOffset = 0, width = (int) (64 * Game.density()), height = (int) (32 * Game.density());
    private static Paint backgroundPaint = new Paint(), backgroundPaint2 = new Paint();
    private static Paint borderPaint = new Paint();
    private TextRenderer subjectTR;
    private TextRenderer timeTR;

    void prepare(){
        backgroundPaint.setARGB(255, 0, 204, 102);
        backgroundPaint2.setARGB(255, 0, 204, 255);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setARGB(255, 0, 0, 0);
        subjectTR = new TextRenderer(subject, xOffset + width + 8 * Game.density(), yOffset + height / 2, 1000 * Game.density(), 16 * Game.density(), Paint.Align.LEFT);
        timeTR = new TextRenderer(sec + "", xOffset + width / 2, yOffset + height / 2, width - 2 * 8 * Game.density(), 16 * Game.density(), Paint.Align.CENTER);
    }

    public void render(Canvas canvas){
        canvas.drawRect(xOffset, yOffset, xOffset + width, yOffset + height, backgroundPaint);
        canvas.drawRect(xOffset, yOffset, xOffset + width, yOffset + height, borderPaint);
        canvas.drawRect(xOffset + width, yOffset, xOffset + width + timeTR.getWidth() + 16 * Game.density(), yOffset + height, backgroundPaint2);
        canvas.drawRect(xOffset + width, yOffset, xOffset + width + subjectTR.getWidth() + 16 * Game.density(), yOffset + height, borderPaint);
        subjectTR.drawText(canvas);
        timeTR.drawText(canvas);
        //        canvas.drawText(subject, xOffset + width + 8 * Game.density(), yOffset + height * 3 / 4, fontPaint2);
//        canvas.drawText("" + sec, xOffset + width / 2, yOffset + height * 3 / 4, fontPaint);
    }

    static void setSec(int sec) {
        PaintingInfoPanel.sec = sec;
    }

    static void setSubject(String subject) {
        PaintingInfoPanel.subject = subject;
    }
}
