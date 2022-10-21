package com.videocutter.videodub_addsound.base;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import com.videocutter.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;


public abstract class BaseActivity extends AppCompatActivity implements MvpView {
    ProgressDialog progressDialog;
    private CompositeDisposable mDisposables = new CompositeDisposable();


    public String[] permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO};

    public int permsRequestCode = 1;


    @TargetApi(Build.VERSION_CODES.M)
    public boolean hasPermission(String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public Activity activity() {
        return this;
    }

    public abstract int getLayoutId();

    public abstract void initView();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

        initView();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(false);
    }


    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissionsSafely(String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void showLoading() {
        progressDialog.show();
    }

    @Override
    public void hideLoading() {
        progressDialog.dismiss();
    }

    public void switchFragment(Fragment fragment, int containerId) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(containerId, fragment).addToBackStack(fragment.getClass().getName()).commit();
    }

    @Override
    public void onError(Throwable e) {
        hideLoading();

    }


    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }




    public static String getDisplayableTime(long value) {

        long difference = 0;
        Long mDate = System.currentTimeMillis();

        if (mDate > value) {
            difference = mDate - value;
            final long seconds = difference / 1000;
            final long minutes = seconds / 60;
            final long hours = minutes / 60;
            final long days = hours / 24;
            final long months = days / 31;
            final long years = days / 365;

            if (seconds < 86400) {
                SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                return formatter.format(new Date(value));
                //return "not yet";
            } else if (seconds < 172800) // 48 * 60 * 60
            {
                return "yesterday";
            } else if (seconds < 2592000) // 30 * 24 * 60 * 60
            {
                return days + " days ago";
            } else if (seconds < 31104000) // 12 * 30 * 24 * 60 * 60
            {

                return months <= 1 ? "one month ago" : days + " months ago";
            } else {

                return years <= 1 ? "one year ago" : years + " years ago";
            }
        }
        return null;
    }



    public void subscribe(Disposable disposable) {
        mDisposables.add(disposable);
    }

    public void unsubscribe() {
        if (mDisposables != null && !mDisposables.isDisposed()) {
            mDisposables.dispose();
            mDisposables.clear();
        }
    }


}