package com.tinslam.comic.activities;

import android.app.Activity;
import android.content.Intent;

public class ActivityManager{
    public static void changeActivity(Activity currentActivity, Class targetActivity){
        Intent intent = new Intent(currentActivity, targetActivity);
        currentActivity.startActivity(intent);
    }

    public static void switchToActivity(Activity currentActivity, Class targetActivity){
        Intent openMainActivity= new Intent(currentActivity, targetActivity);
        openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        currentActivity.startActivityIfNeeded(openMainActivity, 0);
    }
}
