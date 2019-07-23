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
import com.tinslam.comic.networking.Networking;
import com.tinslam.comic.networking.UdpNetworking;
import com.tinslam.comic.states.GameState;
import com.tinslam.comic.utils.Consts;

import java.util.ArrayList;

public class MazeGameState extends GameState{
    private static Camera camera;
    private static MazeMap mazeMap;
    private static Player player;
    private static MovePad movePad;
    private static ArrayList<Player> players;
    private static Object playersLock;
    private static Paint fadePaint = new Paint();
    private static int[] points;
    private static boolean matchStarted = false;
    private static MazeGameState mazeGameState;
    private boolean transitionFading = true;
    private int networkSendPositionsInterval = 0;

    public MazeGameState(){
        matchStarted = false;
    }

    @Override
    public void disconnected() {
        Game.lostConnection();
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

    public static void onUdpConnection(){ // Method is being invoked.
        Networking.sendMazeReceived();
    }

    @Override
    public void handleKeyEvent(KeyEvent event) {

    }

    @Override
    public void startState() {
        MazeGameState.mazeGameState = this;
        setLoading(true);
        setMapRightEnd(Consts.MAZE_TILE_WIDTH * Consts.MAZE_WIDTH);
        setMapBottomEnd(Consts.MAZE_TILE_HEIGHT * Consts.MAZE_HEIGHT);
        ignoresLostConnection = true;
        camera = new Camera();
        mazeMap = new MazeMap(camera);
        player = new Player(Networking.getUsername()
                , Consts.MAZE_START_POSITION_X * Consts.MAZE_TILE_WIDTH + (float) (Consts.MAZE_TILE_WIDTH - Images.player.getWidth()) / 2
                , Consts.MAZE_START_POSITION_Y * Consts.MAZE_TILE_HEIGHT + (float) (Consts.MAZE_TILE_HEIGHT - Images.player.getHeight()) / 2
                , Images.player, camera, mazeMap);
        camera.setFollowObject(player);
        players = new ArrayList();
        playersLock = new Object();
        movePad = new MovePad(player);
        fadePaint.setColor(Color.BLACK);
        fadePaint.setAlpha(255);
        points = new int[Networking.getUsernames().length];
        for(int i = 0; i < points.length; i++){
            points[i] = 0;
        }
        createPlayers();
    }

    public void createPlayers(){
        synchronized(playersLock){
            for(int i = 0; i < Networking.getUsernames().length; i++){
                if(Networking.getUsernames()[i].equalsIgnoreCase(Networking.getUsername())) continue;
                players.add(new Player(Networking.getUsernames()[i]
                        , Consts.MAZE_START_POSITION_X * Consts.MAZE_TILE_WIDTH + (float) (Consts.MAZE_TILE_WIDTH - Images.player.getWidth()) / 2
                        , Consts.MAZE_START_POSITION_Y * Consts.MAZE_TILE_HEIGHT + (float) (Consts.MAZE_TILE_HEIGHT - Images.player.getHeight()) / 2
                        , Images.player, camera, mazeMap));
            }
        }
    }

    public static void setPositions(String username, int x, int y){
        Player p = getPlayer(username);
        if(p == null) return;
        p.setX2(x * Game.density());
        p.setY2(y * Game.density());
    }

    @Override
    public void trophyReached(){
        Networking.trophyReached();
    }

    public static void trophyReached(String username, int x, int y){
        points[Networking.getPlayerIndex(username)]++;
        mazeMap.setNewTrophy(x, y);
    }

    public static void receiveMaze(int[] array){
        mazeMap.setTiles(MazeGenerator.convertTo2D(array));
        UdpNetworking.startUdpConnection(MazeGameState.class);
    }

    public static int[] requestMaze(){
        UdpNetworking.startUdpConnection(MazeGameState.class);
        MazeGenerator maze = MazeGenerator.generateMaze(Consts.MAZE_WIDTH, Consts.MAZE_HEIGHT);
        mazeMap.setTiles(maze.getData());
        return MazeGenerator.create1DArray(maze);
    }

    public static void startMatch(){
        matchStarted = true;
        MazeGameState.mazeGameState.startTimer(Consts.MAZE_TIME / 1000);
        setLoading(false);
    }

    @Override
    public void tick() {
        if(!matchStarted) return;
        if(transitionFading){
            fadePaint.setAlpha(fadePaint.getAlpha() - 5);
            if(fadePaint.getAlpha() == 0) transitionFading = false;
        }
        synchronized(Entity.getEntitiesLock()){
            for(Entity x : Entity.getEntities()){
                x.tick();
            }
        }
        synchronized(playersLock){
            for(Player x : players){
                x.move(Consts.NETWORK_POSITION_TIME_TO_REACH_DESTINATION);
            }
        }
        camera.tick();
        movePad.move();
        networkSendPositionsInterval++;
        if(networkSendPositionsInterval >= Consts.NETWORK_SEND_POSITIONS_INTERVAL){
            networkSendPositionsInterval = 0;
            UdpNetworking.sendPlayerPositions();
        }
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
            showTime(canvas);
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
        camera = null;
        mazeMap = null;
        player = null;
        movePad = null;
        players.clear();
        players = null;
        playersLock = null;
        points = null;
        matchStarted = false;
        mazeGameState = null;
    }

    public static ArrayList<Player> getPlayers() {
        return players;
    }

    public static Object getPlayersLock() {
        return playersLock;
    }

    public static Player getPlayer() {
        return player;
    }

    public static Player getPlayer(String username) {
        synchronized(playersLock){
            for(Player x : players){
                if(x.getUsername().equalsIgnoreCase(username)) return x;
            }
        }

        return null;
    }
}