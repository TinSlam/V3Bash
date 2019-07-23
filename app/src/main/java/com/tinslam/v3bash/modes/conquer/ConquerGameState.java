package com.tinslam.comic.modes.conquer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.tinslam.comic.UI.buttons.Button;
import com.tinslam.comic.UI.graphics.Images;
import com.tinslam.comic.base.Game;
import com.tinslam.comic.gameElements.Camera;
import com.tinslam.comic.gameElements.entity.Entity;
import com.tinslam.comic.gameElements.entity.movingEntity.ConquerTroop;
import com.tinslam.comic.gameElements.entity.movingEntity.Player;
import com.tinslam.comic.gameElements.entity.staticEntity.ConquerTower;
import com.tinslam.comic.networking.Networking;
import com.tinslam.comic.states.GameState;
import com.tinslam.comic.utils.Consts;
import com.tinslam.comic.utils.Utils;

import java.util.ArrayList;

public class ConquerGameState extends GameState{
    private static ArrayList<Player> players;
    private static Object playersLock;
    private static Paint fadePaint = new Paint();
    private static Paint fontPaint = new Paint();
    private boolean transitionFading = true;
    private static ConquerGameState conquerGameState;
    private static Camera camera;
    private static float armyRatio = 0.5f;
    private static String enemy = "n";
    private static boolean player2 = false;

    public ConquerGameState(String username, boolean player2Var){
        enemy = username;
        player2 = player2Var;

        fontPaint.setTextSize(20 * Game.density());
        fontPaint.setTextAlign(Paint.Align.CENTER);
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
        conquerGameState = this;
//        setLoading(true);
        ignoresLostConnection = true;
//        player = new Player(""
//                , Consts.HIDE_AND_SEEK_START_POSITION_X * Consts.HIDE_AND_SEEK_TILE_WIDTH + (float) (Consts.HIDE_AND_SEEK_TILE_WIDTH - Images.player.getWidth()) / 2
//                , Consts.HIDE_AND_SEEK_START_POSITION_Y * Consts.HIDE_AND_SEEK_TILE_HEIGHT + (float) (Consts.HIDE_AND_SEEK_TILE_HEIGHT - Images.player.getHeight()) / 2
//                , Images.player, camera, null);
        players = new ArrayList();
        camera = new Camera();
        playersLock = new Object();
        fadePaint.setColor(Color.BLACK);
        fadePaint.setAlpha(255);
        spawnTowers();
        createPlayers();
    }

    public static void transportTroops(int srcIndex, int dstIndex, int num, String username, int time){
        ConquerTower src, dst;
        synchronized(ConquerTower.getConquerTowersLock()){
            src = ConquerTower.getConquerTowers().get(srcIndex);
            dst = ConquerTower.getConquerTowers().get(dstIndex);
        }
        for(int i = 0; i < num; i++){
            int tx = (int) (Math.random() * 64 * Game.density());
            int ty = (int) (Math.random() * 16 * Game.density());

            int x = (int) (src.getVisualXCenter() - 64 * Game.density() + tx);
            int y = (int) (src.getCollisionBox().bottom + ty);
            if(username.equalsIgnoreCase(Networking.getUsername())){
                new ConquerTroop(x, y, dst.getVisualXCenter(), dst.getVisualYCenter() + ty, time, Images.conquer_troops, ConquerGameState.getCamera());
            }else{
                new ConquerTroop(x, y, dst.getVisualXCenter(), dst.getVisualYCenter() + ty, time, Images.conquer_troops_enemy, ConquerGameState.getCamera());
            }
        }
        src.setTroops(src.getTroops() - num);
    }

    public static void updateTroops(int[] array){
        synchronized(ConquerTower.getConquerTowersLock()){
            for(int i = 0; i < array.length; i++){

                ConquerTower.getConquerTowers().get(i).setTroops(array[i]);
            }
        }
    }

    public static void troopsReached(int dstIndex, int dstTroops, byte dstTeam){
        ConquerTower dst;
        synchronized(ConquerTower.getConquerTowersLock()){
            dst = ConquerTower.getConquerTowers().get(dstIndex);
        }
        dst.setTroops(dstTroops);
        dst.setTeam(dstTeam);
    }

