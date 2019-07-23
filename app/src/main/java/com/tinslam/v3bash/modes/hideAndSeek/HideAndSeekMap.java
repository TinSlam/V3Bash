package com.tinslam.comic.modes.hideAndSeek;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.tinslam.comic.UI.graphics.Images;
import com.tinslam.comic.base.Game;
import com.tinslam.comic.gameElements.Camera;
import com.tinslam.comic.gameElements.Map;
import com.tinslam.comic.utils.Consts;
import com.tinslam.comic.utils.Utils;

import java.util.ArrayList;

class HideAndSeekMap extends Map{
    private boolean[][] foggedTiles;
    private Paint fogPaint = new Paint();
    private ArrayList<int[]> foggedTilesArrayList = new ArrayList<>();
    private ArrayList<int[]> checkFoggedTilesArrayList = new ArrayList<>();
    private final Object foggedTilesLock = new Object();
    private final Object checkFoggedTilesLock = new Object();

    HideAndSeekMap(Camera camera){
        super(camera);

        fogPaint.setAlpha(160);
    }

    public boolean isFogged(int x, int y){
        try{
            return foggedTiles[x][y];
        }catch(Exception ignored){}

        return true;
    }

    public boolean isSolid(Rect rect){
        int x1 = rect.left;
        int x2 = rect.right;
        int y1 = rect.top;
        int y2 = rect.bottom;

        for(int i = x1 / Consts.HIDE_AND_SEEK_TILE_WIDTH; i <= x2 / Consts.HIDE_AND_SEEK_TILE_WIDTH; i++){
            for(int j = y1 / Consts.HIDE_AND_SEEK_TILE_HEIGHT; j <= y2 / Consts.HIDE_AND_SEEK_TILE_HEIGHT; j++){
                if(tiles[i][j] == Consts.HIDE_AND_SEEK_WALL) return true;
            }
        }

        return false;
    }

    @Override
    public boolean isSolidCircle(float cx, float cy, float radius){
        for(int i = (int) (cx - radius) / Consts.HIDE_AND_SEEK_TILE_WIDTH; i <= (int) (cx + radius) / Consts.HIDE_AND_SEEK_TILE_WIDTH; i++){
            for(int j = (int) (cy - radius) / Consts.HIDE_AND_SEEK_TILE_HEIGHT; j <= (int) (cy + radius) / Consts.HIDE_AND_SEEK_TILE_HEIGHT; j++){
                try {
                    if(tiles[i][j] == Consts.HIDE_AND_SEEK_WALL){
                        if(Utils.rectCollidesCircle(new Rect(i * Consts.HIDE_AND_SEEK_TILE_WIDTH, j * Consts.HIDE_AND_SEEK_TILE_HEIGHT, i * Consts.HIDE_AND_SEEK_TILE_WIDTH + Consts.HIDE_AND_SEEK_TILE_WIDTH, j * Consts.HIDE_AND_SEEK_TILE_HEIGHT + Consts.HIDE_AND_SEEK_TILE_HEIGHT),
                                cx, cy, radius)) return true;
                    }
                }catch(Exception e){
                    return true;
                }
            }
        }

        return false;
    }

    void updateFog(float x, float y, float radius){
        x = (int) x / Consts.HIDE_AND_SEEK_TILE_WIDTH;
        y = (int) y / Consts.HIDE_AND_SEEK_TILE_HEIGHT;

        // Clear previous fogs

        synchronized(foggedTilesLock){
            for(int[] i : foggedTilesArrayList){
                try{
                    foggedTiles[i[0]][i[1]] = true;
                }catch(Exception ignored){}
            }
            foggedTilesArrayList.clear();
        }

        // Unobstructed fog

//        for(int i = (int) (x - radius); i <= Math.ceil(x + radius); i++){
//            for(int j = (int) (y - radius); j <= Math.ceil(y + radius); j++){
//                if(Math.abs(x - i) + Math.abs(y - j) > radius) continue;
//                try{
//                    foggedTiles[i][j] = false;
//                    synchronized(foggedTilesLock){
//                        foggedTilesArrayList.add(new int[] {i, j});
//                    }
//                }catch(Exception ignored){}
//            }
//        }
        spreadFog((int) x, (int) y, (int) x, (int) y, (int) radius);
        synchronized(checkFoggedTilesLock){
            for(int[] i : checkFoggedTilesArrayList){
                try{
                    foggedTiles[i[0]][i[1]] = true;
                }catch(Exception ignored){}
            }
            checkFoggedTilesArrayList.clear();
        }
    }

