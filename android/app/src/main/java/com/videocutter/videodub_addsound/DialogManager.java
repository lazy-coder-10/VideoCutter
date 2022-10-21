package com.videocutter.videodub_addsound;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.videocutter.R;


public class DialogManager {
    static Dialog mDialog;
    static Dialog mProgressDialog;


    public static Dialog getDialog(Context mContext, int mLayout) {

        Dialog mDialog = new Dialog(mContext, R.style.AppTheme);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN | WindowManager
                        .LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mDialog.setContentView(mLayout);
        mDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT));
        mDialog.getWindow().setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        mDialog.setCancelable(true);
        mDialog.setCanceledOnTouchOutside(true);

        return mDialog;
    }

    private static Dialog getLoadingDialog(Context mContext, int mLay) {

        mDialog = getDialog(mContext, mLay);
        mDialog.setCancelable(true);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.getWindow().setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        return mDialog;
    }


    public static void showProgress(Context context) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        try {
            mProgressDialog = getLoadingDialog(context, R.layout.progress);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setCanceledOnTouchOutside(true);
            mProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    return keyCode == KeyEvent.KEYCODE_BACK;
                }
            });
            mProgressDialog.show();
        } catch (Exception e) {
            Log.e("Corners", e.getMessage());
        }

    }






    public static void hideProgress() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            try {
                mProgressDialog.dismiss();
            } catch (Exception e) {
                Log.e("Corners", e.getMessage());
            }
        }
    }


}


