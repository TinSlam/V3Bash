package com.tinslam.comic.UI.graphics;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.tinslam.comic.R;
import com.tinslam.comic.base.Game;
import com.tinslam.comic.utils.Consts;

public class Images{
    public static Bitmap button_empty, button_empty_hover, ranking_slot, ranking_slot2;
    public static Bitmap color_locked, color_black, color_white, color_red, color_green, color_blue, color_yellow;
    public static Bitmap palette_left_button, palette_right_button;
    public static Bitmap brush, palette, undo, redo, settings;
    public static Bitmap brush_fill, brush_style_rect, brush_style_oval;
    public static Bitmap background_main_menu, background_grass;
    public static Bitmap loadingAnimation, bug, bug_hit_animation, hide_and_seek_seeker_transition_animation;
    public static Bitmap player, trophy, player_possessed;
    public static Bitmap move_pad_small, move_pad_big;
    public static Bitmap maze_floor_tile, maze_wall_tile;
    public static Bitmap conquer_tower_red, conquer_tower_neutral, conquer_tower_blue, conquer_background_grass;
    public static Bitmap conquer_tower_red_small, conquer_tower_neutral_small, conquer_tower_blue_small;
    public static Bitmap conquer_troops, conquer_troops_enemy;
    public static Bitmap flag_english, flag_persian;

    public static void loadImages(){
        Resources res = Game.Context().getResources();
        bug = BitmapFactory.decodeResource(res, R.drawable.bug);
        bug = resizeImage(bug, bug.getWidth() / 2);
        flag_english = BitmapFactory.decodeResource(res, R.drawable.flag_english);
        flag_persian = BitmapFactory.decodeResource(res, R.drawable.flag_persian);
        flag_english = resizeImage(flag_english, 48 * Game.density(), 32 * Game.density());
        flag_persian = resizeImage(flag_persian, 48 * Game.density(), 32 * Game.density());
        button_empty = BitmapFactory.decodeResource(res, R.drawable.button_empty);
        button_empty_hover = BitmapFactory.decodeResource(res, R.drawable.button_empty_hover);
        button_empty = resizeImage(button_empty, (int) (256 * Game.density()), (int) (64 * Game.density()));
        button_empty_hover = resizeImage(button_empty_hover, (int) (256 * Game.density()), (int) (64 * Game.density()));
        Bitmap color_default = BitmapFactory.decodeResource(res, R.drawable.color_default);
        color_locked = BitmapFactory.decodeResource(res, R.drawable.color_locked);
        move_pad_big = BitmapFactory.decodeResource(res, R.drawable.move_pad_big);
        ranking_slot = BitmapFactory.decodeResource(res, R.drawable.ranking_slot3);
        ranking_slot2 = BitmapFactory.decodeResource(res, R.drawable.ranking_slot2);
        move_pad_small = BitmapFactory.decodeResource(res, R.drawable.move_pad_small);
        player = BitmapFactory.decodeResource(res, R.drawable.player);
        conquer_background_grass = BitmapFactory.decodeResource(res, R.drawable.conquer_background_grass);
        conquer_tower_red = BitmapFactory.decodeResource(res, R.drawable.conquer_tower_red);
        conquer_tower_red = resizeImage(conquer_tower_red, 48 * Game.density());
        conquer_tower_red_small = resizeImage(conquer_tower_red, 36 * Game.density());
        conquer_tower_blue = BitmapFactory.decodeResource(res, R.drawable.conquer_tower_blue);
        conquer_tower_blue = resizeImage(conquer_tower_blue, 48 * Game.density());
        conquer_tower_blue_small = resizeImage(conquer_tower_blue, 36 * Game.density());
        conquer_tower_neutral = BitmapFactory.decodeResource(res, R.drawable.conquer_tower_neutral);
        conquer_tower_neutral = resizeImage(conquer_tower_neutral, 48 * Game.density());
        conquer_tower_neutral_small = resizeImage(conquer_tower_neutral, 36 * Game.density());
        hide_and_seek_seeker_transition_animation = BitmapFactory.decodeResource(res, R.drawable.hide_and_seek_seeker_transition);
        resizeImage(hide_and_seek_seeker_transition_animation, 64 * Game.density(), 64 * Game.density());
        player_possessed = BitmapFactory.decodeResource(res, R.drawable.player_possessed);
        maze_floor_tile = BitmapFactory.decodeResource(res, R.drawable.maze_floor_tile);
        maze_wall_tile = BitmapFactory.decodeResource(res, R.drawable.maze_wall_tile);
        trophy = BitmapFactory.decodeResource(res, R.drawable.trophy);
        trophy = resizeImage(trophy, Consts.MAZE_TILE_WIDTH);
        maze_floor_tile = resizeImage(maze_floor_tile, Consts.MAZE_TILE_WIDTH, Consts.MAZE_TILE_HEIGHT);
        maze_wall_tile = resizeImage(maze_wall_tile, Consts.MAZE_TILE_WIDTH, Consts.MAZE_TILE_HEIGHT);
        conquer_troops = resizeImage(player, 12 * Game.density(), 12 * Game.density());
        conquer_troops_enemy = resizeImage(trophy, 12 * Game.density(), 12 * Game.density());
        player = resizeImage(player, 32 * Game.density(), 32 * Game.density());
        player_possessed = resizeImage(player_possessed, 32 * Game.density(), 32 * Game.density());
        color_default = resizeImage(color_default, (int) (64 * Game.density()), (int) (64 * Game.density()));
        color_locked = resizeImage(color_locked, (int) (64 * Game.density()), (int) (64 * Game.density()));
//        color_red = BitmapFactory.decodeResource(res, R.drawable.color_red);
//        color_green = BitmapFactory.decodeResource(res, R.drawable.color_green);
//        color_blue = BitmapFactory.decodeResource(res, R.drawable.color_blue);1
//        color_yellow = BitmapFactory.decodeResource(res, R.drawable.color_yellow);
        palette_left_button = BitmapFactory.decodeResource(res, R.drawable.palette_left_button);
        palette_right_button = BitmapFactory.decodeResource(res, R.drawable.palette_right_button);
        palette = BitmapFactory.decodeResource(res, R.drawable.palette);
        brush = BitmapFactory.decodeResource(res, R.drawable.brush);
        undo = BitmapFactory.decodeResource(res, R.drawable.undo);
        settings = BitmapFactory.decodeResource(res, R.drawable.settings_button);
        redo = BitmapFactory.decodeResource(res, R.drawable.redo);
        brush_fill = BitmapFactory.decodeResource(res, R.drawable.brush_fill);
        brush_style_oval = BitmapFactory.decodeResource(res, R.drawable.brush_style_oval);
        brush_style_rect = BitmapFactory.decodeResource(res, R.drawable.brush_style_rect);
        Bitmap background_default = BitmapFactory.decodeResource(res, R.drawable.background_default);
        background_main_menu = BitmapFactory.decodeResource(res, R.drawable.background_main_menu);
        background_grass = BitmapFactory.decodeResource(res, R.drawable.background_grass);
        loadingAnimation = BitmapFactory.decodeResource(res, R.drawable.loading_animation);
        bug_hit_animation = BitmapFactory.decodeResource(res, R.drawable.bug_hit_animation);
        bug_hit_animation = resizeImage(bug_hit_animation, bug_hit_animation.getWidth() / 2);
//        background_login = replaceBlackWithColor(background_default, Color.GREEN);
//        background_register = replaceBlackWithColor(background_default, Color.argb(255, 0, 200, 200));
//        background_login = Images.resizeImage(background_login, Game.getScreenWidth(), Game.getScreenHeight());
//        background_register = Images.resizeImage(background_register, Game.getScreenWidth(), Game.getScreenHeight());
        color_black = changeImageColor(color_default, Color.BLACK);
        color_white = changeImageColor(color_default, Color.WHITE);
        color_red = changeImageColor(color_default, Color.RED);
        color_green = changeImageColor(color_default, Color.GREEN);
        color_blue = changeImageColor(color_default, Color.BLUE);
        color_yellow = changeImageColor(color_default, Color.YELLOW);
    }

