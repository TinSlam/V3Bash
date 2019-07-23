package com.tinslam.comic.utils;

import com.tinslam.comic.base.Game;

import java.util.Scanner;

public class FileManager {
    public static byte[][] loadFile(String path, int width, int height) {
        byte[][] tiles = null;
        try {
            Scanner sc = new Scanner(Game.Context().getAssets().open(path));
            if(Integer.parseInt(sc.next()) != width || height != Integer.parseInt(sc.next())) return null;
            tiles = new byte[width][height];

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    tiles[i][j] = Byte.parseByte(sc.next());
                }
            }

            sc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tiles;
    }
}

