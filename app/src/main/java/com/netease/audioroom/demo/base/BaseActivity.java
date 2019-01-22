package com.netease.audioroom.demo.base;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.netease.audioroom.demo.permission.MPermission;
import com.netease.audioroom.demo.util.Network;
import com.netease.audioroom.demo.util.NetworkChange;
import com.netease.audioroom.demo.util.NetworkWatcher;
import com.netease.audioroom.demo.widget.unitepage.loadsir.core.LoadService;
import com.netease.audioroom.demo.widget.unitepage.loadsir.core.LoadSir;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;

import java.util.Observable;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "AudioRoom";
    protected boolean isPaused = true;
    protected Context mContext;
    protected LoadService loadService;//通用页面


    protected interface NetworkReconnection {
        void onNetworkReconnection();

        void onNetworkInterrupt();
    }

    NetworkReconnection networkReconnection;

    //网络状态监听
    private NetworkWatcher watcher = new NetworkWatcher() {
        @Override
        public void update(Observable observable, Object data) {
            super.update(observable, data);
            //观察者接受到被观察者的通知，来更新自己的数据操作。
            Network network = (Network) data;
            if (network.isConnected()) {
                networkReconnection.onNetworkReconnection();
            } else {
                networkReconnection.onNetworkInterrupt();
            }
        }

    };


    protected static final int LIVE_PERMISSION_REQUEST_CODE = 1001;
    protected boolean isPermissionGrant = false;

    // 权限控制
    protected static final String[] LIVE_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE};

    protected void requestLivePermission() {
        MPermission.with(this)
                .addRequestCode(LIVE_PERMISSION_REQUEST_CODE)
                .permissions(LIVE_PERMISSIONS)
                .request();
    }


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
        NetworkChange.getInstance().addObserver(watcher);
        mContext = this;
        setContentView(getContentViewID());
        initView();
        loadService = LoadSir.getDefault().register(BaseActivityManager.getInstance().getCurrentActivity());
    }

    protected abstract int getContentViewID();

    protected abstract void initView();

    @Override
    protected void onStart() {
        super.onStart();

    }

    protected void onNetWork() {

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


    @Override
    protected void onDestroy() {
        registerObserver(false);
        super.onDestroy();

    }

    protected void registerObserver(boolean register) {
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(onlineStatusObserver, register);

    }


    protected void onLoginEvent(StatusCode statusCode) {
        Log.i(TAG, "login status  , code = " + statusCode);
    }

    public final boolean isActivityPaused() {
        return isPaused;
    }


    public void setNetworkReconnection(NetworkReconnection networkReconnection) {
        this.networkReconnection = networkReconnection;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
}
