package com.tinslam.comic.modes.painting;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;

import com.tinslam.comic.UI.buttons.Node;
import com.tinslam.comic.base.Game;
import com.tinslam.comic.base.GameThread;
import com.tinslam.comic.utils.Consts;
import com.tinslam.comic.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public class PaintingCanvas {
    private static Paint borderPaint = new Paint();
    private static Paint canvasBackGroundPaint = new Paint();
    private static Paint drawingPaint = new Paint();
    private static int xOffset = (int) ((64 + 24) * Game.density()), yOffset = (int) (8 * Game.density());
    private static int width = (int) (512 * Game.density()), height = (int) ((256 + 32) * Game.density());
    private static int rightOffset;
    private static Rect rect;
    private static byte colorCode = Consts.COLOR_BLACK;
    private static byte elementWidth, elementHeight;
    private static ArrayList<HashMap<String, Node>> nodesHistory = new ArrayList<>();
    private static final Object nodesLock = new Object();
    private static final Object nodesHistoryLock = new Object();
    private static Point[] lastMoveActionPoint = new Point[5];
    private static int nodesIndex = 0;
    private static ArrayList<Integer> pointers = new ArrayList<>();
    private static final Object pointersLock = new Object();
    private static final Object undoRedoLock = new Object();
    private static byte brushStyle = Consts.BRUSH_STYLE_RECT;
    private static byte brushStyleLast = Consts.BRUSH_STYLE_RECT;
    private static HashMap<String, Node> nodesMap = new HashMap<>();
    private static boolean showGrid = false;
    private static int[] numberOfEachColor = new int[Consts.COLOR_NUMBER];
    private static double ratio;

//    public PaintingCanvas(){
//        colorCode = Consts.COLOR_BLACK;
//        lastMoveActionPoint = new Point[5];
//        nodesIndex = 0;
//        brushStyle = Consts.BRUSH_STYLE_RECT;
//    }

    public static void prepare(){
        for(int i = 0; i < Consts.COLOR_NUMBER; i++){
            numberOfEachColor[i] = 0;
        }
        HashMap<String, Node> newNodes;
        synchronized(nodesLock){
            synchronized(nodesHistoryLock){
                newNodes = (HashMap<String, Node>) nodesMap.clone();
                nodesHistory.add(newNodes);
            }
        }
        for(int i = 0; i < lastMoveActionPoint.length; i++){
            lastMoveActionPoint[i] = new Point(-1, -1);
        }
        int u = (int) (Game.getScreenWidth() - 16 * Game.density());
        int v = (int) (Game.getScreenHeight() - (16) * Game.density());
        if(u > 128){
            width = u / 128 * 128;
        }else{
            width = 64;
        }
        height = v / 64 * 64;
        xOffset = (Game.getScreenWidth() - width) / 2;
        yOffset = (Game.getScreenHeight() - height) / 2;
        rightOffset = Game.getScreenWidth() - width - xOffset;
        rect = new Rect(xOffset, yOffset, xOffset + width, yOffset + height);
        elementWidth = (byte) (width / 128);
        elementHeight = (byte) (height / 64);
        ratio = (double) elementWidth / elementHeight;
        borderPaint.setStyle(Paint.Style.STROKE);
        canvasBackGroundPaint.setColor(Utils.getColor(Consts.COLOR_WHITE));
    }

    public static boolean onActionDown(MotionEvent event){
        lastMoveActionPoint[event.getPointerId(0)].set(-1, -1);
        if(!rect.contains((int) event.getX(), (int) event.getY())) return false;
        synchronized(pointersLock){
            pointers.add(event.getPointerId(0));
        }

        byte x = getMatrixX((int) event.getX());
        byte y = getMatrixY((int) event.getY());
        switch(brushStyle){
            case Consts.BRUSH_FILL :
                GameThread.pauseThread();
                synchronized(nodesLock){
                    fill(x, y);
                }
                GameThread.resumeThread();
                return true;

            case Consts.BRUSH_STYLE_RECT :
                Node node = getNodeInList(x, y);

                if(node == null){
                    synchronized(nodesLock){
                        Node newNode = new Node(x, y, colorCode, brushStyle);
                        nodesMap.put(Utils.getString(x, y), newNode);
                        numberOfEachColor[colorCode]++;
                    }
                }else{
                    synchronized(nodesLock){
                        nodesMap.remove(Utils.getString(node.getX(), node.getY()));
                        numberOfEachColor[node.getColorCode()]--;
                        nodesMap.put(Utils.getString(node.getX(), node.getY()), new Node(node.getX(), node.getY(), colorCode, brushStyle));
                        numberOfEachColor[colorCode]++;
                    }
                }

                return true;

            default :
                return true;
        }
    }

    public static boolean onActionPointerDown(MotionEvent event){
        lastMoveActionPoint[event.getPointerId(event.getActionIndex())].set(-1, -1);
        if(!rect.contains((int) event.getX(event.getActionIndex()), (int) event.getY(event.getActionIndex()))) return false;
        synchronized(pointersLock){
            pointers.add(event.getPointerId(event.getActionIndex()));
        }

        byte x = getMatrixX((int) event.getX(event.getActionIndex()));
        byte y = getMatrixY((int) event.getY(event.getActionIndex()));

        switch(brushStyle) {
            case Consts.BRUSH_FILL :
                GameThread.pauseThread();
                synchronized(nodesLock){
                    fill(x, y);
                }
                GameThread.resumeThread();
            return true;

            case Consts.BRUSH_STYLE_RECT:
                Node node = getNodeInList(x, y);

                if (node == null) {
                    synchronized (nodesLock) {
                        Node newNode = new Node(x, y, colorCode, brushStyleLast);
                        nodesMap.put(Utils.getString(x, y), newNode);
                        numberOfEachColor[colorCode]++;
                    }
                } else {
                    synchronized(nodesLock){
                        nodesMap.remove(Utils.getString(node.getX(), node.getY()));
                        nodesMap.put(Utils.getString(node.getX(), node.getY()), new Node(node.getX(), node.getY(), colorCode, brushStyle));
                        numberOfEachColor[colorCode]++;
                        numberOfEachColor[node.getColorCode()]--;
                    }
                }

                return true;

            default:
                return true;
        }
    }

    public static boolean onActionMove(MotionEvent event){
        ArrayList<Integer> indices = new ArrayList<>();
        synchronized(pointersLock){
            for(Integer x : pointers){
                for(int i = 0; i < event.getPointerCount(); i++){
                    if(x == event.getPointerId(i)){
                        indices.add(i);
                        break;
                    }
                }
            }
            if(indices.isEmpty()) return false;
        }
        for(Integer index : indices){
            if(!rect.contains((int) event.getX(index), (int) event.getY(index))) return false;

            byte x = getMatrixX((int) event.getX(index));
            byte y = getMatrixY((int) event.getY(index));

            switch(brushStyle){
                case Consts.BRUSH_STYLE_RECT :
                    byte x0, y0;

                    try{
                        int u = lastMoveActionPoint[event.getPointerId(index)].x;
                        int v = lastMoveActionPoint[event.getPointerId(index)].y;
                        if(u == -1 && v == -1) throw new Exception();
                        x0 = (byte) u;
                        y0 = (byte) v;
                    }catch(Exception e){
                        x0 = x;
                        y0 = y;
                    }

                    setNode(x0, y0, x, y);
                    lastMoveActionPoint[event.getPointerId(index)].set(x, y);
                    return true;

                default :
                    return true;
            }
        }
        return true;
    }

    private static void setNode(byte x0, byte y0, byte x, byte y){
        float m;
        try{
            if(x == x0) throw new Exception();
            m = (y - y0) / (float) (x - x0);
        }catch(Exception e){
            if(y >= y0){
                for(int i = y0; i <= y; i++) {
                    Node node = getNodeInList(x, i);

                    synchronized (nodesLock) {
                        if (node == null) {
                            Node newNode = new Node(x, (byte) i, colorCode, brushStyle);
                            nodesMap.put(Utils.getString(newNode.getX(), newNode.getY()), newNode);
                            numberOfEachColor[colorCode]++;
                        } else {
                            synchronized(nodesLock){
                                nodesMap.remove(Utils.getString(node.getX(), node.getY()));
                                nodesMap.put(Utils.getString(node.getX(), node.getY()), new Node(node.getX(), node.getY(), colorCode, brushStyle));
                                numberOfEachColor[colorCode]++;
                                numberOfEachColor[node.getColorCode()]--;
                            }
                        }
                    }
                }
            }else{
                for(int i = y0; i >= y; i--){
                    Node node = getNodeInList(x, i);

                    synchronized(nodesLock){
                        if(node == null){
                            Node newNode = new Node(x, (byte) i, colorCode, brushStyle);
                            nodesMap.put(Utils.getString(newNode.getX(), newNode.getY()), newNode);
                            numberOfEachColor[colorCode]++;
                        }else{
                            synchronized(nodesLock){
                                nodesMap.remove(Utils.getString(node.getX(), node.getY()));
                                nodesMap.put(Utils.getString(node.getX(), node.getY()), new Node(node.getX(), node.getY(), colorCode, brushStyle));
                                numberOfEachColor[colorCode]++;
                                numberOfEachColor[node.getColorCode()]--;
                            }
                        }
                    }
                }
            }
            return;
        }
        if(Math.abs(m) <= 1) {
            if (x > x0) {
                for (int i = x0; i <= x; i++) {
                    Node node = getNodeInList(i, Math.round((i - x0) * m + y0));

                    if (node == null) {
                        synchronized (nodesLock) {
                            Node newNode = new Node((byte) i, (byte) Math.round((i - x0) * m + y0), colorCode, brushStyle);
                            nodesMap.put(Utils.getString(newNode.getX(), newNode.getY()), newNode);
                            numberOfEachColor[colorCode]++;
                        }
                    } else {
                        synchronized(nodesLock){
                            nodesMap.remove(Utils.getString(node.getX(), node.getY()));
                            nodesMap.put(Utils.getString(node.getX(), node.getY()), new Node(node.getX(), node.getY(), colorCode, brushStyle));
                            numberOfEachColor[colorCode]++;
                            numberOfEachColor[node.getColorCode()]--;
                        }
                    }
                }
            } else {
                for (int i = x0; i >= x; i--) {
                    Node node = getNodeInList(i, Math.round((i - x0) * m + y0));

                    if (node == null) {
                        synchronized (nodesLock) {
                            Node newNode = new Node((byte) i, (byte) Math.round((i - x0) * m + y0), colorCode, brushStyle);
                            nodesMap.put(Utils.getString(newNode.getX(), newNode.getY()), newNode);
                            numberOfEachColor[colorCode]++;
                        }
                    } else {
                        synchronized(nodesLock){
                            nodesMap.remove(Utils.getString(node.getX(), node.getY()));
                            nodesMap.put(Utils.getString(node.getX(), node.getY()), new Node(node.getX(), node.getY(), colorCode, brushStyle));
                            numberOfEachColor[colorCode]++;
                            numberOfEachColor[node.getColorCode()]--;
                        }
                    }
                }
            }
        }else{
            if (y > y0) {
                for (int i = y0; i <= y; i++) {
                    Node node = getNodeInList(Math.round((i - y0) / m + x0), i);

                    if (node == null) {
                        synchronized (nodesLock) {
                            Node newNode = new Node((byte) Math.round((i - y0) / m + x0), (byte) i, colorCode, brushStyle);
                            nodesMap.put(Utils.getString(newNode.getX(), newNode.getY()), newNode);
                            numberOfEachColor[colorCode]++;
                        }
                    } else {
                        synchronized(nodesLock){
                            nodesMap.remove(Utils.getString(node.getX(), node.getY()));
                            nodesMap.put(Utils.getString(node.getX(), node.getY()), new Node(node.getX(), node.getY(), colorCode, brushStyle));
                            numberOfEachColor[colorCode]++;
                            numberOfEachColor[node.getColorCode()]--;
                        }
                    }
                }
            } else {
                for (int i = y0; i >= y; i--) {
                    Node node = getNodeInList(Math.round((i - y0) / m + x0), i);

                    if (node == null) {
                        synchronized (nodesLock) {
                            Node newNode = new Node((byte) Math.round((i - y0) / m + x0), (byte) i, colorCode, brushStyle);
                            nodesMap.put(Utils.getString(newNode.getX(), newNode.getY()), newNode);
                            numberOfEachColor[colorCode]++;
                        }
                    } else {
                        synchronized(nodesLock){
                            nodesMap.remove(Utils.getString(node.getX(), node.getY()));
                            nodesMap.put(Utils.getString(node.getX(), node.getY()), new Node(node.getX(), node.getY(), colorCode, brushStyle));
                            numberOfEachColor[colorCode]++;
                            numberOfEachColor[node.getColorCode()]--;
                        }
                    }
                }
            }
        }
    }

    public static boolean onActionUp(MotionEvent event){
        synchronized(pointersLock){
            for(Integer x : pointers){
                if(x == event.getPointerId(0)){
                    actionUpUndoRedoHandler();
                    pointers.remove(x);
                    break;
                }
            }
        }
        if(!rect.contains((int) event.getX(), (int) event.getY())) return false;

        byte x = getMatrixX((int) event.getX(0));
        byte y = getMatrixY((int) event.getY(0));

        return true;
    }

    private static void actionUpUndoRedoHandler(){
        synchronized(nodesHistoryLock){
            int t = nodesHistory.size();
            for(int i = nodesIndex + 1; i < t; i++){
                nodesHistory.remove(nodesHistory.size() - 1);
            }
            HashMap<String, Node> newNodes;
            synchronized(nodesLock){
                newNodes = (HashMap<String, Node>) nodesMap.clone();
            }
            nodesHistory.add(newNodes);
        }
        nodesIndex = nodesHistory.size() - 1;
    }

    public static boolean onActionPointerUp(MotionEvent event){
        synchronized(pointersLock){
            for(Integer x : pointers){
                if(x == event.getPointerId(event.getActionIndex())){
                    actionUpUndoRedoHandler();
                    pointers.remove(x);
                    break;
                }
            }
        }
        if(!rect.contains((int) event.getX(event.getActionIndex()), (int) event.getY(event.getActionIndex()))) return false;

        byte x = getMatrixX((int) event.getX(event.getActionIndex()));
        byte y = getMatrixY((int) event.getY(event.getActionIndex()));

        return true;
    }

    private static void fill(byte x, byte y){
        Node node = getNodeInList(x, y);
        if(node == null){
            synchronized(nodesLock){
                Node newNode = new Node(x, y, colorCode, Consts.BRUSH_STYLE_RECT);
                nodesMap.put(Utils.getString(x, y), newNode);
                numberOfEachColor[colorCode]++;
            }
            if(x > 0) if(getNodeInList(x - 1, y) == null) fill((byte) (x - 1), y);
            if(y > 0) if(getNodeInList(x, y - 1) == null) fill(x, (byte) (y - 1));
            if(x < 127) if(getNodeInList(x + 1, y) == null) fill((byte) (x + 1), y);
            if(y < 63) if(getNodeInList(x, y + 1) == null) fill(x, (byte) (y + 1));
        }else{
            Node up = getNodeInList(x, y - 1);
            Node right = getNodeInList(x + 1, y);
            Node left = getNodeInList(x - 1, y);
            Node down = getNodeInList(x, y + 1);
            boolean l = false, r = false, u = false, d = false;

            if(x > 0) if(left != null) if(left.getColorCode() == node.getColorCode() && left.getColorCode() != colorCode) l = true;
            if(y > 0) if(up != null) if(up.getColorCode() == node.getColorCode() && up.getColorCode() != colorCode) u = true;
            if(x < 127) if(right != null) if(right.getColorCode() == node.getColorCode() && right.getColorCode() != colorCode) r = true;
            if(y < 63) if(down != null) if(down.getColorCode() == node.getColorCode() && down.getColorCode() != colorCode) d = true;

            synchronized(nodesLock){
                nodesMap.remove(Utils.getString(node.getX(), node.getY()));
                nodesMap.put(Utils.getString(node.getX(), node.getY()), new Node(node.getX(), node.getY(), colorCode, Consts.BRUSH_STYLE_RECT));
                numberOfEachColor[colorCode]++;
                numberOfEachColor[node.getColorCode()]--;
            }

            if(l) fill((byte) (x - 1), y);
            if(u) fill(x, (byte) (y - 1));
            if(r) fill((byte) (x + 1), y);
            if(d) fill(x, (byte) (y + 1));
        }
    }

    public static void reset(){
        PaintingCanvas.nodesMap.clear();
        PaintingCanvas.colorCode = Consts.COLOR_BLACK;
        PaintingCanvas.nodesHistory.clear();
        PaintingCanvas.nodesIndex = 0;
        PaintingCanvas.lastMoveActionPoint = new Point[5];
        PaintingCanvas.canvasBackGroundPaint.setColor(Consts.COLOR_WHITE);
        PaintingCanvas.pointers.clear();
        PaintingCanvas.prepare();
    }

    public void render(Canvas canvas){
        canvas.drawRect(xOffset, yOffset, xOffset + width, yOffset + height, canvasBackGroundPaint);
        renderNodes(canvas);
        if(showGrid) renderGrid(canvas);
        canvas.drawRect(xOffset, yOffset, xOffset + width, yOffset + height, borderPaint);
    }

    private void renderNodes(Canvas canvas){
        synchronized(nodesLock){
            for(Node x : nodesMap.values()){
                drawingPaint.setColor(Utils.getColor(x.getColorCode()));
                switch(x.getBrushStyle()){
                    case Consts.BRUSH_STYLE_RECT :
                        canvas.drawRect(xOffset + x.getX() * elementWidth, yOffset + x.getY() * elementHeight,
                                xOffset + (x.getX() + 1) * elementWidth, yOffset + (x.getY() + 1) * elementHeight, drawingPaint);
                        break;

//                    case Consts.BRUSH_STYLE_OVAL :
//                        canvas.drawCircle((int) (xOffset + x.getX() * elementWidth + elementWidth / 2), (int) (yOffset + x.getY() * elementHeight + elementHeight / 2),
//                                elementWidth, drawingPaint);
//                        break;
                }
            }
        }
    }

    private void renderGrid(Canvas canvas){
        for(int i = 0; i < 128; i++){
            canvas.drawLine(xOffset + i * elementWidth, yOffset, xOffset + i * elementWidth, yOffset + height, borderPaint);
        }
        for(int i = 0; i < 64; i++){
            canvas.drawLine(xOffset, yOffset + i * elementHeight, xOffset + width, yOffset + i * elementHeight, borderPaint);
        }
    }

    private static Node getNodeInList(int x, int y){
        return nodesMap.get(Utils.getString(x, y));
    }

    static void undo(){
        synchronized(undoRedoLock) {
            if (nodesIndex == 0) return;
            nodesMap = (HashMap<String, Node>) nodesHistory.get(nodesIndex - 1).clone();
            nodesIndex--;
        }
    }

    static void redo(){
        synchronized(undoRedoLock){
            if(nodesIndex == nodesHistory.size() - 1) return;
            nodesMap = (HashMap<String, Node>) nodesHistory.get(nodesIndex + 1).clone();
            nodesIndex++;
        }
    }

    private static byte getMatrixX(int x){
        return (byte) ((x - xOffset) / elementWidth);
    }

    private static byte getMatrixY(int y){
        return (byte) ((y - yOffset) / elementHeight);
    }

    public static int getRightOffset() {
        return rightOffset;
    }

    public static int getyOffset() {
        return yOffset;
    }

    public static double getRatio() {
        return ratio;
    }

    public static void setColor(byte colorCode){
        PaintingCanvas.colorCode = colorCode;
    }

    static void setBrushStyle(byte brushStyle){
        PaintingCanvas.brushStyleLast = PaintingCanvas.brushStyle;
        PaintingCanvas.brushStyle = brushStyle;
    }

    public static void setBackgroundColor(int color){
        canvasBackGroundPaint.setColor(color);
    }

    public static int getColor(){
        return Utils.getColor(colorCode);
    }

    public static void toggleGrid(){
        showGrid = !showGrid;
    }

    public static HashMap<String, Node> getHashMap(){
        return nodesMap;
    }

    public static int getBackgroundColor() {
        return canvasBackGroundPaint.getColor();
    }

    public static int[] getNumberOfEachColor() {
        return numberOfEachColor;
    }

    public static Object getNodesLock() {
        return nodesLock;
    }
}