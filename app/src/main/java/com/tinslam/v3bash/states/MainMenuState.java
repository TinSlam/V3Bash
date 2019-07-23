package com.tinslam.comic.states;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.tinslam.comic.R;
import com.tinslam.comic.UI.graphics.Images;
import com.tinslam.comic.UI.buttons.Button;
import com.tinslam.comic.UI.buttons.rectanglebuttons.RectangleButton;
import com.tinslam.comic.activities.ActivityManager;
import com.tinslam.comic.activities.LoginActivity;
import com.tinslam.comic.activities.MainActivity;
import com.tinslam.comic.modes.maze.MazeGameState;
import com.tinslam.comic.modes.maze.MazePracticeState;
import com.tinslam.comic.modes.painting.PaintingPracticeState;
import com.tinslam.comic.networking.Networking;
import com.tinslam.comic.base.Game;
import com.tinslam.comic.utils.Utils;

public class MainMenuState extends GameState{
    @Override
    public void disconnected() {
        
    }

    @Override
    public void connected() {

    }

    @Override
    public void trophyReached() {

    }

    @Override
    public void surfaceDestroyed() {

    }

    @Override
    public void handleBackPressed() {

    }

    @Override
    public void handleKeyEvent(KeyEvent event) {

    }

    @Override
    public void startState() {
        ignoresLostConnection = true;
        backGroundPaint.setARGB(255, 150, 0, 255);

//        int imageWidth = (int) ((float) Images.button_empty.getWidth() / Game.getScreenWidth() * 100);
//        int imageWidth = (int) Utils.widthPercentage(15);
        int imageWidth = 15;
        Bitmap image = Images.resizeImage(Images.button_empty, imageWidth * Game.getScreenWidth() / 100);
        Bitmap imageHover = Images.resizeImage(Images.button_empty_hover, imageWidth * Game.getScreenWidth() / 100);

        RectangleButton paintingPracticeButton = new RectangleButton((int) (Game.getScreenWidth() * (float) (90 - imageWidth) / 100), (int) (Game.getScreenHeight() * (float) 75 / 100),
                image, imageHover, Game.Context().getString(R.string.practice_painting), false) {
            @Override
            public boolean onDown() {
                return true;
            }

            @Override
            public boolean onUp() {
                Game.setState(new PaintingPracticeState());
                return true;
            }
        };
//        paintingPracticeButton.resizeImage((int) ((128 + 64) * Game.density()), (int) ((32 + 8) * Game.density()));
//        paintingPracticeButton.reposition((int) (Game.getScreenWidth() * (float) (90 - Utils.getScreenWidthPercentage(paintingPracticeButton.getImage().getWidth())) / 100), (int) (Game.getScreenHeight() * (float) 75 / 100));

        if(Game.getLanguage().equalsIgnoreCase("fa")){
            new RectangleButton(0, 0,
                    Images.flag_english, Images.flag_english, false) {
                @Override
                public boolean onDown() {
                    return true;
                }

                @Override
                public boolean onUp() {
                    Game.changeLanguage("en");
                    return true;
                }
            };

        }else{
            new RectangleButton(0, 0,
                    Images.flag_persian, Images.flag_persian, false) {
                @Override
                public boolean onDown() {
                    return true;
                }

                @Override
                public boolean onUp() {
                    Game.changeLanguage("fa");
                    return true;
                }
            };
        }

        RectangleButton mazePracticeButton = new RectangleButton((int) (Game.getScreenWidth() * (float) (60 - imageWidth) / 100), (int) (Game.getScreenHeight() * (float) 35 / 100),
                image, imageHover, Game.Context().getString(R.string.practice_maze), false) {
            @Override
            public boolean onDown() { return true; }

            @Override
            public boolean onUp() {
                Game.setState(new MazePracticeState());
                return true;
            }
        };
//        mazePracticeButton.resizeImage((int) ((128 + 64) * Game.density()), (int) ((32 + 8) * Game.density()));
//        mazePracticeButton.reposition((int) (Game.getScreenWidth() * (float) (60 - Utils.getScreenWidthPercentage(mazePracticeButton.getImage().getWidth())) / 100), (int) (Game.getScreenHeight() * (float) 35 / 100));

        if(Networking.getUsername().equals("")) {
            RectangleButton loginButton = new RectangleButton((int) (Game.getScreenWidth() * (float) (90 - imageWidth) / 100), (int) (Game.getScreenHeight() * (float) 55 / 100),
                    image, imageHover, Game.Context().getString(R.string.login), false) {
                @Override
                public boolean onDown() {
                    return true;
                }

                @Override
                public boolean onUp() {
                    ActivityManager.changeActivity(MainActivity.mainActivity(), LoginActivity.class);
                    return true;
                }
            };
//            loginButton.resizeImage((int) ((128 + 64) * Game.density()), (int) ((32 + 8) * Game.density()));
//            loginButton.reposition((int) (Game.getScreenWidth() * (float) (90 - Utils.getScreenWidthPercentage(loginButton.getImage().getWidth())) / 100), (int) (Game.getScreenHeight() * (float) 55 / 100));
        }else{
            RectangleButton playButton = new RectangleButton((int) (Game.getScreenWidth() * (float) (90 - imageWidth) / 100), (int) (Game.getScreenHeight() * (float) 35 / 100),
                    image, imageHover, Game.Context().getString(R.string.play), false) {
                @Override
                public boolean onDown() { return true; }

                @Override
                public boolean onUp() {
                    Game.setState(new QueueState());
                    return true;
                }
            };
//            playButton.resizeImage((int) ((128 + 64) * Game.density()), (int) ((32 + 8) * Game.density()));
//            playButton.reposition((int) (Game.getScreenWidth() * (float) (90 - Utils.getScreenWidthPercentage(playButton.getImage().getWidth())) / 100), (int) (Game.getScreenHeight() * (float) 35 / 100));

            RectangleButton logout = new RectangleButton((int) (Game.getScreenWidth() * (float) (90 - imageWidth) / 100), (int) (Game.getScreenHeight() * (float) 80 / 100),
                    image, imageHover, Game.Context().getString(R.string.logout), false) {
                @Override
                public boolean onDown() { return true; }

                @Override
                public boolean onUp() {
                    Networking.logout();
                    return true;
                }
            };
//            logout.resizeImage((int) ((128 + 64) * Game.density()), (int) ((32 + 8) * Game.density()));
//            logout.reposition((int) (Game.getScreenWidth() * (float) (90 - Utils.getScreenWidthPercentage(logout.getImage().getWidth())) / 100), (int) (Game.getScreenHeight() * (float) 75 / 100));
//            paintingPracticeButton.reposition((int) (Game.getScreenWidth() * (float) (90 - Utils.getScreenWidthPercentage(logout.getImage().getWidth())) / 100), (int) (Game.getScreenHeight() * (float) 55 / 100));
        }
    }

    @Override
    public void tick() {

    }

    @Override
    public void render(Canvas canvas) {
        canvas.drawBitmap(Images.background_main_menu, null, Game.getScreenRect(), null);
        Button.renderButtons(canvas, buttons, buttonsLock);
    }

    @Override
    public boolean onActionDown(MotionEvent event) {
        return Button.onActionDown(event, buttons, buttonsLock);
    }

    @Override
    public boolean onActionPointerDown(MotionEvent event) {
        return Button.onActionPointerDown(event, buttons, buttonsLock);
    }

    @Override
    public boolean onActionMove(MotionEvent event) {
        return Button.onActionMove(event, buttons, buttonsLock);
    }

    @Override
    public boolean onActionUp(MotionEvent event) {
        return Button.onActionUp(event, buttons, buttonsLock);
    }

    @Override
    public boolean onActionPointerUp(MotionEvent event) {
        return Button.onActionPointerUp(event, buttons, buttonsLock);
    }

    @Override
    public void endState() {
        buttons.clear();
    }
}
