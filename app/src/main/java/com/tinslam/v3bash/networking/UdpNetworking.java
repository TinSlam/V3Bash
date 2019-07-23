package com.tinslam.comic.networking;

import com.tinslam.comic.base.Game;
import com.tinslam.comic.gameElements.entity.movingEntity.Player;
import com.tinslam.comic.modes.maze.MazeGameState;
import com.tinslam.comic.utils.Consts;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public abstract class UdpNetworking {
    private static DatagramSocket socket;
    private static InetAddress serverInetAddress;
    private static byte[] buffer = new byte[1024];

    private static void handleRP(byte[] buffer) throws IOException, ClassNotFoundException {
        try {
            JSONObject json = new JSONObject(new String(buffer));
            switch (json.getInt("id")) {
                case Consts.UDP_PLAYER_POSITIONS:
                    Method m = Game.getState().getClass().getMethod("setPositions", String.class, int.class, int.class);
                    m.invoke(null, json.getString("username"), (int) json.getDouble("x"), (int) json.getDouble("y"));
                    break;

                case Consts.UDP_SELF_POSITIONS:
                    m = Game.getState().getClass().getMethod("setPositions", String.class, int.class, int.class);
                    m.invoke(null, Networking.getUsername(), (int) json.getDouble("x"), (int) json.getDouble("y"));
                    break;
            }
        } catch (JSONException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void sendPlayerPositions() {
//        Networking.tcpSendPlayerPositions();
        try {
            JSONObject json = new JSONObject();
            json.put("id", Consts.UDP_PLAYER_POSITIONS);
            json.put("x", Player.getPlayer().getX() / Game.density());
            json.put("y", Player.getPlayer().getY() / Game.density());
            sendUdpPacket(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void listen() {
        Thread listenerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    DatagramPacket rp = new DatagramPacket(buffer, buffer.length);
                    try {
                        socket.receive(rp);
                        handleRP(rp.getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        listenerThread.start();
    }

    public static void startUdpConnection(final Class gameClass) {
        try {
            socket = new DatagramSocket();
            serverInetAddress = InetAddress.getByName(Consts.SERVER_ADDRESS);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        (new Thread(new Runnable() {
            @Override
            public void run() {
                while (!socket.isConnected()) {
                    try {
                        socket.connect(serverInetAddress, Consts.SERVER_PORT);
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Method method = gameClass.getMethod("onUdpConnection");
                    method.invoke(null);
                    listen();
                    JSONObject json = new JSONObject();
                    json.put("id", Consts.UDP_CONNECTED);
                    json.put("username", Networking.getUsername());
                    sendUdpPacket(json);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        })).start();
    }

    public static void endUdpConnection() {
        socket.close();
    }

    public static void sendUdpPacket(JSONObject json) {
        byte[] buff = json.toString().getBytes();
        DatagramPacket sp = new DatagramPacket(buff, buff.length, serverInetAddress, Consts.SERVER_PORT);
        try {
            socket.send(sp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
