package com.netease.audioroom.demo.app;

import android.app.Application;

import com.netease.audioroom.demo.cache.DemoCache;
import com.netease.audioroom.demo.http.NimHttpClient;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.util.NIMUtil;

public class NimApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        LoginInfo loginInfo = new LoginInfo("wen01", "e10adc3949ba59abbe56e057f20f883e");
        NIMClient.init(this, loginInfo, null);
        DemoCache.setContext(this);

    }


}
