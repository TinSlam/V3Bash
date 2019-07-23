package com.tinslam.comic.states;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.tinslam.comic.R;
import com.tinslam.comic.UI.buttons.Button;
import com.tinslam.comic.UI.buttons.rectanglebuttons.RectangleButton;
import com.tinslam.comic.UI.buttons.roundbuttons.RoundButton;
import com.tinslam.comic.UI.graphics.Images;
import com.tinslam.comic.base.Game;
import com.tinslam.comic.networking.Networking;
import com.tinslam.comic.utils.Utils;

public class RankingState extends State{
    private Paint fontPaint = new Paint();
//    private Matrix[] modesMatrix;
    private Matrix[] rankMatrix;
    private Matrix[] namesMatrix;
    private Matrix[] pointsMatrix;
    private int players, modes;
    private float xOffset = (float) Game.getScreenWidth() / 40;
    private float yOffset = (float) Game.getScreenHeight() / 10;
    private float modesDiameter = (float) Game.getScreenHeight() / 5;
    private float tableXOffset = 3 * xOffset + 2 * modesDiameter;
    private float tableWidth = Game.getScreenWidth() - (2 * modesDiameter + 4 * xOffset);
    private float nameSlotWidth =  tableWidth * (float) 45 / 100;
    private float rankSlotWidth = tableWidth * (float) 10 / 100;
    private float pointsSlotWidth = tableWidth * (float) 45 / 100;
    private float slotHeight = Game.getScreenHeight() * (float) 2 / 15;
    private float textPaddingLeft = 8 * Game.density();
    private String[][] usernames;
    private int[][] points;
    private int mode;
    private String[] modeNames;
    private boolean canExit = false;
    private RectangleButton exitButton;

    public RankingState(int players, int modes){
        this.players = players;
        this.modes = modes;
//        modesMatrix = new Matrix[modes + 1];
        rankMatrix = new Matrix[players];
        namesMatrix = new Matrix[players];
        pointsMatrix = new Matrix[players];
//        for(int i = 0; i < modesMatrix.length; i++){
//            modesMatrix[i] = new Matrix();
//        }
        for(int i = 0; i < rankMatrix.length; i++){
            rankMatrix[i] = new Matrix();
            namesMatrix[i] = new Matrix();
            pointsMatrix[i] = new Matrix();
        }
    }

    @Override
    public void disconnected() {

    }

    @Override
    public void connected() {

    }

    @Override
    public void surfaceDestroyed() {

    }

    @Override
    public void handleBackPressed() {

    }

    @Override
    public void handleKeyEvent(KeyEvent event) {

    }

    @Override
    public void startState() {
        ignoresLostConnection = true;
        fontPaint.setTextSize(slotHeight * 3 / 4);
        setPositions();
        sortArrays();
        initButtons();
        Bitmap bitmap = Images.resizeImage(Images.button_empty, 96 * Game.density(), 32 * Game.density());
        exitButton = new RectangleButton(Game.getScreenWidth() - bitmap.getWidth(), Game.getScreenHeight() - bitmap.getHeight(), bitmap, bitmap, Game.Context().getString(R.string.exit_game), true) {
            @Override
            public boolean onDown() {
                return true;
            }

            @Override
            public boolean onUp() {
                if(canExit) Game.setState(new MainMenuState());
                return true;
            }
        };
    }

    private void initButtons(){
        Bitmap bitmap = Images.resizeImage(Images.color_yellow, modesDiameter, modesDiameter);
        for(int i = 0; i < modes + 1; i++){
            final int j = i;
            new RoundButton((int) (((i) / 3) * (modesDiameter + xOffset) + xOffset + modesDiameter / 2), (int) (((i) % 3) * (yOffset + modesDiameter) + yOffset + modesDiameter / 2), bitmap, bitmap, i == 0 ? modeNames[0] : modeNames[modes - i + 1], false) {
                @Override
                public boolean onDown() {
                    return true;
                }

                @Override
                public boolean onUp() {
                    if(j == 0){
                        mode = j;
                    }else{
                        mode = modes + 1 - j;
                    }
                    return true;
                }
            };
        }
    }