    public static Bitmap replaceBlackWithColor(Bitmap srcBmp, int dstColor){
        int width = srcBmp.getWidth();
        int height = srcBmp.getHeight();

        float srcHSV[] = new float[3];
        float dstHSV[] = new float[3];

        Bitmap dstBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if(((srcBmp.getPixel(col, row) & 0xff000000) >> 24) == 0){
                    dstBitmap.setPixel(col, row, dstColor);
                }else{
                    dstBitmap.setPixel(col, row, srcBmp.getPixel(col, row));
                }
            }
        }

        return dstBitmap;
    }

    public static Bitmap changeImageColor(Bitmap srcBmp, int dstColor) {
        int width = srcBmp.getWidth();
        int height = srcBmp.getHeight();

        float srcHSV[] = new float[3];
        float dstHSV[] = new float[3];

        Bitmap dstBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if(((srcBmp.getPixel(col, row) & 0xff000000) >> 24) == 0) continue;
                Color.colorToHSV(srcBmp.getPixel(col, row), srcHSV);
                Color.colorToHSV(dstColor, dstHSV);

                // If it area to be painted set only value of original image
                dstHSV[2] = srcHSV[2];  // value

                dstBitmap.setPixel(col, row, Color.HSVToColor(dstHSV));
            }
        }

        return dstBitmap;
    }

    public static Bitmap coverCircleWithColor(Bitmap bitmap, int color){
        Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
        Bitmap image = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(image);
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAlpha(127);
        canvas.drawCircle(image.getWidth() / 2, image.getHeight() / 2, image.getWidth() / 2, paint);
        return image;
    }

    public static Bitmap resizeImage(Bitmap bm, float newWidth, float newHeight) {
        return resizeImage(bm, (int) newWidth, (int) newHeight);
    }

    public static Bitmap resizeImage(Bitmap bm, int newWidth, int newHeight) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                bm, newWidth, newHeight, false);
        return resizedBitmap;
    }

    public static Bitmap resizeImage(Bitmap bm, float newWidth) {
        return resizeImage(bm, (int) newWidth);
    }

    public static Bitmap resizeImage(Bitmap bm, int newWidth) {
        int newHeight = (int) (bm.getHeight() * (float) newWidth / bm.getWidth());
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                bm, newWidth, newHeight, false);
        return resizedBitmap;
    }
}