    private void spreadFog(int x0, int y0, int x, int y, int radius){
        if(radius == -1) return;
        try{
            if (tiles[x][y] == Consts.HIDE_AND_SEEK_WALL) {
                synchronized (checkFoggedTilesLock) {
                    if(x > x0 && y > y0){
                        int i = Math.abs(x - x0);
                        int j = Math.abs(y - y0);
                        if(i > j){
                            checkFoggedTilesArrayList.add(new int[]{x + 1, y});
//                            checkFoggedTilesArrayList.add(new int[]{x, y + 1});
                        }else if(i < j){
//                            checkFoggedTilesArrayList.add(new int[]{x + 1, y});
                            checkFoggedTilesArrayList.add(new int[]{x, y + 1});
                        }else{
                            checkFoggedTilesArrayList.add(new int[]{x + 1, y});
                            checkFoggedTilesArrayList.add(new int[]{x, y + 1});
                        }
                    }else if(x < x0 && y < y0){
                        int i = Math.abs(x - x0);
                        int j = Math.abs(y - y0);
                        if(i > j){
                            checkFoggedTilesArrayList.add(new int[]{x - 1, y});
//                            checkFoggedTilesArrayList.add(new int[]{x, y - 1});
                        }else if(i < j){
//                            checkFoggedTilesArrayList.add(new int[]{x - 1, y});
                            checkFoggedTilesArrayList.add(new int[]{x, y - 1});
                        }else{
                            checkFoggedTilesArrayList.add(new int[]{x - 1, y});
                            checkFoggedTilesArrayList.add(new int[]{x, y - 1});
                        }
                    }else if(x > x0 && y < y0){
                        int i = Math.abs(x - x0);
                        int j = Math.abs(y - y0);
                        if(i > j){
                            checkFoggedTilesArrayList.add(new int[]{x + 1, y});
//                            checkFoggedTilesArrayList.add(new int[]{x, y - 1});
                        }else if(i < j){
//                            checkFoggedTilesArrayList.add(new int[]{x + 1, y});
                            checkFoggedTilesArrayList.add(new int[]{x, y - 1});
                        }else{
                            checkFoggedTilesArrayList.add(new int[]{x + 1, y});
                            checkFoggedTilesArrayList.add(new int[]{x, y - 1});
                        }
                        checkFoggedTilesArrayList.add(new int[]{x + 1, y});
                        checkFoggedTilesArrayList.add(new int[]{x, y - 1});
                    }else if(x < x0 && y > y0){
                        int i = Math.abs(x - x0);
                        int j = Math.abs(y - y0);
                        if(i > j){
                            checkFoggedTilesArrayList.add(new int[]{x - 1, y});
//                            checkFoggedTilesArrayList.add(new int[]{x, y + 1});
                        }else if(i < j){
//                            checkFoggedTilesArrayList.add(new int[]{x - 1, y});
                            checkFoggedTilesArrayList.add(new int[]{x, y + 1});
                        }else{
                            checkFoggedTilesArrayList.add(new int[]{x - 1, y});
                            checkFoggedTilesArrayList.add(new int[]{x, y + 1});
                        }
                    }else if(x == x0 && y > y0){
                        checkFoggedTilesArrayList.add(new int[]{x, y + 1});
                        checkFoggedTilesArrayList.add(new int[]{x - 1, y + 1});
                        checkFoggedTilesArrayList.add(new int[]{x + 1, y + 1});
                    }else if(x == x0 && y < y0){
                        checkFoggedTilesArrayList.add(new int[]{x - 1, y - 1});
                        checkFoggedTilesArrayList.add(new int[]{x, y - 1});
                        checkFoggedTilesArrayList.add(new int[]{x  +1, y - 1});
                    }else if(y == y0 && x < x0){
                        checkFoggedTilesArrayList.add(new int[]{x - 1, y - 1});
                        checkFoggedTilesArrayList.add(new int[]{x - 1, y});
                        checkFoggedTilesArrayList.add(new int[]{x - 1, y + 1});
                    }else if(y == y0 && x > x0){
                        checkFoggedTilesArrayList.add(new int[]{x + 1, y - 1});
                        checkFoggedTilesArrayList.add(new int[]{x + 1, y});
                        checkFoggedTilesArrayList.add(new int[]{x + 1, y + 1});
                    }
                }
                return;
            }

            synchronized(checkFoggedTilesLock){
                for(int[] i : checkFoggedTilesArrayList){
                    if(i[0] == x && i[1] == y){
                        return;
//                        if(x != x0 && y != y0){
//                            if((x == x0 + 1 && y == y0 + 1) ||
//                                    (x == x0 - 1 && y == y0 + 1) ||
//                                    (x == x0 - 1 && y == y0 - 1) ||
//                                    (x == x0 + 1 && y == y0 - 1)) continue;
//                            return;
//                        }
                    }
                }
            }
            foggedTiles[x][y] = false;
            synchronized(foggedTilesLock){
                foggedTilesArrayList.add(new int[] {x, y});
            }

            spreadFog(x0, y0, x - 1, y, radius - 1);
            spreadFog(x0, y0, x + 1, y, radius - 1);
            spreadFog(x0, y0, x, y - 1, radius - 1);
            spreadFog(x0, y0, x, y + 1, radius - 1);
        }catch(Exception e){
            return;
        }
    }