    private void spawnTowers(){
        if(player2){
            new ConquerTower(Utils.widthPercentage(5 - Utils.getScreenWidthPercentage(Images.conquer_tower_red.getWidth() / 2)), Utils.heightPercentage(50 - Utils.getScreenHeightPercentage(Images.conquer_tower_red.getHeight() * 3 / 4)), camera, Consts.CONQUER_TOWER_SIZE_BIG, Consts.CONQUER_TEAM_ENEMY, Consts.SIDE_LEFT);
            new ConquerTower(Utils.widthPercentage(25 - Utils.getScreenWidthPercentage(Images.conquer_tower_red_small.getWidth() / 2)), Utils.heightPercentage(50 - Utils.getScreenHeightPercentage(Images.conquer_tower_red_small.getHeight() * 3 / 4)), camera, Consts.CONQUER_TOWER_SIZE_SMALL, Consts.CONQUER_TEAM_NEUTRAL, Consts.SIDE_LEFT);
            new ConquerTower(Utils.widthPercentage(95 - Utils.getScreenWidthPercentage(Images.conquer_tower_red.getWidth() / 2)), Utils.heightPercentage(50 - Utils.getScreenHeightPercentage(Images.conquer_tower_red.getHeight() * 3 / 4)), camera, Consts.CONQUER_TOWER_SIZE_BIG, Consts.CONQUER_TEAM_ALLY, Consts.SIDE_RIGHT);
            new ConquerTower(Utils.widthPercentage(75 - Utils.getScreenWidthPercentage(Images.conquer_tower_red_small.getWidth() / 2)), Utils.heightPercentage(50 - Utils.getScreenHeightPercentage(Images.conquer_tower_red_small.getHeight() * 3 / 4)), camera, Consts.CONQUER_TOWER_SIZE_SMALL, Consts.CONQUER_TEAM_NEUTRAL, Consts.SIDE_RIGHT);
            new ConquerTower(Utils.widthPercentage(65 - Utils.getScreenWidthPercentage(Images.conquer_tower_red_small.getWidth() / 2)), Utils.heightPercentage(15 - Utils.getScreenHeightPercentage(Images.conquer_tower_red_small.getHeight() * 3 / 4)), camera, Consts.CONQUER_TOWER_SIZE_SMALL, Consts.CONQUER_TEAM_NEUTRAL, Consts.SIDE_RIGHT);
            new ConquerTower(Utils.widthPercentage(35 - Utils.getScreenWidthPercentage(Images.conquer_tower_red_small.getWidth() / 2)), Utils.heightPercentage(85 - Utils.getScreenHeightPercentage(Images.conquer_tower_red_small.getHeight() * 3 / 4)), camera, Consts.CONQUER_TOWER_SIZE_SMALL, Consts.CONQUER_TEAM_NEUTRAL, Consts.SIDE_LEFT);
            new ConquerTower(Utils.widthPercentage(85 - Utils.getScreenWidthPercentage(Images.conquer_tower_red_small.getWidth() / 2)), Utils.heightPercentage(25 - Utils.getScreenHeightPercentage(Images.conquer_tower_red_small.getHeight() * 3 / 4)), camera, Consts.CONQUER_TOWER_SIZE_SMALL, Consts.CONQUER_TEAM_ALLY, Consts.SIDE_RIGHT);
            new ConquerTower(Utils.widthPercentage(15 - Utils.getScreenWidthPercentage(Images.conquer_tower_red_small.getWidth() / 2)), Utils.heightPercentage(75 - Utils.getScreenHeightPercentage(Images.conquer_tower_red_small.getHeight() * 3 / 4)), camera, Consts.CONQUER_TOWER_SIZE_SMALL, Consts.CONQUER_TEAM_ENEMY, Consts.SIDE_LEFT);
            new ConquerTower(Utils.widthPercentage(85 - Utils.getScreenWidthPercentage(Images.conquer_tower_red_small.getWidth() / 2)), Utils.heightPercentage(75 - Utils.getScreenHeightPercentage(Images.conquer_tower_red_small.getHeight() * 3 / 4)), camera, Consts.CONQUER_TOWER_SIZE_SMALL, Consts.CONQUER_TEAM_ALLY, Consts.SIDE_RIGHT);
            new ConquerTower(Utils.widthPercentage(15 - Utils.getScreenWidthPercentage(Images.conquer_tower_red_small.getWidth() / 2)), Utils.heightPercentage(25 - Utils.getScreenHeightPercentage(Images.conquer_tower_red_small.getHeight() * 3 / 4)), camera, Consts.CONQUER_TOWER_SIZE_SMALL, Consts.CONQUER_TEAM_ENEMY, Consts.SIDE_LEFT);
            new ConquerTower(Utils.widthPercentage(40 - Utils.getScreenWidthPercentage(Images.conquer_tower_red.getWidth() / 2)), Utils.heightPercentage(40 - Utils.getScreenHeightPercentage(Images.conquer_tower_red.getHeight() * 3 / 4)), camera, Consts.CONQUER_TOWER_SIZE_BIG, Consts.CONQUER_TEAM_NEUTRAL, Consts.SIDE_LEFT);
            new ConquerTower(Utils.widthPercentage(60 - Utils.getScreenWidthPercentage(Images.conquer_tower_red.getWidth() / 2)), Utils.heightPercentage(60 - Utils.getScreenHeightPercentage(Images.conquer_tower_red.getHeight() * 3 / 4)), camera, Consts.CONQUER_TOWER_SIZE_BIG, Consts.CONQUER_TEAM_NEUTRAL, Consts.SIDE_RIGHT);
        }else{
            new ConquerTower(Utils.widthPercentage(5 - Utils.getScreenWidthPercentage(Images.conquer_tower_red.getWidth() / 2)), Utils.heightPercentage(50 - Utils.getScreenHeightPercentage(Images.conquer_tower_red.getHeight() * 3 / 4)), camera, Consts.CONQUER_TOWER_SIZE_BIG, Consts.CONQUER_TEAM_ALLY, Consts.SIDE_RIGHT);
            new ConquerTower(Utils.widthPercentage(25 - Utils.getScreenWidthPercentage(Images.conquer_tower_red_small.getWidth() / 2)), Utils.heightPercentage(50 - Utils.getScreenHeightPercentage(Images.conquer_tower_red_small.getHeight() * 3 / 4)), camera, Consts.CONQUER_TOWER_SIZE_SMALL, Consts.CONQUER_TEAM_NEUTRAL, Consts.SIDE_RIGHT);
            new ConquerTower(Utils.widthPercentage(95 - Utils.getScreenWidthPercentage(Images.conquer_tower_red.getWidth() / 2)), Utils.heightPercentage(50 - Utils.getScreenHeightPercentage(Images.conquer_tower_red.getHeight() * 3 / 4)), camera, Consts.CONQUER_TOWER_SIZE_BIG, Consts.CONQUER_TEAM_ENEMY, Consts.SIDE_LEFT);
            new ConquerTower(Utils.widthPercentage(75 - Utils.getScreenWidthPercentage(Images.conquer_tower_red_small.getWidth() / 2)), Utils.heightPercentage(50 - Utils.getScreenHeightPercentage(Images.conquer_tower_red_small.getHeight() * 3 / 4)), camera, Consts.CONQUER_TOWER_SIZE_SMALL, Consts.CONQUER_TEAM_NEUTRAL, Consts.SIDE_LEFT);
            new ConquerTower(Utils.widthPercentage(65 - Utils.getScreenWidthPercentage(Images.conquer_tower_red_small.getWidth() / 2)), Utils.heightPercentage(15 - Utils.getScreenHeightPercentage(Images.conquer_tower_red_small.getHeight() * 3 / 4)), camera, Consts.CONQUER_TOWER_SIZE_SMALL, Consts.CONQUER_TEAM_NEUTRAL, Consts.SIDE_LEFT);
            new ConquerTower(Utils.widthPercentage(35 - Utils.getScreenWidthPercentage(Images.conquer_tower_red_small.getWidth() / 2)), Utils.heightPercentage(85 - Utils.getScreenHeightPercentage(Images.conquer_tower_red_small.getHeight() * 3 / 4)), camera, Consts.CONQUER_TOWER_SIZE_SMALL, Consts.CONQUER_TEAM_NEUTRAL, Consts.SIDE_RIGHT);
            new ConquerTower(Utils.widthPercentage(85 - Utils.getScreenWidthPercentage(Images.conquer_tower_red_small.getWidth() / 2)), Utils.heightPercentage(25 - Utils.getScreenHeightPercentage(Images.conquer_tower_red_small.getHeight() * 3 / 4)), camera, Consts.CONQUER_TOWER_SIZE_SMALL, Consts.CONQUER_TEAM_ENEMY, Consts.SIDE_LEFT);
            new ConquerTower(Utils.widthPercentage(15 - Utils.getScreenWidthPercentage(Images.conquer_tower_red_small.getWidth() / 2)), Utils.heightPercentage(75 - Utils.getScreenHeightPercentage(Images.conquer_tower_red_small.getHeight() * 3 / 4)), camera, Consts.CONQUER_TOWER_SIZE_SMALL, Consts.CONQUER_TEAM_ALLY, Consts.SIDE_RIGHT);
            new ConquerTower(Utils.widthPercentage(85 - Utils.getScreenWidthPercentage(Images.conquer_tower_red_small.getWidth() / 2)), Utils.heightPercentage(75 - Utils.getScreenHeightPercentage(Images.conquer_tower_red_small.getHeight() * 3 / 4)), camera, Consts.CONQUER_TOWER_SIZE_SMALL, Consts.CONQUER_TEAM_ENEMY, Consts.SIDE_LEFT);
            new ConquerTower(Utils.widthPercentage(15 - Utils.getScreenWidthPercentage(Images.conquer_tower_red_small.getWidth() / 2)), Utils.heightPercentage(25 - Utils.getScreenHeightPercentage(Images.conquer_tower_red_small.getHeight() * 3 / 4)), camera, Consts.CONQUER_TOWER_SIZE_SMALL, Consts.CONQUER_TEAM_ALLY, Consts.SIDE_RIGHT);
            new ConquerTower(Utils.widthPercentage(40 - Utils.getScreenWidthPercentage(Images.conquer_tower_red.getWidth() / 2)), Utils.heightPercentage(40 - Utils.getScreenHeightPercentage(Images.conquer_tower_red.getHeight() * 3 / 4)), camera, Consts.CONQUER_TOWER_SIZE_BIG, Consts.CONQUER_TEAM_NEUTRAL, Consts.SIDE_RIGHT);
            new ConquerTower(Utils.widthPercentage(60 - Utils.getScreenWidthPercentage(Images.conquer_tower_red.getWidth() / 2)), Utils.heightPercentage(60 - Utils.getScreenHeightPercentage(Images.conquer_tower_red.getHeight() * 3 / 4)), camera, Consts.CONQUER_TOWER_SIZE_BIG, Consts.CONQUER_TEAM_NEUTRAL, Consts.SIDE_LEFT);
        }
    }

