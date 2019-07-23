package com.tinslam.comic.gameElements.entity.staticEntity;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.tinslam.comic.base.Game;
import com.tinslam.comic.gameElements.Camera;
import com.tinslam.comic.gameElements.entity.movingEntity.Player;
import com.tinslam.comic.modes.maze.MazeGameState;
import com.tinslam.comic.states.GameState;
import com.tinslam.comic.utils.Utils;

import java.util.ArrayList;

public class Trophy extends StaticEntity{
    private boolean reached = false;
    private static Trophy trophy;

    public Trophy(float x, float y, Bitmap image, Camera camera) {
        super(x, y, image, camera);

        Trophy.trophy = this;
    }

    @Override
    public void destroyStaticEntity(){

    }

    @Override
    public void tickStaticEntity() {
        checkCollisionWithPlayer();
    }

//    private void checkCollisionWithPlayers(){
//        ArrayList<Player> players = MazeGameState.getPlayers();
//        synchronized(MazeGameState.getPlayersLock()){
//            for(Player x : players){
//                if(Utils.rectCollidesCircle(collisionBox, x.getCircle())){
//                    reached = true;
//                    MazeGameState.trophyReached(x);
//                    return;
//                }
//            }
//        }
//    }

    private void checkCollisionWithPlayer(){
        if(Utils.rectCollidesCircle(collisionBox, Player.getPlayer().getCircle())){
            ((GameState) Game.getState()).trophyReached();
        }
    }

    @Override
    public void renderStaticEntity(Canvas canvas) {
        canvas.drawBitmap(image, transformationMatrix, null);
    }

    public static Trophy getTrophy() {
        return trophy;
    }
}