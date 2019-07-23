package com.tinslam.comic.modes.maze;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.tinslam.comic.UI.buttons.Button;
import com.tinslam.comic.UI.graphics.Images;
import com.tinslam.comic.base.Game;
import com.tinslam.comic.gameElements.Camera;
import com.tinslam.comic.gameElements.MovePad;
import com.tinslam.comic.gameElements.entity.Entity;
import com.tinslam.comic.gameElements.entity.movingEntity.Player;
import com.tinslam.comic.states.GameState;
import com.tinslam.comic.states.MainMenuState;
import com.tinslam.comic.utils.Consts;

import java.util.ArrayList;

public class MazePracticeState extends GameState{
    private static Camera camera;
    private static MazeMap mazeMap;
    private static Player player;
    private static MovePad movePad;
    private static ArrayList<Player> players;
    private static Object playersLock;
    private static Paint fadePaint = new Paint();
    private boolean transitionFading = true;

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
        Game.setState(new MainMenuState());
    }

    @Override
    public void handleKeyEvent(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_R){
            camera.addRotation(-10);
        }

        if(event.getKeyCode() == KeyEvent.KEYCODE_F){
            camera.addRotation(+10);
        }

        if(event.getKeyCode() == KeyEvent.KEYCODE_C){
            camera.jump(-(float) (Math.random() * mapRightEnd), -(float) Math.random() * mapBottomEnd);
        }
    }

    @Override
    public void startState() {
//        setLoading(true);
        setMapRightEnd(Consts.MAZE_TILE_WIDTH * Consts.MAZE_WIDTH);
        setMapBottomEnd(Consts.MAZE_TILE_HEIGHT * Consts.MAZE_HEIGHT);
        ignoresLostConnection = true;
        camera = new Camera();
        mazeMap = new MazeMap(camera);
        player = new Player(""
                , Consts.MAZE_START_POSITION_X * Consts.MAZE_TILE_WIDTH + (float) (Consts.MAZE_TILE_WIDTH - Images.player.getWidth()) / 2
                , Consts.MAZE_START_POSITION_Y * Consts.MAZE_TILE_HEIGHT + (float) (Consts.MAZE_TILE_HEIGHT - Images.player.getHeight()) / 2
                , Images.player, camera, mazeMap);
        camera.setFollowObject(player);
        players = new ArrayList();
        playersLock = new Object();
        movePad = new MovePad(player);
        fadePaint.setColor(Color.BLACK);
        fadePaint.setAlpha(255);
        requestMaze();
    }

    @Override
    public void trophyReached(){
        mazeMap.setNewTrophy();
    }

    public static int[] requestMaze(){
        MazeGenerator maze = MazeGenerator.generateMaze(Consts.MAZE_WIDTH, Consts.MAZE_HEIGHT);
        mazeMap.setTiles(maze.getData());
        return MazeGenerator.create1DArray(maze);
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
        camera.tick();
        movePad.move();
    }

    @Override
    public void render(Canvas canvas) {
        synchronized(renderLock){
            mazeMap.render(canvas);
            synchronized(Entity.getEntitiesLock()){
                for(Entity x : Entity.getEntities()){
                    if(x == camera.getFollowObject()) continue;
                    if(camera.isOnScreen(x.getCollisionBox())) x.render(canvas);
                }
            }
            camera.getFollowObject().render(canvas);
            movePad.render(canvas);
            if(transitionFading){
                canvas.drawRect(Game.getScreenRect(), fadePaint);
            }
        }
    }

    @Override
    public boolean onActionDown(MotionEvent event) {
        if(!Button.onActionDown(event, buttons, buttonsLock)){
            if(movePad.isTouched(event.getX(), event.getY())) movePad.actionDown(event);
        }

        return true;
    }

    @Override
    public boolean onActionPointerDown(MotionEvent event) {
        if(!Button.onActionPointerDown(event, buttons, buttonsLock)){
            if(movePad.isTouched(event.getX(event.getPointerId(event.getActionIndex())), event.getY(event.getPointerId(event.getActionIndex())))) movePad.actionPointerDown(event);
        }

        return true;
    }

    @Override
    public boolean onActionMove(MotionEvent event) {
        for(int i = 0; i < event.getPointerCount(); i++){
            if(movePad.getPointerId() == event.getPointerId(i)){
                movePad.actionMove(event, i);
            }
        }
        return true;
    }

    @Override
    public boolean onActionUp(MotionEvent event) {
        if(!Button.onActionUp(event, buttons, buttonsLock)){
            if(event.getPointerId(0) == movePad.getPointerId()) movePad.actionUp(event);
        }

        return true;
    }

    @Override
    public boolean onActionPointerUp(MotionEvent event) {
        if(!Button.onActionPointerUp(event, buttons, buttonsLock)){
            if(movePad.getPointerId() == event.getPointerId(event.getActionIndex())) movePad.actionPointerUp(event);
        }

        return true;
    }

    @Override
    public void endState() {
        buttons.clear();
    }

    public static ArrayList<Player> getPlayers() {
        return players;
    }

    public static Object getPlayersLock() {
        return playersLock;
    }
}