package com.videocutter.videodub_addsound.base;

import android.app.Activity;

public interface MvpView {
    Activity activity();
    void showLoading();
    void hideLoading();
    void onError(Throwable e);
}
