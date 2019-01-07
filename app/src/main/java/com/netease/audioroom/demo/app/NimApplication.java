package com.netease.audioroom.demo.app;

import android.app.Application;

import com.netease.audioroom.demo.cache.DemoCache;
import com.netease.audioroom.demo.http.NimHttpClient;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.util.NIMUtil;

public class NimApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        NIMClient.init(this, null, null);
        DemoCache.setContext(this);

    }


}
