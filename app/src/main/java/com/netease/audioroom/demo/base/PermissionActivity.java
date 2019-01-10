package com.netease.audioroom.demo.base;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;

import com.netease.audioroom.demo.permission.MPermission;
import com.netease.audioroom.demo.permission.annotation.OnMPermissionDenied;
import com.netease.audioroom.demo.permission.annotation.OnMPermissionGranted;
import com.netease.audioroom.demo.permission.annotation.OnMPermissionNeverAskAgain;

public abstract class PermissionActivity extends BaseActivity {

    protected static final int LIVE_PERMISSION_REQUEST_CODE = 1001;

    // 权限控制
    protected static final String[] LIVE_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE};


    protected void requestLivePermission() {
        MPermission.with(this)
                .addRequestCode(LIVE_PERMISSION_REQUEST_CODE)
                .permissions(LIVE_PERMISSIONS)
                .request();
    }


    @OnMPermissionGranted(LIVE_PERMISSION_REQUEST_CODE)
    public void onLivePermissionGrantedInner() {
        onLivePermissionGranted();
    }


    @OnMPermissionDenied(LIVE_PERMISSION_REQUEST_CODE)
    public void onLivePermissionDeniedInner() {
        onLivePermissionDenied();
    }


    @OnMPermissionNeverAskAgain(LIVE_PERMISSION_REQUEST_CODE)
    public void onLivePermissionDeniedAsNeverAskAgainInner() {
        onLivePermissionDeniedAsNeverAskAgain();
    }


    protected abstract void onLivePermissionGranted();

    protected abstract void onLivePermissionDenied();

    protected abstract void onLivePermissionDeniedAsNeverAskAgain();
}
