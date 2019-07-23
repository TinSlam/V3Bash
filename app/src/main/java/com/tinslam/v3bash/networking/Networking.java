package com.tinslam.comic.networking;

import com.tinslam.comic.UI.buttons.Node;
import com.tinslam.comic.activities.LoginActivity;
import com.tinslam.comic.activities.RegisterActivity;
import com.tinslam.comic.base.Game;
import com.tinslam.comic.base.GameThread;
import com.tinslam.comic.modes.conquer.ConquerGameState;
import com.tinslam.comic.modes.conquer.ConquerWaitingState;
import com.tinslam.comic.modes.hideAndSeek.HideAndSeekGameState;
import com.tinslam.comic.modes.maze.MazeGameState;
import com.tinslam.comic.modes.painting.PaintingCanvas;
import com.tinslam.comic.modes.painting.PaintingGameState;
import com.tinslam.comic.states.MainMenuState;
import com.tinslam.comic.states.RankingState;
import com.tinslam.comic.utils.Consts;
import com.tinslam.comic.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Networking{
    private static Socket socket;
    private static String clientSocketId = "";
    private static String username = "";
    private static boolean listening = false;
    private static ArrayList<String> paintingsReceivedArray = new ArrayList<>();
    private static ArrayList<String> votesReceivedArray = new ArrayList<>();
    private static String[] usernames;
    private static int[] points;
    private static int[][] modePoints;
    private static String[] modes;
    private static int modeCounter = 0;

    static {
        try {
            socket = IO.socket("http://" + Consts.SERVER_ADDRESS + ":" + String.valueOf(Consts.SERVER_PORT));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

//    public static void tcpSendPlayerPositions(){
////        try {
////            JSONObject json = new JSONObject();
////            json.put("id", Consts.UDP_PLAYER_POSITIONS);
////            json.put("x", Player.getPlayer().getX() / Game.density());
////            json.put("y", Player.getPlayer().getY() / Game.density());
//            emitWithAck("tcpSendPositions", Player.getPlayer().getX() / Game.density(), Player.getPlayer().getY() / Game.density());
////        } catch (JSONException e) {
////            e.printStackTrace();
////        }
//    }

    public static void resetIp(){
        socket.disconnect();
        try {
            socket = IO.socket("http://" + Consts.SERVER_ADDRESS + ":" + String.valueOf(Consts.SERVER_PORT));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static boolean start(){
        socket.connect();
        if(!socket.connected()) return false;
        if(listening) return true;
        System.out.println("Connected !");
        socket.off();
        listening = true;
        listen();
        emitWithAck("reconnected", clientSocketId);
        Game.getState().connected();
        return true;
    }

    public static void connectFromOtherActivities(){
        if(listening) return;
        socket.connect();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(socket.connected()){
                    if(!listening){
                        listening = true;
                        System.out.println("Connected from other activities !");
                        socket.off();
                        listen();
                        emitWithAck("reconnected", clientSocketId);
                    }
                }
            }
        }, 1000);
    }

    private static void emitWithAck(final String event, final Object... data){
        final Acknowledgement ack = new Acknowledgement();
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!ack.isRec()){
                    socket.emit(event, data, ack);
                }else{
                    timer.cancel();
                }
            }
        }, 0, 3000);
        final Timer timeout = new Timer();
        timeout.schedule(new TimerTask() {
            @Override
            public void run() {
                timeout.cancel();
            }
        }, 20 * 1000);
    }

    private static void disconnected(){
        listening = false;
        Game.getState().disconnected();
//        socket.disconnect();
    }

    public static void logout(){
        emitWithAck("logout");
    }

    private static void emitOnce(final String event, final Object... data){
        socket.emit(event, data);
    }

    public static void sendReceivedAllPaintings(){
        emitWithAck("paintingReceivedAllPaintings");
    }

    public static void sendReceivedAllVotes(){
        emitWithAck("paintingReceivedAllVotes");
    }

    public static void stopFindingMatch(){
        emitWithAck("stopFindingMatch");
    }

    public static void trophyReached(){
        emitWithAck("mazeTrophyReached");
    }

    public static void playerCaught(String username){
        emitWithAck("hideAndSeekPlayerCaught", username);
    }

    public static void login(String username, String password){
        if(!socket.connected()){
            LoginActivity.loginActivity().noConnection();
            return;
        }
        Game.setUsername(username);
        Game.setPassword(password);
        emitOnce("loginAttempt", username, password);
    }

    public static void register(String username, String password, String email){
        if(!socket.connected()){
            RegisterActivity.registerActivity().noConnection();
            return;
        }
        emitOnce("registerAttempt", username, password, email);
    }

    public static void verify(String code){
        emitWithAck("verifyAccount", code);
    }

    public static void sendPainting(){
        HashMap<String, Node> hashMap = PaintingCanvas.getHashMap();
        replaceDominantColorWithBackgroundColor(hashMap);
        int backgroundColorCode = PaintingCanvas.getBackgroundColor();
        PaintingGameState.receivedPainting(PaintingCanvas.getHashMap(), Networking.getUsername(), PaintingCanvas.getBackgroundColor(), PaintingCanvas.getRatio());
        JSONArray nodes = new JSONArray();
//        int[] nodes = new int[hashMap.size() + 1];
        nodes.put(backgroundColorCode);
        synchronized(PaintingCanvas.getNodesLock()) {
            for (Node x : hashMap.values()) {
                nodes.put(x.getX() + 1000 * x.getY() + 100000 * x.getColorCode() + 1000000 * x.getBrushStyle());
            }
        }
        emitWithAck("paintingSendingPainting", nodes, PaintingCanvas.getRatio());
    }

    private static void replaceDominantColorWithBackgroundColor(HashMap<String, Node> hashMap){
        int[] colorNumbers = PaintingCanvas.getNumberOfEachColor();
        int emptyNodes = 128 * 64 - hashMap.size();
        int dominantColor = Utils.getIndexOfHighestInArray(colorNumbers);
        if(dominantColor > emptyNodes){
            GameThread.pauseThread();
            synchronized(PaintingCanvas.getNodesLock()) {
                for (int i = 0; i < 128; i++) {
                    for (int j = 0; j < 64; j++) {
                        if (!hashMap.containsKey(Utils.getString(i, j))) {
                            hashMap.put(Utils.getString(i, j), new Node((byte) i, (byte) j, (byte) dominantColor, Consts.BRUSH_STYLE_RECT));
                        } else {
                            Node node = hashMap.get(Utils.getString(i, j));
                            if (node.getColorCode() == dominantColor) {
                                hashMap.remove(Utils.getString(i, j));
                            }
                        }
                    }
                }
            }
            GameThread.resumeThread();
            PaintingCanvas.setBackgroundColor(Utils.getColor((byte) dominantColor));
        }
    }

    public static void sendMazeReceived(){
        emitWithAck("mazeMazeReceived");
    }

    public static void sendVotes(ArrayList<Byte> list){
        int votes = 0;
        for(int i = 0; i < list.size(); i += 2){
            votes += (int) Math.pow(10, list.get(i)) * list.get(i + 1);
        }
        PaintingGameState.receivedVotes(votes);
        emitWithAck("paintingSendingVotes", votes);
    }

    public static void findMatch(){
        emitOnce("findMatch");
    }

    private static void listen(){
        socket.on("mazeRequestMaze", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                int[] array1D = MazeGameState.requestMaze();
                JSONArray jsArray = new JSONArray();
                for(int i = 0; i < array1D.length; i++){
                    jsArray.put(array1D[i]);
                }
                emitWithAck("mazeReceiveMaze", jsArray);
            }
        }).on("mazeReceiveMaze", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                JSONObject data = (JSONObject) args[0];
                try {
                    JSONArray jArray = data.getJSONArray("array1D");
                    int[] array1D = new int[jArray.length()];
                    for(int i = 0; i < array1D.length; i++){
                        array1D[i] = jArray.getInt(i);
                    }
                    MazeGameState.receiveMaze(array1D);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on("mazeStartMatch", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                MazeGameState.startMatch();
            }
        }).on("mazeTrophyReached", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                JSONObject data = (JSONObject) args[0];
                try {
                    String username = data.getString("username");
                    int x = data.getInt("x");
                    int y = data.getInt("y");
                    MazeGameState.trophyReached(username, x, y);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on("mazeSendFinalPoints", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                JSONObject data = (JSONObject) args[0];
                try{
                    JSONArray ja = data.getJSONArray("points");
                    int[] points = new int[ja.length()];
                    for(int i = 0; i < points.length; i++){
                        points[i] = ja.getInt(i);
                    }
                    setModePoints(Consts.MODE_MAZE, points);
                    Game.setState(new RankingState(usernames.length, modes.length));
                }catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on("mazeInstantiateState", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                Game.setState(new MazeGameState());
            }
        });

        socket.on("hideAndSeekStartMatch", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                HideAndSeekGameState.seekerReleased();
            }
        }).on("hideAndSeekSendFinalPoints", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                JSONObject data = (JSONObject) args[0];
                try{
                    JSONArray ja = data.getJSONArray("points");
                    int[] points = new int[ja.length()];
                    for(int i = 0; i < points.length; i++){
                        points[i] = ja.getInt(i);
                    }
                    setModePoints(Consts.MODE_HIDE_AND_SEEK, points);
                    Game.setState(new RankingState(usernames.length, modes.length));
                }catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on("hideAndSeekInstantiateState", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                Game.setState(new HideAndSeekGameState());
            }
        }).on("hideAndSeekKillPlayer", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                JSONObject json = (JSONObject) args[0];
                try {
                    HideAndSeekGameState.playerKilled(json.getString("username"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on("hideAndSeekAssignSeeker", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                JSONObject json = (JSONObject) args[0];
                try {
                    HideAndSeekGameState.assignSeeker(json.getString("username"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        socket.on("matchFound", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
            }
        }).on("sendFinalPoints", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                JSONObject data = (JSONObject) args[0];
                try{
                    JSONArray ja = data.getJSONArray("points");
                    int[] points = new int[ja.length()];
                    for(int i = 0; i < points.length; i++){
                        points[i] = ja.getInt(i);
                    }
                    setPoints(points);
                    RankingState rState = new RankingState(usernames.length, modes.length);
                    Game.setState(rState);
                    rState.setCanExit(true);
                }catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                disconnected();
            }
        }).on("logout", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                Game.setPassword("");
                Game.setUsername("");
                username = "";
                socket.disconnect();
                Game.setState(new MainMenuState());
            }
        }).on("multipleLogins", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                Game.setPassword("");
                Game.setUsername("");
                username = "";
                socket.disconnect();
                Game.setState(new MainMenuState());
            }
        }).on("verifyFailed", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                RegisterActivity.registerActivity().verifyFailed();
            }
        }).on("verifyCodeInvalid", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                RegisterActivity.registerActivity().verifyCodeInvalid();
            }
        }).on("sendUsernames", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                JSONObject data = (JSONObject) args[0];
                try {
                    JSONArray array = data.getJSONArray("usernames");
                    JSONArray arrayModes = data.getJSONArray("modes");
                    setUsernames(array);
                    setModes(arrayModes);
                    points = new int[array.length()];
                    for(int i = 0; i < array.length(); i++){
                        points[i] = 0;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on("loginFailed", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                LoginActivity.loginActivity().loginFailed();
            }
        }).on("loginSuccessful", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                JSONObject data = (JSONObject) args[0];
                try {
                    username = data.getString("username");
                    LoginActivity.loginActivity().loginSuccessful();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on("registerFailed", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                RegisterActivity.registerActivity().registerFailed();
            }
        }).on("verify", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                JSONObject data = (JSONObject) args[0];
                try {
                    RegisterActivity.registerActivity().setVerifyCode(data.getString("verifyCode"));
                    RegisterActivity.registerActivity().verify();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on("registerSuccessful", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                RegisterActivity.registerActivity().registerSuccessful();
            }
        }).on("socketId", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                JSONObject data = (JSONObject) args[0];
                try {
                    clientSocketId = data.getString("socketId");
                    PaintingGameState.setId(clientSocketId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on("playerDisconnected", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
//                System.out.println("Someone dced.");
                PaintingGameState.setPlayers((byte) (PaintingGameState.getPlayers() - 1));
            }
        }).on("playerReconnected", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                PaintingGameState.setPlayers((byte) (PaintingGameState.getPlayers() + 1));
            }
        });

        socket.on("paintingHandleSubjects", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                if(PaintingGameState.getState() == Consts.STATE_RESULTS || PaintingGameState.getState() == Consts.STATE_NULL) PaintingGameState.handleSubjects();
            }
        }).on("paintingSendFinalPoints", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                JSONObject data = (JSONObject) args[0];
                try{
                    JSONArray ja = data.getJSONArray("points");
                    int[] points = new int[ja.length()];
                    for(int i = 0; i < points.length; i++){
                        points[i] = ja.getInt(i);
                    }
//                    PaintingGameState.setPoints(points);
                    setModePoints(Consts.MODE_PAINTING, points);
//                    PaintingGameState.pointsReceived();
                    Game.setState(new RankingState(usernames.length, modes.length));
                }catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on("paintingHandlePaintings", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                if(PaintingGameState.getState() == Consts.STATE_NULL){
                    paintingsReceivedArray.clear();
                    PaintingGameState.receivedAllPaintings();
                }
            }
        }).on("paintingHandleVotes", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                if(PaintingGameState.getState() == Consts.STATE_NULL){
                    votesReceivedArray.clear();
                    PaintingGameState.receivedAllVotes();
                }
            }
        }).on("paintingInstantiateState", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                System.out.println("New PSTATE");
                Game.setState(new PaintingGameState((byte) usernames.length));
            }
        }).on("paintingReceiveSubjects", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                if(PaintingGameState.getState() != Consts.STATE_NULL) return;
                JSONObject data = (JSONObject) args[0];
                try {
                    PaintingGameState.setSubjects(data.getJSONArray("subs"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on("paintingReceivePainting", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                JSONObject data = (JSONObject) args[0];
                HashMap<String, Node> hashMap = new HashMap<>();
                int[] nodes = null;
                String player = "";
                double ratio = 0;
                try {
                    JSONArray array = data.getJSONArray("nodes");
                    ratio = data.getDouble("ratio");
                    player = data.getString("player");
                    if(paintingsReceivedArray.contains(player)) return;
                    nodes = new int[array.length()];
                    for(int i = 0; i < array.length(); i++){
                        nodes[i] = array.getInt(i);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for(int i = 1; i < nodes.length; i++){
                    int x = nodes[i] % 1000;
                    int n1 = nodes[i] / 1000;
                    int y = n1 % 100;
                    int n2 = n1 / 100;
                    int colorCode = n2 % 10;
                    int n3 = n2 / 10;
                    int brushStyle = n3 % 10;
                    hashMap.put(Utils.getString(x, y), new Node((byte) x, (byte) y, (byte) colorCode, (byte) brushStyle));
                }
                paintingsReceivedArray.add(player);
                PaintingGameState.receivedPainting(hashMap, player, nodes[0], ratio);
            }
        }).on("paintingReceiveVotes", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                JSONObject data = (JSONObject) args[0];
                int votes = 0;
                try {
                    votes = data.getInt("votes");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                int player = votes % 10;
                if(votesReceivedArray.contains(Networking.getUsernames()[player])) return;
                votesReceivedArray.add(Networking.getUsernames()[player]);
                votes /= 10;
                PaintingGameState.receivedVotes(votes);
            }
        });

        socket.on("conquerInstantiateState", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                Game.setState(new ConquerWaitingState());
            }
        }).on("conquerSendFinalPoints", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                JSONObject data = (JSONObject) args[0];
                try{
                    JSONArray ja = data.getJSONArray("points");
                    int[] points = new int[ja.length()];
                    for(int i = 0; i < points.length; i++){
                        points[i] = ja.getInt(i);
                    }
                    setModePoints(Consts.MODE_CONQUER, points);
                    Game.setState(new RankingState(usernames.length, modes.length));
                }catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on("conquerUpdateTroops", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                JSONObject data = (JSONObject) args[0];
                try {
                    JSONArray ja = data.getJSONArray("array");
                    int[] array = new int[ja.length()];
                    for(int i = 0; i < array.length; i++){
                        array[i] = ja.getInt(i);
                    }
                    ConquerGameState.updateTroops(array);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on("conquerStartMatch", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                JSONObject data = (JSONObject) args[0];
                try {
                    boolean player2 = data.getBoolean("player2");
                    String username = data.getString("username");
                    Game.setState(new ConquerGameState(username, player2));
                    ConquerGameState.startMatch();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on("conquerSendTroops", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                JSONObject data = (JSONObject) args[0];
                try {
                    int troops = data.getInt("troops");
                    int src = data.getInt("src");
                    int dst = data.getInt("dst");
                    String username = data.getString("username");
                    int time = data.getInt("time");
                    ConquerGameState.transportTroops(src, dst, troops, username, time);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on("conquerTroopsArrived", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                JSONObject data = (JSONObject) args[0];
                try {
                    int troops = data.getInt("dstTroops");
                    String username = data.getString("username");
                    int dst = data.getInt("dst");
                    byte team = Consts.CONQUER_TEAM_ENEMY;
                    if(username.equalsIgnoreCase(getUsername())){
                        team = Consts.CONQUER_TEAM_ALLY;
                    }else if(username.equalsIgnoreCase("n")){
                        team = Consts.CONQUER_TEAM_NEUTRAL;
                    }
                    ConquerGameState.troopsReached(dst, troops, team);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on("conquerRoundEnded", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Ack callback = (Ack) args[args.length - 1];
                callback.call();
                JSONObject data = (JSONObject) args[0];
                try{
                    JSONArray ja = data.getJSONArray("points");
                    int[] points = new int[ja.length()];
                    int index = -1;

                    for(int i = 0; i < ja.length(); i++){
                        points[i] = ja.getInt(i);
                    }

                    String modeName = Utils.getModeNameFromByte(Consts.MODE_CONQUER);
                    for(int i = 0; i < modes.length; i++){
                        if(modeName.equalsIgnoreCase(modes[i])){
                            index = i;
                            break;
                        }
                    }
                    if(index == -1) return;

                    for(int i = 0; i < points.length; i++){
                        Networking.points[i] += points[i] - modePoints[index][i];
                    }

                    System.arraycopy(points, 0, modePoints[index], 0, usernames.length);
                    Game.setState(new RankingState(usernames.length, modes.length));
                }catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        });

//        socket.on("tcpSendPositions", new Emitter.Listener() {
//            @Override
//            public void call(Object... args) {
//                Ack callback = (Ack) args[args.length - 1];
//                callback.call();
//                Method m = null;
//                JSONObject data = (JSONObject) args[0];
//                try {
//                    m = Game.getState().getClass().getMethod("setPositions", String.class, int.class, int.class);
//                    m.invoke(null, data.getString("username"), (int) data.getDouble("x"), (int) data.getDouble("y"));
//                } catch (NoSuchMethodException e) {
//                    e.printStackTrace();
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                } catch (InvocationTargetException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).on("tcpSendSelfPositions", new Emitter.Listener() {
//            @Override
//            public void call(Object... args) {
//                Ack callback = (Ack) args[args.length - 1];
//                callback.call();
//                Method m = null;
//                JSONObject data = (JSONObject) args[0];
//                try {
//                    m = Game.getState().getClass().getMethod("setPositions", String.class, int.class, int.class);
//                    m.invoke(null, Networking.getUsername(), (int) data.getDouble("x"), (int) data.getDouble("y"));
//                } catch (NoSuchMethodException e) {
//                    e.printStackTrace();
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                } catch (InvocationTargetException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    public static Socket getSocket() {
        return socket;
    }

    public static String getUsername() {
        return username;
    }

    public static byte getPlayerIndex(String username){
        for(int i = 0; i < usernames.length; i++){
            if(usernames[i].equalsIgnoreCase(username)) return (byte) i;
        }

        return 0;
    }

    public static void setUsernames(JSONArray users) {
        usernames = new String[users.length()];
        for(int i = 0; i < usernames.length; i++){
            try {
                usernames[i] = users.getString(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static String[] getUsernames() {
        return usernames;
    }

    public static void setModes(JSONArray users) {
        modes = new String[users.length()];
        modePoints = new int[modes.length][usernames.length];
        for(int i = 0; i < modes.length; i++){
            for(int j = 0; j < usernames.length; j++){
                modePoints[i][j] = 0;
            }
            try {
                modes[i] = Utils.getModeNameFromByte(users.getInt(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static String[] getModes() {
        return modes;
    }

    public static void setPoints(int[] array) {
        for(int i = 0; i < array.length; i++){
            try{
                points[i] = array[i];
            }catch(Exception ignored){}
        }
    }

    public static void addPoints(int[] array) {
        for(int i = 0; i < array.length; i++){
            try{
                points[i] += array[i];
            }catch(Exception ignored){}
        }
    }

    public static int[] getPoints() {
        return points;
    }

    public static int[][] getModePoints() {
        return modePoints;
    }

    private static void setModePoints(byte mode, int[] points) {
        int index = -1;
        String modeName = Utils.getModeNameFromByte(mode);
        for(int i = 0; i < modes.length; i++){
            if(modeName.equalsIgnoreCase(modes[i])){
                index = i;
                break;
            }
        }
        if(index == -1) return;

        System.arraycopy(points, 0, modePoints[index], 0, usernames.length);
        addPoints(points);
    }

    public static void conquerSendTroops(int src, int troops, int dst) {
        emitWithAck("conquerSendTroops", src, troops, dst);
    }
}