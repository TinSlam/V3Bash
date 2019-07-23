package com.tinslam.comic.modes.maze;

import com.tinslam.comic.utils.Consts;

public class MazeGenerator {
    private byte[][] data;
    private int width;
    private int height;
    private java.util.Random rand = new java.util.Random();

    public MazeGenerator(int width, int height) {
        this.width = width;
        this.height = height;
        data = new byte[width][];
    }

    public static MazeGenerator generateMaze(int width, int height){
        MazeGenerator maze = new MazeGenerator(width, height);
        maze.generate();
        maze.addGoblet();
        maze.addInterances();
        maze.print();
        return maze;
    }

    private void addInterances(){
        while(true){
            int i = (int) (Math.random() * (width - 4)) + 2;
            if(data[i][2] == Consts.MAZE_SPACE){
                data[i][1] = Consts.MAZE_SPACE;
                break;
            }
        }
        while(true){
            int i = (int) (Math.random() * (height - 4)) + 2;
            if(data[2][i] == Consts.MAZE_SPACE){
                data[1][i] = Consts.MAZE_SPACE;
                break;
            }
        }
        while(true){
            int i = (int) (Math.random() * (height - 4)) + 2;
            if(data[height - 3][i] == Consts.MAZE_SPACE){
                data[height - 2][i] = Consts.MAZE_SPACE;
                break;
            }
        }
        while(true){
            int i = (int) (Math.random() * (width - 4)) + 2;
            if(data[i][width - 3] == Consts.MAZE_SPACE){
                data[i][width - 2] = Consts.MAZE_SPACE;
                break;
            }
        }
    }

    private void carve(int x, int y) {

        final int[] upx = { 1, -1, 0, 0 };
        final int[] upy = { 0, 0, 1, -1 };

        int dir = rand.nextInt(4);
        int count = 0;
        while(count < 4) {
            final int x1 = x + upx[dir];
            final int y1 = y + upy[dir];
            final int x2 = x1 + upx[dir];
            final int y2 = y1 + upy[dir];
            if(data[x1][y1] == Consts.MAZE_WALL && data[x2][y2] == Consts.MAZE_WALL) {
                data[x1][y1] = Consts.MAZE_SPACE;
                data[x2][y2] = Consts.MAZE_SPACE;
                carve(x2, y2);
            } else {
                dir = (dir + 1) % 4;
                count += 1;
            }
        }
    }

    public void generate() {
        for(int x = 0; x < width; x++) {
            data[x] = new byte[height];
            for(int y = 0; y < height; y++) {
                data[x][y] = Consts.MAZE_WALL;
            }
        }
        for(int x = 0; x < width; x++) {
            data[x][0] = Consts.MAZE_SPACE;
            data[x][height - 1] = Consts.MAZE_SPACE;
        }
        for(int y = 0; y < height; y++) {
            data[0][y] = Consts.MAZE_SPACE;
            data[width - 1][y] = Consts.MAZE_SPACE;
        }

        data[2][2] = Consts.MAZE_SPACE;
        carve(2, 2);

//            data[2][1] = Consts.MAZE_SPACE;
//            data[width - 3][height - 2] = Consts.MAZE_SPACE;
    }

    public void print() {
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                if(data[x][y] == Consts.MAZE_WALL) {
                    System.out.print("[]");
                } else if(data[x][y] == Consts.MAZE_SPACE){
                    System.out.print("  ");
                }else{
                    System.out.print("G ");
                }
            }
            System.out.println();
        }
    }

    public void addGoblet() {
        boolean found = false;
        while (!found) {
            int x = (int) (Math.random() * (width - Consts.MAZE_GOBLET_XOFFSET)) + Consts.MAZE_GOBLET_XOFFSET;
            int y = (int) (Math.random() * (height - Consts.MAZE_GOBLET_YOFFSET)) + Consts.MAZE_GOBLET_YOFFSET;
            if (data[x][y] == Consts.MAZE_SPACE) {
                data[x][y] = Consts.MAZE_GOBLET;
                found = true;
            }
        }
    }

    public static int[] create1DArray(MazeGenerator maze){
        int[] array = new int[(int) Math.ceil((float) maze.width * maze.height / 10)];
        int counter = 0;
        int tens = 0;
        for(int i = 0; i < maze.width; i++){
            for(int j = 0; j < maze.height; j++){
                array[counter] += Math.pow(10, tens) * maze.data[i][j];
                tens++;
                if(tens == 10){
                    counter++;
                    tens = 0;
                }
            }
        }
        return array;
    }

    public static byte[][] convertTo2D(int[] array){
        byte[][] arr = new byte[Consts.MAZE_WIDTH][Consts.MAZE_HEIGHT];

        int counter = 0;
        int tens = 0;

        for(int i = 0; i < Consts.MAZE_WIDTH; i++){
            for(int j = 0; j < Consts.MAZE_HEIGHT; j++){
                arr[i][j] = (byte) ((array[counter] / Math.pow(10, tens)) % 10);
                tens++;
                if(tens == 10){
                    tens = 0;
                    counter++;
                }
            }
        }

        return arr;
    }

    public void printArray(){
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                switch(data[i][j]){
                    case Consts.MAZE_SPACE :
                        System.out.print("  ");
                        break;

                    case Consts.MAZE_WALL :
                        System.out.print("O ");
                        break;

                    case Consts.MAZE_GOBLET :
                        System.out.print("x ");
                        break;
                }
            }
            System.out.println();
        }
    }

    public byte[][] getData() {
        return data;
    }
}