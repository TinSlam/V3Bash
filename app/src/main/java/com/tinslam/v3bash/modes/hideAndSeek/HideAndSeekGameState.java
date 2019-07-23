package com.tinslam.comic.modes.hideAndSeek;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.tinslam.comic.UI.buttons.Button;
import com.tinslam.comic.UI.graphics.Animation;
import com.tinslam.comic.UI.graphics.Images;
import com.tinslam.comic.base.Game;
import com.tinslam.comic.gameElements.Camera;
import com.tinslam.comic.gameElements.MovePad;
import com.tinslam.comic.gameElements.entity.Entity;
import com.tinslam.comic.gameElements.entity.movingEntity.Player;
import com.tinslam.comic.networking.Networking;
import com.tinslam.comic.networking.UdpNetworking;
import com.tinslam.comic.states.GameState;
import com.tinslam.comic.states.MainMenuState;
import com.tinslam.comic.utils.Consts;
import com.tinslam.comic.utils.FileManager;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class HideAndSeekGameState extends GameState{
    private static Camera camera;
    private static HideAndSeekMap map;
    private static Player player;
    private static MovePad movePad;
    private static ArrayList<Player> players;
    private static Object playersLock;
    private static Paint fadePaint = new Paint();
    private boolean transitionFading = true;
    private static boolean transitionSeeker = false;
    private static boolean matchStarted = false;
    private static HideAndSeekGameState hideAndSeekGameState;
    private int networkSendPositionsInterval = 0;
    private int collisionCD = 0;
    private static Player seeker;
    private static Animation transitionSeekerAnimation;

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

    public static void onUdpConnection(){ // Method is being invoked.

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

    public static void assignSeeker(final String username){
        synchronized(playersLock){
            if(players.isEmpty()){
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        assignSeeker(username);
                    }
                }, 1000);
            }
        }
        HideAndSeekGameState.hideAndSeekGameState.startTimer(Consts.HIDE_AND_SEEK_TIME / 1000);
        if(Networking.getUsername().equalsIgnoreCase(username)){
            seeker = player;
            player.setImage(Images.player_possessed);
        }else{
            synchronized(playersLock){
                for(Player x : players){
                    if(x.getUsername().equalsIgnoreCase(username)){
                        seeker = x;
                        seeker.setImage(Images.player_possessed);
                        startMatch();
                        return;
                    }
                }
            }
        }
    }

    public static void seekerReleased(){
        if(seeker == player){
            startMatch();
        }

        transitionSeeker = true;
        transitionSeekerAnimation = new Animation(Images.hide_and_seek_seeker_transition_animation, 50, 20, 2) {
            @Override
            public void onEnd() {
                transitionSeeker = false;
            }

            @Override
            public void onCycleEnd() {

            }
        };
    }

    @Override
    public void startState() {
        hideAndSeekGameState = this;
        setLoading(true);
        setMapRightEnd(Consts.HIDE_AND_SEEK_TILE_WIDTH * Consts.HIDE_AND_SEEK_WIDTH);
        setMapBottomEnd(Consts.HIDE_AND_SEEK_TILE_HEIGHT * Consts.HIDE_AND_SEEK_HEIGHT);
        ignoresLostConnection = true;
        camera = new Camera();
        map = new HideAndSeekMap(camera);
        byte[][] tiles = FileManager.loadFile("maps/2nd.tls", Consts.HIDE_AND_SEEK_WIDTH, Consts.HIDE_AND_SEEK_HEIGHT);
        if(tiles == null){
            Networking.logout();
            Game.setState(new MainMenuState());
        }
        map.setTiles(tiles);
        player = new Player(""
                , Consts.HIDE_AND_SEEK_START_POSITION_X * Consts.HIDE_AND_SEEK_TILE_WIDTH + (float) (Consts.HIDE_AND_SEEK_TILE_WIDTH - Images.player.getWidth()) / 2
                , Consts.HIDE_AND_SEEK_START_POSITION_Y * Consts.HIDE_AND_SEEK_TILE_HEIGHT + (float) (Consts.HIDE_AND_SEEK_TILE_HEIGHT - Images.player.getHeight()) / 2
                , Images.player, camera, map);
        camera.setFollowObject(player);
        players = new ArrayList();
        playersLock = new Object();
        movePad = new MovePad(player);
        fadePaint.setColor(Color.BLACK);
        fadePaint.setAlpha(255);
        UdpNetworking.startUdpConnection(HideAndSeekGameState.class);
        createPlayers();
    }

    private void createPlayers(){
        synchronized(playersLock){
            for(int i = 0; i < Networking.getUsernames().length; i++){
                if(Networking.getUsernames()[i].equalsIgnoreCase(Networking.getUsername())) continue;
                players.add(new Player(Networking.getUsernames()[i]
                        , Consts.HIDE_AND_SEEK_START_POSITION_X * Consts.HIDE_AND_SEEK_TILE_WIDTH + (float) (Consts.HIDE_AND_SEEK_TILE_WIDTH - Images.player.getWidth()) / 2
                        , Consts.HIDE_AND_SEEK_START_POSITION_Y * Consts.HIDE_AND_SEEK_TILE_HEIGHT + (float) (Consts.HIDE_AND_SEEK_TILE_HEIGHT - Images.player.getHeight()) / 2
                        , Images.player, camera, map));
            }
        }
    }

    public static void playerKilled(String username){
        if(username.equalsIgnoreCase(Networking.getUsername())){
            player.setDead(true);
            camera.setFollowObject(seeker);
        }else{
            getPlayer(username).setDead(true);
//            if(player.isDead()){
//                synchronized(playersLock){
//                    for(Player x : players){
//                        if(!x.isDead()){
//                            camera.setFollowObject(x);
//                        }
//                    }
//                }
//            }else{
//                synchronized(playersLock){
//                    camera.setFollowObject(players.get(0) != null ? players.get(0) : camera.getFollowObject());
//                }
//            }
        }
    }

    @Override
    public void trophyReached(){

    }

    public static void startMatch(){
        matchStarted = true;
        setLoading(false);
    }

    public static void setPositions(String username, int x, int y){
        Player p = getPlayer(username);
        if(p == null) return;
        p.setX2(x * Game.density());
        p.setY2(y * Game.density());
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
        map.updateFog(camera.getFollowObject().getX() + camera.getFollowObject().getImage().getWidth() / 2, camera.getFollowObject().getY() + camera.getFollowObject().getImage().getHeight() / 2, Consts.HIDE_AND_SEEK_FOG_RADIUS);
        if(player.isDead()) return;
        if(seeker == player) checkCollisionWithPlayers();
        networkSendPositionsInterval++;
        if(networkSendPositionsInterval >= Consts.NETWORK_SEND_POSITIONS_INTERVAL){
            networkSendPositionsInterval = 0;
            UdpNetworking.sendPlayerPositions();
        }
    }

    private void checkCollisionWithPlayers(){
        if(collisionCD > 0){
            collisionCD--;
            return;
        }
        synchronized(playersLock){
            for(Player x : players){
                if(x.isDead()) continue;
                if(x.getCollisionBox().intersect(player.getCollisionBox())){
                    collisionCD = Consts.NETWORK_COLLISION_CD;
                    Networking.playerCaught(x.getUsername());
                }
            }
        }
    }

    @Override
    public void render(Canvas canvas) {
        map.render(canvas);
        synchronized(renderLock){
            synchronized(Entity.getEntitiesLock()){
                for(Entity x : Entity.getEntities()){
                    if(map.isFogged(((int) x.getX() + x.getImage().getWidth() / 2) / Consts.HIDE_AND_SEEK_TILE_WIDTH, ((int) x.getY() + x.getImage().getHeight() / 2) / Consts.HIDE_AND_SEEK_TILE_HEIGHT)) continue;
                    if(x == camera.getFollowObject()) continue;
                    if(camera.isOnScreen(x.getCollisionBox())){
                        if(!x.isDead()){
                            x.render(canvas);
                        }
                    }
                }
            }
            if(!camera.getFollowObject().isDead()) camera.getFollowObject().render(canvas);
            movePad.render(canvas);
            showTime(canvas);
            if(transitionFading){
                canvas.drawRect(Game.getScreenRect(), fadePaint);
            }
        }
        if(transitionSeeker){
            transitionSeekerAnimation.render(Game.getScreenWidth() - transitionSeekerAnimation.getWidth(), Game.getScreenHeight() / 2 - transitionSeekerAnimation.getHeight() / 2, canvas);
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
        map = null;
        player = null;
        movePad = null;
        players.clear();
        players = null;
        playersLock = null;
        fadePaint = new Paint();
        transitionSeeker = false;
        matchStarted = false;
        hideAndSeekGameState = null;
        seeker = null;
        transitionSeekerAnimation = null;
    }

    public static ArrayList<Player> getPlayers() {
        return players;
    }

    public static Object getPlayersLock() {
        return playersLock;
    }

    public static Player getPlayer(String username) {
        if(username.equalsIgnoreCase(Networking.getUsername())) return player;
        synchronized(playersLock){
            for(Player x : players){
                if(x.getUsername().equalsIgnoreCase(username)) return x;
            }
        }

        return null;
    }
}