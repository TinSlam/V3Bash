package com.tinslam.comic.modes.painting;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.tinslam.comic.UI.buttons.Button;
import com.tinslam.comic.UI.buttons.Node;
import com.tinslam.comic.base.Game;
import com.tinslam.comic.networking.Networking;
import com.tinslam.comic.states.GameState;
import com.tinslam.comic.states.State;
import com.tinslam.comic.utils.Consts;
import com.tinslam.comic.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class PaintingGameState extends GameState {
    private static Paint rankingFontPaint = new Paint();
    private static PaintingPalette palette;
    private static PaintingBrushPanel brushPanel;
    private static PaintingToolsPanel toolsPanel;
    private static PaintingCanvas paintingCanvas;
    private static PaintingInfoPanel infoPanel;
    private static PaintingSettingsPanel paintingSettingsPanel;
    private static byte players;
    private static int[] points = new int[6];
    private static String[] subjects;
    private static String id = "";
    private static int currentSubject = 0;
    private static byte state = Consts.STATE_NULL;
    private static int timeCounter = 0;
    private static Timer timer1 = new Timer();
    private static Timer timer2 = new Timer();
    private static ArrayList<PaintingPainting> paintings = new ArrayList<>();
    private static ArrayList<PaintingPainting> allPaintings = new ArrayList<>();
    private static final Object paintingsLock = new Object();
    private static final Object allPaintingsLock = new Object();
    private static byte vote = 1;
    private static int votesReceived = 0;
    private int rankingStateTop = (int) ((Game.getScreenHeight() - 6 * 32 * Game.density()) / 2);;
    private int rankingStateLeft = (Game.getScreenWidth() / 10);
    private static ArrayList<Object[]> rankingList = new ArrayList<>();

    public PaintingGameState(byte players){
        PaintingGameState.players = players;
        timer1.cancel();
        timer2.cancel();
        vote = 1;
        votesReceived = 0;
        timeCounter = 0;
        currentSubject = 0;
        state = Consts.STATE_NULL;
    }

    @Override
    public void disconnected() {
        Game.lostConnection();
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
        if(!Networking.getSocket().connected()) Game.lostConnection();
        State.setLoading(true);
        for(int i = 0; i < 6; i++) points[i] = 0;
        paintingCanvas = new PaintingCanvas();
        brushPanel = new PaintingBrushPanel();
        palette = new PaintingPalette();
        toolsPanel = new PaintingToolsPanel();
        infoPanel = new PaintingInfoPanel();
        paintingSettingsPanel = new PaintingSettingsPanel();
        backGroundPaint.setARGB(100, 200, 200, 100);
        rankingFontPaint.setTextSize(Game.getScreenHeight() * (float) 5 / 100);
        rankingFontPaint.setTextAlign(Paint.Align.LEFT);
        prepareDrawingStage();
    }

    public static void handleSubjects(){
        System.out.println("Handling subs.");
        state = Consts.STATE_DRAW;
        State.setLoading(false);
        PaintingCanvas.reset();
        timer1.cancel();
        timer2.cancel();
        synchronized(paintingsLock) {
            synchronized (allPaintingsLock) {
                Utils.addListMembersToNewList(paintings, allPaintings);
            }
            paintings.clear();
        }
        PaintingInfoPanel.setSec((int) (Consts.TIME_TO_DRAW / 1000));
        PaintingInfoPanel.setSubject(subjects[currentSubject]);
        timeCounter = (int) (Consts.TIME_TO_DRAW / 1000);
        timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                if(timeCounter != 0){
                    timeCounter--;
                    PaintingInfoPanel.setSec(timeCounter);
                }else{
                    State.setLoading(true);
//                    synchronized(paintingsLock){
//                        paintings.add(new PaintingPainting(PaintingCanvas.getHashMap(), playerIndex, subjects[currentSubject], PaintingCanvas.getBackgroundColor()));
//                    }
                    state = Consts.STATE_NULL;
                    timer1.cancel();
                    vote = 1;
                    Networking.sendPainting();
                }
            }
        }, 1000, 1000);
    }

    private static void generateRandomOrder(){
        ArrayList<Integer> list = new ArrayList<>();
        for(int i = 0; i < paintings.size(); i++){
            list.add(i);
        }
        for(PaintingPainting x : paintings){
            x.setOrder(-1);
        }
        for(int i = 0; i < paintings.size(); i++){
            int t = (int) (Math.random() * list.size());
            paintings.get(paintings.size() - 1 - i).setOrder(list.get(t));
            list.remove(t);
        }
    }

    private static void sortByPoints(){
        Collections.sort(paintings, new Comparator<PaintingPainting>() {
            @Override
            public int compare(PaintingPainting o1, PaintingPainting o2) {
                if(o1.getPoints() > o2.getPoints()) return -1;
                else if(o1.getPoints() < o2.getPoints()) return 1;
                else return 0;
            }
        });
        int order = 0;
        for(int i = 0; i < paintings.size(); i++){
            paintings.get(i).setOrder(order);
            order++;
        }
    }

    public static void receivedAllVotes(){
        State.setLoading(false);
        votesReceived = 0;
        sortByPoints();
        timer1.cancel();
        timer2.cancel();
        PaintingInfoPanel.setSec(timeCounter);
        timeCounter = (int) (Consts.TIME_TO_SEE_RESULTS / 1000);
        currentSubject++;
        state = Consts.STATE_RESULTS;
        timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                if(timeCounter != 0){
                    timeCounter--;
                    PaintingInfoPanel.setSec(timeCounter);
                }else{
                    timer1.cancel();
                    State.setLoading(true);
                    if(currentSubject != subjects.length){
                        handleSubjects();
                    }
                }
            }
        }, 1000, 1000);
    }

    public static void pointsReceived(){
        prepareRanking();
        state = Consts.STATE_RANKINGS;
        paintings.clear();
        State.setLoading(false);
    }

    public static void receivedAllPaintings(){
        State.setLoading(false);
        timer1.cancel();
        timer2.cancel();
        generateRandomOrder();
        PaintingInfoPanel.setSec(timeCounter);
        state = Consts.STATE_VOTE;
        timeCounter = (int) (Consts.TIME_TO_VOTE / 1000);
        timer2 = new Timer();
        timer2.schedule(new TimerTask() {
            @Override
            public void run() {
                if(timeCounter != 0){
                    timeCounter--;
                    PaintingInfoPanel.setSec(timeCounter);
                }else{
                    state = Consts.STATE_NULL;
                    State.setLoading(true);
                    timer2.cancel();
                    processVotes();
                }
            }
        }, 1000, 1000);
    }

    private static void processVotes(){
        ArrayList<Byte> votes = new ArrayList<>();
        synchronized(paintingsLock){
            for(PaintingPainting x : paintings){
                if(x.getOrder() != -1){
                    x.setOrder(-1);
                    votes.add(x.getPlayer());
                    votes.add(x.getVote());
                }
            }
        }
        Networking.sendVotes(votes);
    }

    private static void prepareDrawingStage(){
        paintingCanvas.prepare();
        palette.prepare();
        brushPanel.prepare();
        toolsPanel.prepare();
        infoPanel.prepare();
        paintingSettingsPanel.prepare();
    }

    @Override
    public void tick() {

    }

    @Override
    public void render(Canvas canvas) {
        canvas.drawRect(0, 0, Game.getScreenWidth(), Game.getScreenHeight(), backGroundPaint);
        Button.renderButtons(canvas, buttons, buttonsLock);
        switch(state){
            case Consts.STATE_DRAW :
                paintingCanvas.render(canvas);
                palette.render(canvas);
                brushPanel.render(canvas);
                toolsPanel.render(canvas);
                infoPanel.render(canvas);
                paintingSettingsPanel.render(canvas);
                break;

            case Consts.STATE_VOTE :
                drawPaintings(canvas);
                infoPanel.render(canvas);
                break;

            case Consts.STATE_RESULTS :
                drawResults(canvas);
                break;

            case Consts.STATE_RANKINGS :
                setLoading(false);
                drawRankings(canvas);
                break;
        }
    }

    private void drawPaintings(Canvas canvas){
        synchronized(paintingsLock){
            for(PaintingPainting x : paintings){
                x.render(canvas);
            }
        }
    }

    private void drawResults(Canvas canvas){
        for(PaintingPainting x : paintings){
            x.renderResults(canvas);
        }
    }

    private static void prepareRanking(){
        rankingList.clear();

        for(int i = 0; i < Networking.getUsernames().length; i++){
            rankingList.add(new Object[]{Networking.getUsernames()[i], points[i]});
        }
        Collections.sort(rankingList, new Comparator<Object[]>() {
            @Override
            public int compare(Object[] o1, Object[] o2) {
                if((int) o1[1] > (int) o2[1]) return -1;
                if((int) o1[1] < (int) o2[1]) return 1;
                return 0;
            }
        });
    }

    private void drawRankings(Canvas canvas){
        for(int i = 0; i < Networking.getUsernames().length; i++){
            canvas.drawText(rankingList.get(i)[0] + "\t\t" + rankingList.get(i)[1], rankingStateLeft + (i / 3) * (Game.getScreenWidth() * (float) 20 / 100), rankingStateTop + (i % 3) * (Game.getScreenHeight() * (float) 20 / 100), rankingFontPaint);
        }
    }

    @Override
    public boolean onActionDown(MotionEvent event) {
        if(!Button.onActionDown(event, buttons, buttonsLock)){
            switch(state){
                case Consts.STATE_DRAW :
                    return PaintingCanvas.onActionDown(event);

                case Consts.STATE_VOTE :
                    return PaintingPainting.onActionDown(event, paintings, paintingsLock);
            }
        }

        return true;
    }

    @Override
    public boolean onActionPointerDown(MotionEvent event) {
        if(!Button.onActionPointerDown(event, buttons, buttonsLock)){
            switch(state){
                case Consts.STATE_DRAW :
                    return PaintingCanvas.onActionPointerDown(event);

                case Consts.STATE_VOTE :
                    return PaintingPainting.onActionPointerDown(event, paintings, paintingsLock);
            }
        }

        return true;
    }

    @Override
    public boolean onActionMove(MotionEvent event) {
        switch(state){
            case Consts.STATE_DRAW :
                return PaintingCanvas.onActionMove(event);
        }

        return true;
    }

    @Override
    public boolean onActionUp(MotionEvent event) {
        if(!Button.onActionUp(event, buttons, buttonsLock)){
            switch(state){
                case Consts.STATE_DRAW :
                    return PaintingCanvas.onActionUp(event);

                case Consts.STATE_VOTE :
                    return PaintingPainting.onActionUp(event, paintings, paintingsLock);
            }
        }

        return true;
    }

    @Override
    public boolean onActionPointerUp(MotionEvent event) {
        if(!Button.onActionPointerUp(event, buttons, buttonsLock)){
            switch(state){
                case Consts.STATE_DRAW :
                    return PaintingCanvas.onActionPointerUp(event);

                case Consts.STATE_VOTE :
                    return PaintingPainting.onActionPointerUp(event, paintings, paintingsLock);
            }
        }

        return true;
    }

    public static void settings(){
        PaintingSettingsPanel.toggleHide();
    }

    @Override
    public void endState() {
        paintings.clear();
        allPaintings.clear();
        vote = 1;
        votesReceived = 0;
        state = Consts.STATE_NULL;
        players = 0;
        subjects = null;
        id = "";
        timer1.cancel();
        timer2.cancel();
        timeCounter = 0;
        currentSubject = 0;
        buttons.clear();
        PaintingCanvas.reset();
        PaintingBrushPanel.reset();
        PaintingSettingsPanel.reset();
        PaintingPalette.reset();
        PaintingToolsPanel.reset();
    }

    public static byte getPlayers() {
        return players;
    }

    public static void setPlayers(byte players) {
        PaintingGameState.players = players;
    }

    public static String[] getSubjects() {
        return subjects;
    }

    public static void setSubjects(JSONArray subs) {
        subjects = new String[subs.length()];
        for(int i = 0; i < subjects.length; i++){
            try {
                subjects[i] = subs.getString(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void receivedPainting(HashMap<String, Node> hashMap, String player, int backgroundColor, double ratio){
        synchronized(paintingsLock){
            paintings.add(new PaintingPainting(hashMap, player, subjects[currentSubject], backgroundColor, ratio));
        }

        if(paintings.size() % players == 0){
            Networking.sendReceivedAllPaintings();
        }
    }

    public static void receivedVotes(int votes){
        votesReceived++;
        System.out.println("Votes are " + votes);
        for(int i = 0; i < players; i++){
            try{
                PaintingPainting p = paintings.get(i);
                int point = (votes / (int) (Math.pow(10, p.getPlayer()))) % 10;
                System.out.println(p.getPlayer());
                if (point == 0) continue;
                p.addPoints((6 - point));
                points[p.getPlayer()] += 6 - point;
            }catch(Exception ignored){}
        }

        if(votesReceived % players == 0){
            Networking.sendReceivedAllVotes();
        }
    }

    public static String getId() {
        return id;
    }

    public static void setId(String id) {
        PaintingGameState.id = id;
    }

    public static Timer getTimer1() {
        return timer1;
    }

    public static Timer getTimer2() {
        return timer2;
    }

    public static byte getVote() {
        return vote;
    }

    public static void setVote(byte vote) {
        PaintingGameState.vote = vote;
    }

    public static ArrayList<PaintingPainting> getPaintings() {
        return paintings;
    }

    public static Object getPaintingsLock() {
        return paintingsLock;
    }

    public static byte getState() {
        return state;
    }

    public static void setPoints(int[] points) {
        PaintingGameState.points = points;
    }
}