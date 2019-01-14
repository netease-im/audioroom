package com.netease.audioroom.demo.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.netease.audioroom.demo.model.AccountInfo;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;


public class DemoCache {


    private static final String ACCOUNT_INFO_KEY = "account_info_key";


    private static Context context;

    private static String account;

    private static NimUserInfo userInfo;


    public static void clear() {
        account = null;
        userInfo = null;
    }

    public static String getAccount() {
        return account;
    }

    public static void setAccount(String account) {
        DemoCache.account = account;
    }

    public static Context getContext() {
        return context;
    }

    public static void init(Context context) {
        DemoCache.context = context.getApplicationContext();
    }

    public static NimUserInfo getUserInfo() {
        if (userInfo == null) {
            userInfo = NIMClient.getService(UserService.class).getUserInfo(account);
        }

        return userInfo;
    }


    public static void saveAccountInfo(AccountInfo accountInfo) {
        getSharedPreferences().edit().putString(ACCOUNT_INFO_KEY, accountInfo.toString()).apply();
    }


    public static AccountInfo getAccountInfo() {
        String jsonStr = getSharedPreferences().getString(ACCOUNT_INFO_KEY, null);
        if (jsonStr == null) {
            return null;
        }
        return new AccountInfo(jsonStr);
    }


    private static SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences("audio_demo", Context.MODE_PRIVATE);
    }


}
