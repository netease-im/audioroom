package com.netease.audioroom.demo.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "AudioRoom";
    protected boolean isPaused = true;

    //监听登录状态
    private Observer<StatusCode> onlineStatusObserver = new Observer<StatusCode>() {
        @Override
        public void onEvent(StatusCode statusCode) {
            onLoginEvent(statusCode);
        }
    };




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerObserver(true);
    }

    @Override
    protected void onDestroy() {
        registerObserver(false);
        super.onDestroy();

    }

    protected void registerObserver(boolean register) {

        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(onlineStatusObserver, register);

    }

    @Override
    protected void onResume() {
        super.onResume();
        isPaused = false;
    }

    @Override
    protected void onPause() {
        isPaused = true;
        super.onPause();
    }

    protected void onLoginEvent(StatusCode statusCode) {

        Log.i(TAG, "login status  , code = " + statusCode);
    }

    public final boolean isActivityPaused() {
        return isPaused;
    }
}