    private void createPlayers(){
//        synchronized(playersLock){
//            for(int i = 0; i < Networking.getUsernames().length; i++){
//                if(Networking.getUsernames()[i].equalsIgnoreCase(Networking.getUsername())) continue;
//                players.add(new Player(Networking.getUsernames()[i]
//                        , Consts.HIDE_AND_SEEK_START_POSITION_X * Consts.HIDE_AND_SEEK_TILE_WIDTH + (float) (Consts.HIDE_AND_SEEK_TILE_WIDTH - Images.player.getWidth()) / 2
//                        , Consts.HIDE_AND_SEEK_START_POSITION_Y * Consts.HIDE_AND_SEEK_TILE_HEIGHT + (float) (Consts.HIDE_AND_SEEK_TILE_HEIGHT - Images.player.getHeight()) / 2
//                        , Images.player, camera, null));
//            }
//        }
    }

    @Override
    public void trophyReached(){

    }

    public static void startMatch(){
        setLoading(false);
        conquerGameState.startTimer(Consts.CONQUER_TIME);
    }

    @Override
    public void tick() {
        if(transitionFading){
            fadePaint.setAlpha(fadePaint.getAlpha() - 5);
            if(fadePaint.getAlpha() == 0) transitionFading = false;
        }
        synchronized(Entity.getEntitiesLock()){
            for(Entity x : Entity.getEntities()){
                x.tick();
            }
        }
    }

