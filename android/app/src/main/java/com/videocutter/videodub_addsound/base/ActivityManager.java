package com.videocutter.videodub_addsound.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class ActivityManager {
    public static File file_videopath;
    public static String outputfile;
    public static String outputfile2;

    public ActivityManager(Context context){
        file_videopath = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"");
        file_videopath.mkdirs();
        StringBuilder sb = new StringBuilder();
        sb.append(file_videopath);
        sb.append("/output.mp4");
        outputfile = sb.toString();
        StringBuilder sb2 = new StringBuilder();
        sb2.append(file_videopath);
        sb2.append("/output2.mp4");
        outputfile2 = sb2.toString();
        StringBuilder sb3 = new StringBuilder();
        sb3.append(file_videopath);
        sb3.append("/output-filtered.mp4");
    }


    public static void redirectToActivityWithBundle(Context activity, Class<? extends AppCompatActivity> redirectToActivity, Bundle bundle) {
        Intent redirectTo = new Intent(activity, redirectToActivity);
        redirectTo.putExtras(bundle);
        activity.startActivity(redirectTo);
    }

    public static void redirectActivityForResult(Activity activity, Class<? extends AppCompatActivity> redirectToActivity, Bundle bundle) {
        Intent redirectTo = new Intent(activity, redirectToActivity);
        redirectTo.putExtras(bundle);
        activity.startActivityForResult(redirectTo,100);
    }


    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
