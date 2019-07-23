package com.tinslam.comic.utils;

import com.tinslam.comic.R;
import com.tinslam.comic.base.Game;

public class Consts {
    public static final long TIME_TO_DRAW = 45000,
//    TIME_TO_VOTE = 5000,
//    TIME_TO_SEE_RESULTS = 5000,
//    TIME_TO_DRAW = 5000,
    TIME_TO_VOTE = 10000,
    TIME_TO_SEE_RESULTS = 10000,
    TIME_TO_HIDE_LOG = 3000;

    public static final byte SIDE_RIGHT = 0,
    SIDE_LEFT = 1;

    public static final int CONQUER_TOWER_TROOPS_BIG = 20,
    CONQUER_TOWER_TROOPS_SMALL = 10,
    CONQUER_TIME = 60;

    public static final byte CONQUER_TOWER_SIZE_SMALL = 0,
    CONQUER_TOWER_SIZE_BIG = 1;

    public static final byte CONQUER_TEAM_NEUTRAL = 0,
    CONQUER_TEAM_ALLY = 1,
    CONQUER_TEAM_ENEMY = 2;

    public static final int HIDE_AND_SEEK_START_POSITION_X = 1,
    HIDE_AND_SEEK_START_POSITION_Y = 1,
    HIDE_AND_SEEK_TILE_WIDTH = (int) (64 * Game.density()),
    HIDE_AND_SEEK_TILE_HEIGHT = (int) (64 * Game.density()),
    HIDE_AND_SEEK_FOG_RADIUS = 4,
    HIDE_AND_SEEK_WIDTH = 64,
    HIDE_AND_SEEK_HEIGHT = 64,
    HIDE_AND_SEEK_TIME = 2 * 60 * 1000;
//
//    public static final byte[][] HIDE_AND_SEEK_MAP = {
//    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
//    {0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0},
//    {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
//    {0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 1, 1, 0},
//    {0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
//    {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0},
//    {1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 0, 0, 0},
//    {0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
//    {0, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 1},
//    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
//    {0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 0},
//    {0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 1, 1, 0},
//    {0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 0}
//    };

    public static final byte HIDE_AND_SEEK_SPACE = 0,
    HIDE_AND_SEEK_WALL = 1;

    public static final int MAZE_START_POSITION_X = 0,
    MAZE_START_POSITION_Y = 0,
    MAZE_WIDTH = 65,
    MAZE_HEIGHT = 65,
    MAZE_GOBLET_XOFFSET = 0,
    MAZE_GOBLET_YOFFSET = 0,
    MAZE_TILE_WIDTH = (int) (64 * Game.density()),
    MAZE_TILE_HEIGHT = (int) (64 * Game.density()),
    MAZE_TIME = 2 * 60 * 1000;

    public static final byte MAZE_SPACE = 0,
    MAZE_WALL = 1,
    MAZE_GOBLET = 2;

    public static final byte MODE_PAINTING = 0,
    MODE_MAZE = 1,
    MODE_HIDE_AND_SEEK = 2,
    MODE_CONQUER = 3;

    public static final int MAX_CHARS_USERNAME = 25,
    MAX_CHARS_PASSWORD = 255,
    MAX_CHARS_EMAIL = 255,
    MIN_CHARS_PASSWORD = 7,
    MIN_CHARS_USERNAME = 3;

    public static final byte STATE_NULL = 0,
    STATE_DRAW = 1,
    STATE_VOTE = 2,
    STATE_RESULTS = 3,
    STATE_RANKINGS = 4;

    public static final byte LAYOUT_PALETTE_ONE_ROW = 0,
    LAYOUT_PALETTE_TWO_ROW = 1;

    public static final byte COLOR_RED = 0, // IF MORE THAN 2 DIGITS YOU GOTTA CHANGE THE PICTURE SENDING ALGORITHM !
    COLOR_GREEN = 1,
    COLOR_BLUE = 2,
    COLOR_YELLOW = 3,
    COLOR_BLACK = 4,
    COLOR_WHITE = 5,
    COLOR_NO_COLOR = 6, // LOCKED COLORS !
    COLOR_NUMBER = 7; // THIS NEEDS TO BE THE NUMBER OF COLORS !

    public static final byte BRUSH_STYLE_RECT = 0, // IF MORE THAN 1 DIGITS YOU GOTTA CHANGE THE PICTURE SENDING ALGORITHM !
//    BRUSH_STYLE_OVAL = 2,
    BRUSH_FILL = 1;

    public static final long CAMERA_MOVE_TIME = 1000;

    public static final int NETWORK_SEND_POSITIONS_INTERVAL = 3,
    NETWORK_POSITION_TIME_TO_REACH_DESTINATION = 3,
    NETWORK_COLLISION_CD = 30;

    public static final byte UDP_PLAYER_POSITIONS = 0,
    UDP_SELF_POSITIONS = 1,
    UDP_CONNECTED = 2;

//    public static  String SERVER_ADDRESS = "151.232.106.189"; //151.232.106.189  172.16.54.6 On Static ip 192.168.1.33;
//    public static  String SERVER_ADDRESS = "192.168.1.15";
//    public static  String SERVER_ADDRESS = "172.23.183.139"; //Madrese
    public static  String SERVER_ADDRESS = "185.81.99.32"; //VPS
//    public static  String SERVER_ADDRESS = "172.23.145.159"; //Madrese Lan
//    public static String SERVER_ADDRESS = "172.24.66.87"; //Khabga
//    public static  String SERVER_ADDRESS = "127.0.0.1"; //Localhost
//    public static String SERVER_ADDRESS = "192.168.43.54"; //Hotspot
//    public static  String SERVER_ADDRESS = "192.168.137.1"; //Hotspot
//    public static  String SERVER_ADDRESS = "5.114.89.202"; //Hotspot
//    public static  String SERVER_ADDRESS = "46.4.141.162"; // VPS
    public static final int SERVER_PORT = 31028;
}