    @Override
    public void render(Canvas canvas) {
        canvas.drawBitmap(Images.conquer_background_grass, null, Game.getScreenRect(), null);
        synchronized(renderLock){
//            synchronized(ConquerTroop.getConquerTroops()){
//                for(ConquerTroop x : ConquerTroop.getConquerTroops()){
//                    x.render(canvas);
//                }
//            }
//            synchronized(ConquerTower.getConquerTowersLock()){
//                for(ConquerTower x : ConquerTower.getConquerTowers()){
//                    x.render(canvas);
//                }
//            }
            synchronized(Entity.getEntitiesLock()){
                for(Entity x : Entity.getEntities()){
                    x.render(canvas);
                }
            }
            synchronized(ConquerTouchHandler.getConquerTouchHandlersLock()){
                for(ConquerTouchHandler x : ConquerTouchHandler.getConquerTouchHandlers()){
                    x.render(canvas);
                }
            }
            if(player2){
                fontPaint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(enemy, 16 * Game.density(), 32 * Game.density(), fontPaint);
                fontPaint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(Networking.getUsername(), Game.getScreenWidth() - 16 * Game.density(), 32 * Game.density(), fontPaint);
            }else{
                fontPaint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(Networking.getUsername(), 16 * Game.density(), 32 * Game.density(), fontPaint);
                fontPaint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(enemy, Game.getScreenWidth() - 16 * Game.density(), 32 * Game.density(), fontPaint);
            }
            showTime(canvas);
            if(transitionFading){
                canvas.drawRect(Game.getScreenRect(), fadePaint);
            }
        }
    }

