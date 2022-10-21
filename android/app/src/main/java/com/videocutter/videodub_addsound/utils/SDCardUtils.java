package com.videocutter.videodub_addsound.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import java.io.File;

public class SDCardUtils {
    public static final String FOLDER_NAME = "videoFilterTemp";
    public static final String FOLDER_APP = (Environment.getExternalStorageDirectory().getPath() + File.separator + FOLDER_NAME);
    public static final String FOLDER_TMP = (FOLDER_APP + File.separator + ".tmp");

    public static boolean isExternalStorageRemovable() {
        if (Build.VERSION.SDK_INT >= 9) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    public static boolean checkSDCard() {
        return Environment.getExternalStorageState().equals("mounted");
    }

    public static File getExternalCacheDir(Context context) {
        if (hasExternalCacheDir()) {
            return context.getExternalCacheDir();
        }
        return new File(Environment.getExternalStorageDirectory().getPath() + ("/Android/data/" + context.getPackageName() + "/cache/"));
    }

    public static boolean hasHttpConnectionBug() {
        return Build.VERSION.SDK_INT < 8;
    }

    public static boolean hasExternalCacheDir() {
        return Build.VERSION.SDK_INT >= 8;
    }

    public static File getDiskCacheDir(Context context, String str) {
        String str2;
        if (Environment.getExternalStorageState().equalsIgnoreCase("mounted")) {
            str2 = getExternalCacheDir(context).getPath();
        } else {
            str2 = context.getCacheDir().getPath();
        }
        return new File(str2 + File.separator + str);
    }

    public static void deleteFilesInFolder(String str) {
        try {
            for (File file : new File(str).listFiles()) {
                if (file.exists()) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            Log.e(SDCardUtils.class.getSimpleName(), "Delete error:" + e.getMessage());
        }
    }
}
