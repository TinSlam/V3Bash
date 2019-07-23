package com.tinslam.comic.modes.maze;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

import com.tinslam.comic.UI.graphics.Images;
import com.tinslam.comic.base.Game;
import com.tinslam.comic.gameElements.Camera;
import com.tinslam.comic.gameElements.Map;
import com.tinslam.comic.gameElements.entity.Entity;
import com.tinslam.comic.gameElements.entity.staticEntity.Trophy;
import com.tinslam.comic.utils.Consts;
import com.tinslam.comic.utils.Utils;

public class MazeMap extends Map{

    public MazeMap(Camera camera){
        super(camera);
    }

    public void setNewTrophy(){
        int x = (int) (Math.random() * Consts.MAZE_WIDTH), y = (int) (Math.random() * Consts.MAZE_HEIGHT);
        while(tiles[x][y] != Consts.MAZE_SPACE){
            x = (int) (Math.random() * Consts.MAZE_WIDTH);
            y = (int) (Math.random() * Consts.MAZE_HEIGHT);
        }
        setNewTrophy(x, y);
    }

    public void setNewTrophy(int x, int y){
        for(int i = 0; i < Consts.MAZE_WIDTH; i++){
            for(int j = 0; j < Consts.MAZE_HEIGHT; j++){
                if(tiles[i][j] == Consts.MAZE_GOBLET){
                    tiles[i][j] = Consts.MAZE_SPACE;
                    tiles[x][y] = Consts.MAZE_GOBLET;
                    synchronized(Entity.getEntitiesLock()){
                        Entity.getEntities().remove(Trophy.getTrophy());
                        new Trophy((float) x * Consts.MAZE_TILE_WIDTH + (float) (Consts.MAZE_TILE_WIDTH - Images.trophy.getWidth()) / 2, (float) y * Consts.MAZE_TILE_HEIGHT + (float) (Consts.MAZE_TILE_HEIGHT - Images.trophy.getHeight()) / 2, Images.trophy, camera);
                    }
                    return;
                }
            }
        }
    }

    @Override
    public boolean isSolid(Rect rect){
        int x1 = rect.left;
        int x2 = rect.right;
        int y1 = rect.top;
        int y2 = rect.bottom;

        for(int i = x1 / Consts.MAZE_TILE_WIDTH; i <= x2 / Consts.MAZE_TILE_WIDTH; i++){
            for(int j = y1 / Consts.MAZE_TILE_HEIGHT; j <= y2 / Consts.MAZE_TILE_HEIGHT; j++){
                if(tiles[i][j] == Consts.MAZE_WALL) return true;
            }
        }

        return false;
    }

    @Override
    public boolean isSolidCircle(float cx, float cy, float radius){
        for(int i = (int) (cx - radius) / Consts.MAZE_TILE_WIDTH; i <= (int) (cx + radius) / Consts.MAZE_TILE_WIDTH; i++){
            for(int j = (int) (cy - radius) / Consts.MAZE_TILE_HEIGHT; j <= (int) (cy + radius) / Consts.MAZE_TILE_HEIGHT; j++){
                try {
                    if(tiles[i][j] == Consts.MAZE_WALL){
                        if(Utils.rectCollidesCircle(new Rect(i * Consts.MAZE_TILE_WIDTH, j * Consts.MAZE_TILE_HEIGHT, i * Consts.MAZE_TILE_WIDTH + Consts.MAZE_TILE_WIDTH, j * Consts.MAZE_TILE_HEIGHT + Consts.MAZE_TILE_HEIGHT),
                                cx, cy, radius)) return true;
                    }
                }catch(Exception e){
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void setTiles(byte[][] tiles){
        this.tiles = tiles;
        initTiles();
    }

    @Override
    public void initTiles(){
        for(int i = 0; i < Consts.MAZE_WIDTH; i++){
            for(int j = 0; j < Consts.MAZE_HEIGHT; j++){
                switch(tiles[i][j]){
                    case Consts.MAZE_GOBLET :
                        new Trophy((float) i * Consts.MAZE_TILE_WIDTH + (float) (Consts.MAZE_TILE_WIDTH - Images.trophy.getWidth()) / 2, (float) j * Consts.MAZE_TILE_HEIGHT + (float) (Consts.MAZE_TILE_HEIGHT - Images.trophy.getHeight()) / 2, Images.trophy, camera);
                        break;
                }
            }
        }
    }

    @Override
    public void render(Canvas canvas){
        drawMaze(canvas);
    }

    private void drawMaze(Canvas canvas){
        for(int i = (int) (-camera.getX() / Consts.MAZE_TILE_WIDTH); i < Math.ceil(-camera.getX() + Game.getScreenWidth()) / Consts.MAZE_TILE_WIDTH; i++){
            for(int j = (int) (-camera.getY() / Consts.MAZE_TILE_HEIGHT); j < Math.ceil(-camera.getY() + Game.getScreenHeight()) / Consts.MAZE_TILE_HEIGHT; j++){
                try {
                    switch(tiles[i][j]){
                        case Consts.MAZE_SPACE :
                            drawBitmap(Images.maze_floor_tile, i * Consts.MAZE_TILE_WIDTH, j * Consts.MAZE_TILE_HEIGHT, 0, 1, canvas);
                            break;

                        case Consts.MAZE_WALL :
                            drawBitmap(Images.maze_wall_tile, i * Consts.MAZE_TILE_WIDTH, j * Consts.MAZE_TILE_HEIGHT, 0, 1, canvas);
                            break;

                        case Consts.MAZE_GOBLET :
                            drawBitmap(Images.maze_floor_tile, i * Consts.MAZE_TILE_WIDTH, j * Consts.MAZE_TILE_HEIGHT, 0, 1, canvas);
                            break;
                    }
                }catch(Exception ignored){}
            }
        }
    }
}