    private void sortArrays(){
        usernames = new String[modes + 1][players];
        points = new int[modes + 1][players];
        modeNames = new String[modes + 1];
        modeNames[0] = Game.Context().getString(R.string.mode_total);
        mode = 0;
        for(int i = 0; i < players; i++){
            points[0][i] = Networking.getPoints()[i];
            usernames[0][i] = Networking.getUsernames()[i];
        }
        for(int i = 1; i < modes + 1; i++) {
            for (int j = 0; j < players; j++) {
                points[i][j] = Networking.getModePoints()[i - 1][j];
                usernames[i][j] = Networking.getUsernames()[j];
            }
        }
        for(int i = 0; i < modes + 1; i++){
            Utils.quickSortRanks(points[i], usernames[i], 0, players - 1);
        }
        for(int i = 1; i < modes + 1; i++){
            modeNames[i] = Networking.getModes()[i - 1];
        }
    }

    private void setPositions(){
//        for(int i = 0; i < modes + 1; i++){
//            modesMatrix[i].preScale(modesDiameter / Images.color_yellow.getWidth(), modesDiameter / Images.color_yellow.getHeight());
//            modesMatrix[i].postTranslate(((i) / 3) * (modesDiameter + xOffset) + xOffset, ((i) % 3) * (yOffset + modesDiameter) + yOffset);
//        }
        for(int i = 0; i < players; i++){
            rankMatrix[i].preScale(rankSlotWidth / Images.ranking_slot2.getWidth(), slotHeight / Images.ranking_slot2.getHeight());
            rankMatrix[i].postTranslate(tableXOffset, (i * slotHeight) + yOffset);
            namesMatrix[i].preScale(nameSlotWidth / Images.ranking_slot.getWidth(), slotHeight / Images.ranking_slot.getHeight());
            namesMatrix[i].postTranslate(tableXOffset + rankSlotWidth, (i * slotHeight) + yOffset);
            pointsMatrix[i].preScale(pointsSlotWidth / Images.ranking_slot.getWidth(), slotHeight / Images.ranking_slot.getHeight());
            pointsMatrix[i].postTranslate(tableXOffset + rankSlotWidth + nameSlotWidth, (i * slotHeight) + yOffset);
        }
    }

    @Override
    public void tick() {

    }

    @Override
    public void render(Canvas canvas) {
        canvas.drawBitmap(Images.background_main_menu, null, Game.getScreenRect(), null);
        Button.renderButtons(canvas, buttons, buttonsLock);
        if(canExit) exitButton.render(canvas);
//        for(int i = 0; i < modes + 1; i++){
//            canvas.drawBitmap(Images.color_yellow, modesMatrix[i], null);
//        }
        for(int i = 0; i < players; i++){
            fontPaint.setTextAlign(Paint.Align.CENTER);

            canvas.drawBitmap(Images.ranking_slot2, rankMatrix[i], null);
            Utils.drawTextDynamicSize("" + (i + 1), tableXOffset + rankSlotWidth / 2, yOffset + i * slotHeight + slotHeight / 2, rankSlotWidth - 2 * textPaddingLeft, fontPaint, canvas);

            fontPaint.setTextAlign(Paint.Align.LEFT);

            canvas.drawBitmap(Images.ranking_slot, namesMatrix[i], null);
            Utils.drawTextDynamicSize(usernames[mode][players - 1 - i], tableXOffset + rankSlotWidth + textPaddingLeft, yOffset + i * slotHeight + slotHeight / 2, nameSlotWidth - 2 * textPaddingLeft, fontPaint, canvas);

            canvas.drawBitmap(Images.ranking_slot, pointsMatrix[i], null);
            Utils.drawTextDynamicSize(points[mode][players - 1 - i] + "", tableXOffset + rankSlotWidth + nameSlotWidth + textPaddingLeft, yOffset + i * slotHeight + slotHeight / 2, pointsSlotWidth - 2 * textPaddingLeft, fontPaint, canvas);
        }
    }

    @Override
    public boolean onActionDown(MotionEvent event) {
        return Button.onActionDown(event, buttons, buttonsLock);
    }

    @Override
    public boolean onActionPointerDown(MotionEvent event) {
        return Button.onActionPointerDown(event, buttons, buttonsLock);
    }

    @Override
    public boolean onActionMove(MotionEvent event) {
        return Button.onActionMove(event, buttons, buttonsLock);
    }

    @Override
    public boolean onActionUp(MotionEvent event) {
        return Button.onActionUp(event, buttons, buttonsLock);
    }

    @Override
    public boolean onActionPointerUp(MotionEvent event) {
        return Button.onActionPointerUp(event, buttons, buttonsLock);
    }

    public boolean isCanExit() {
        return canExit;
    }

    public void setCanExit(boolean canExit) {
        this.canExit = canExit;
    }

    @Override
    public void endState() {
        buttons.clear();
    }
}
