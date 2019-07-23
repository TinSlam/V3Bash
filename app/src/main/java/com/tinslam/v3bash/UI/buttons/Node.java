package com.tinslam.comic.UI.buttons;

public class Node{
    private byte x, y;
    private byte colorCode, brushStyle;

    public Node(byte x, byte y, byte colorCode, byte brushStyle){
        this.x = x;
        this.y = y;
        this.colorCode = colorCode;
        this.brushStyle = brushStyle;
    }

    public byte getX() {
        return x;
    }

    public void setX(byte x) {
        this.x = x;
    }

    public byte getY() {
        return y;
    }

    public void setY(byte y) {
        this.y = y;
    }

    public byte getColorCode() {
        return colorCode;
    }

    public void setColorCode(byte colorCode) {
        this.colorCode = colorCode;
    }

    public byte getBrushStyle() {
        return brushStyle;
    }

    public void setBrushStyle(byte brushStyle) {
        this.brushStyle = brushStyle;
    }
}