    @Override
    public void setTiles(byte[][] tiles){
        this.tiles = tiles;
        this.foggedTiles = new boolean[Consts.HIDE_AND_SEEK_TILE_WIDTH][Consts.HIDE_AND_SEEK_TILE_HEIGHT];
        initTiles();
    }

    @Override
    public void initTiles(){
        for(int i = 0; i < Consts.HIDE_AND_SEEK_TILE_WIDTH; i++){
            for(int j = 0; j < Consts.HIDE_AND_SEEK_TILE_HEIGHT; j++){
                foggedTiles[i][j] = true;
            }
        }
    }

    @Override
    public void render(Canvas canvas){
        drawTiles(canvas);
    }

    private void drawTiles(Canvas canvas){
        for(int i = (int) (-camera.getX() / Consts.HIDE_AND_SEEK_TILE_WIDTH); i < Math.ceil(-camera.getX() + Game.getScreenWidth()) / Consts.HIDE_AND_SEEK_TILE_WIDTH; i++){
            for(int j = (int) (-camera.getY() / Consts.HIDE_AND_SEEK_TILE_HEIGHT); j < Math.ceil(-camera.getY() + Game.getScreenHeight()) / Consts.HIDE_AND_SEEK_TILE_HEIGHT; j++){
                try {
                    switch(tiles[i][j]){
                        case Consts.HIDE_AND_SEEK_SPACE :
                            drawBitmap(Images.maze_floor_tile, i * Consts.HIDE_AND_SEEK_TILE_WIDTH, j * Consts.HIDE_AND_SEEK_TILE_HEIGHT, 0, 1, canvas);
                            break;

                        case Consts.HIDE_AND_SEEK_WALL :
                            drawBitmap(Images.maze_wall_tile, i * Consts.HIDE_AND_SEEK_TILE_WIDTH, j * Consts.HIDE_AND_SEEK_TILE_HEIGHT, 0, 1, canvas);
                            break;

//                        case Consts.MAZE_GOBLET :
//                            drawBitmap(Images.maze_floor_tile, i * Consts.HIDE_AND_SEEK_TILE_WIDTH, j * Consts.HIDE_AND_SEEK_TILE_HEIGHT, 0, 1, canvas);
//                            break;
                    }
                    if(foggedTiles[i][j]){
                        drawRect(i * Consts.HIDE_AND_SEEK_TILE_WIDTH, j * Consts.HIDE_AND_SEEK_TILE_HEIGHT, (i + 1) * Consts.HIDE_AND_SEEK_TILE_WIDTH, (j + 1) * Consts.HIDE_AND_SEEK_TILE_HEIGHT, fogPaint, canvas);
                    }
                }catch(Exception ignored){}
            }
        }
    }
}