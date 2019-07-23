package com.tinslam.comic.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import com.tinslam.comic.R;
import com.tinslam.comic.modes.painting.PaintingPainting;
import com.tinslam.comic.base.Game;

import java.util.ArrayList;

public class Utils{
    public static int sumOfArray(int[] array){
        int sum = 0;
        for(int i = 0; i < array.length; i++){
            sum += array[i];
        }
        return sum;
    }

    public static String getString(int x, int y){
        String s1, s2;
        if(x < 10){
            s1 = "00" + String.valueOf(x);
        }else if(x < 100){
            s1 = "0" + String.valueOf(x);
        }else{
            s1 = String.valueOf(x);
        }

        if(y < 10){
            s2 = "00" + String.valueOf(y);
        }else if(y < 100){
            s2 = "0" + String.valueOf(y);
        }else{
            s2 = String.valueOf(y);
        }

        return s1 + s2;
    }

    public static float distance(int x1, int y1, int x2, int y2){
        return (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public static float distance(float x1, float y1, float x2, float y2){
        return (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public static int getColor(byte colorCode){
        switch(colorCode){
            case Consts.COLOR_BLACK :
                return Color.BLACK;

            case Consts.COLOR_WHITE :
                return Color.WHITE;

            case Consts.COLOR_RED :
                return Color.RED;

            case Consts.COLOR_GREEN :
                return Color.GREEN;

            case Consts.COLOR_BLUE :
                return Color.BLUE;

            case Consts.COLOR_YELLOW :
                return Color.YELLOW;

            default :
                return Color.BLACK;
        }
    }

    public static void addListMembersToNewList(ArrayList<PaintingPainting> paintings, ArrayList<PaintingPainting> allPaintings) {
        for(int i = 0; i < paintings.size(); i++){
            allPaintings.add(paintings.get(i));
        }
    }

    public static int getIndexOfHighestInArray(int[] colorNumbers) {
        int index = 0;
        int num = 0;
        for(int i = 0; i < colorNumbers.length; i++){
            if(colorNumbers[i] > num){
                num = colorNumbers[i];
                index = i;
            }
        }
        return index;
    }

    public static float heightPercentage(float percentage) {
        return Game.getScreenHeight() * percentage / 100;
    }

    public static float widthPercentage(float percentage) {
        return Game.getScreenWidth() * percentage / 100;
    }

    public static boolean buttonHovered(View v, int mx, int my) {
        Rect rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        return rect.contains(mx, my);
    }

    public static int getScreenWidthPercentage(int width) {
        return (int) ((float) width / Game.getScreenWidth() * 100);
    }

    public static int getScreenHeightPercentage(int height) {
        return (int) ((float) height / Game.getScreenHeight() * 100);
    }

    public static boolean rectCollidesCircle(Rect rect, float cx, float cy, float radius) {
        if(rect.contains((int) cx, (int) cy)) return true;
        for(int i = rect.left; i <= rect.right; i += 4 * Game.density()){
            if(Utils.isInCircle(i, rect.top, cx, cy, radius)) return true;
            if(Utils.isInCircle(i, rect.bottom, cx, cy, radius)) return true;
        }
        for(int i = rect.top; i <= rect.bottom; i += 4 * Game.density()){
            if(Utils.isInCircle(rect.left, i, cx, cy, radius)) return true;
            if(Utils.isInCircle(rect.right, i, cx, cy, radius)) return true;
        }
        return false;
    }

    public static boolean rectCollidesCircle(Rect rect, Circle circle) {
        return rectCollidesCircle(rect, circle.getX(), circle.getY(), circle.getRadius());
    }

    public static boolean isInCircle(float i, float j, float cx, float cy, float radius){
        return distance(i, j, cx, cy) <= radius;
    }

    public static String getModeNameFromByte(int i) {
        switch(i){
            case Consts.MODE_PAINTING :
                return Game.Context().getString(R.string.mode_painting);

            case Consts.MODE_MAZE :
                return Game.Context().getString(R.string.mode_maze);

            case Consts.MODE_HIDE_AND_SEEK :
                return Game.Context().getString(R.string.mode_hide_and_seek);
        }

        return "Mode not found";
    }

    public static void quickSortRanks(int[] arr, String[] arr2, int low, int high) {
        if (arr == null || arr.length == 0)
            return;

        if (low >= high)
            return;

        // pick the pivot
        int middle = low + (high - low) / 2;
        int pivot = arr[middle];

        // make left < pivot and right > pivot
        int i = low, j = high;
        while (i <= j) {
            while (arr[i] < pivot) {
                i++;
            }

            while (arr[j] > pivot) {
                j--;
            }

            if (i <= j) {
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
                String temp2 = arr2[i];
                arr2[i] = arr2[j];
                arr2[j] = temp2;
                i++;
                j--;
            }
        }

        // recursively sort two sub parts
        if (low < j)
            quickSortRanks(arr, arr2, low, j);

        if (high > i)
            quickSortRanks(arr, arr2, i, high);
    }

    public static void quickSort(int[] arr, int low, int high) {
        if (arr == null || arr.length == 0)
            return;

        if (low >= high)
            return;

        // pick the pivot
        int middle = low + (high - low) / 2;
        int pivot = arr[middle];

        // make left < pivot and right > pivot
        int i = low, j = high;
        while (i <= j) {
            while (arr[i] < pivot) {
                i++;
            }

            while (arr[j] > pivot) {
                j--;
            }

            if (i <= j) {
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
                i++;
                j--;
            }
        }

        // recursively sort two sub parts
        if (low < j)
            quickSort(arr, low, j);

        if (high > i)
            quickSort(arr, i, high);
    }

    public static void drawTextDynamicSize(String string, float x, float y, float maxWidth, Paint paint, Canvas canvas){
        float paintSize = paint.getTextSize();
        Rect bounds = new Rect();
        paint.getTextBounds(string, 0, string.length(), bounds);
        while(bounds.width() > maxWidth){
            float size = paint.getTextSize() - 1;
            if(size <= 5 * Game.density()){
                paint.setTextSize(paintSize);
                string = "Won't fit";
                paint.getTextBounds(string, 0, string.length(), bounds);
                break;
            }
            paint.setTextSize(size);
            paint.getTextBounds(string, 0, string.length(), bounds);
        }
        canvas.drawText(string, x, y + bounds.height() / 2, paint);
        paint.setTextSize(paintSize);
    }
}