    @Override
    public boolean onActionDown(MotionEvent event) {
        if(!Button.onActionDown(event, buttons, buttonsLock)){
            int mx = (int) event.getX();
            int my = (int) event.getY();
            synchronized(ConquerTower.getConquerTowersLock()){
                for(ConquerTower x : ConquerTower.getConquerTowers()){
                    if(x.getTeam() == Consts.CONQUER_TEAM_ALLY){
                        if(x.getCollisionBox().contains(mx, my)){
                            new ConquerTouchHandler(x, event.getPointerId(0));
                            break;
                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    public boolean onActionPointerDown(MotionEvent event) {
        if(!Button.onActionPointerDown(event, buttons, buttonsLock)){
            int mx = (int) event.getX(event.getActionIndex());
            int my = (int) event.getY(event.getActionIndex());
            synchronized(ConquerTower.getConquerTowersLock()){
                for(ConquerTower x : ConquerTower.getConquerTowers()){
                    if(x.getTeam() == Consts.CONQUER_TEAM_ALLY){
                        if(x.getCollisionBox().contains(mx, my)){
                            new ConquerTouchHandler(x, event.getPointerId(event.getActionIndex()));
                            break;
                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    public boolean onActionMove(MotionEvent event) {
        for(int i = 0; i < event.getPointerCount(); i++){
            ConquerTouchHandler cth = ConquerTouchHandler.getConquerTouchHandler(event.getPointerId(i));
            if(cth != null){
                int mx = (int) event.getX(i);
                int my = (int) event.getY(i);
                cth.setX2(mx);
                cth.setY2(my);
                synchronized(ConquerTower.getConquerTowersLock()){
                    for(ConquerTower x : ConquerTower.getConquerTowers()){
                        if(x == cth.getSrc()) continue;
                        if(x.getCollisionBox().contains(mx, my)){
                            cth.setX2((int) x.getVisualXCenter());
                            cth.setY2((int) x.getVisualYCenter());
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean onActionUp(MotionEvent event) {
        if(!Button.onActionUp(event, buttons, buttonsLock)){
            ConquerTouchHandler cth = ConquerTouchHandler.getConquerTouchHandler(event.getPointerId(0));
            if(cth != null){
                int mx = (int) event.getX();
                int my = (int) event.getY();
                synchronized(ConquerTower.getConquerTowersLock()){
                    for(ConquerTower x : ConquerTower.getConquerTowers()){
                        if(x == cth.getSrc()) continue;
                        if(x.getCollisionBox().contains(mx, my)){
                            cth.setDst(x);
                            cth.perform();
                            break;
                        }
                    }
                }
            }
            ConquerTouchHandler.remove(event.getPointerId(0));
        }

        return true;
    }

    @Override
    public boolean onActionPointerUp(MotionEvent event) {
        if(!Button.onActionPointerUp(event, buttons, buttonsLock)){
            ConquerTouchHandler cth = ConquerTouchHandler.getConquerTouchHandler(event.getPointerId(event.getActionIndex()));
            if(cth != null){
                int mx = (int) event.getX(event.getActionIndex());
                int my = (int) event.getY(event.getActionIndex());
                synchronized(ConquerTower.getConquerTowersLock()){
                    for(ConquerTower x : ConquerTower.getConquerTowers()){
                        if(x == cth.getSrc()) continue;
                        if(x.getCollisionBox().contains(mx, my)){
                            cth.setDst(x);
                            cth.perform();
                            break;
                        }
                    }
                }
            }
            ConquerTouchHandler.remove(event.getPointerId(event.getActionIndex()));
        }

        return true;
    }

    @Override
    public void endState() {
        ConquerTouchHandler.getConquerTouchHandlers().clear();
        buttons.clear();
        players.clear();
        players = null;
        playersLock = null;
        conquerGameState = null;
        camera = null;
        armyRatio = 0.5f;
        enemy = "n";
        player2 = false;
    }

    public static ArrayList<Player> getPlayers() {
        return players;
    }

    public static Object getPlayersLock() {
        return playersLock;
    }

//    public static Player getPlayer(String username) {
//        if(username.equalsIgnoreCase(Networking.getUsername())) return player;
//        synchronized(playersLock){
//            for(Player x : players){
//                if(x.getUsername().equalsIgnoreCase(username)) return x;
//            }
//        }
//
//        return null;
//    }

    public static float getArmyRatio() {
        return armyRatio;
    }

    public static void setArmyRatio(float armyRatio) {
        ConquerGameState.armyRatio = armyRatio;
    }

    public static Camera getCamera() {
        return camera;
    }
}