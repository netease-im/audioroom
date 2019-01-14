package com.netease.audioroom.demo.app;

import android.app.Application;

import com.netease.audioroom.demo.cache.DemoCache;
import com.netease.nimlib.sdk.NIMClient;

public class NimApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        DemoCache.init(this);
        NIMClient.init(this, null, null);
    }


}
