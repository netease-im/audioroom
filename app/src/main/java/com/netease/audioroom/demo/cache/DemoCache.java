package com.netease.audioroom.demo.cache;

import android.content.Context;
import android.content.SharedPreferences;

import com.netease.audioroom.demo.model.AccountInfo;
import com.netease.audioroom.demo.model.DemoRoomInfo;


public class DemoCache {
    private static final String ACCOUNT_INFO_KEY = "account_info_key";
    private static final String ROOM_INFO_KEY = "room_info_key";

    private static Context context;

    private static String accountId;
    private static AccountInfo accountInfo;


    private static DemoRoomInfo demoRoomInfo;

    public static String getAccountId() {
        return accountId;
    }

    public static void setAccountId(String accid) {
        accountId = accid;
    }

    public static Context getContext() {
        return context;
    }

    public static void init(Context context) {
        DemoCache.context = context.getApplicationContext();
    }


    public static void saveAccountInfo(AccountInfo account) {
        accountInfo = account;
        getSharedPreferences().edit().putString(ACCOUNT_INFO_KEY, accountInfo.toString()).apply();
    }

    public static void saveRoomInfo(DemoRoomInfo roomInfo) {
        demoRoomInfo = roomInfo;
        getSharedPreferences().edit().putString(ROOM_INFO_KEY, demoRoomInfo.toString()).apply();
    }

    public static DemoRoomInfo getDemoRoomInfo() {
        if (demoRoomInfo != null) {
            return demoRoomInfo;
        }

        String jsonStr = getSharedPreferences().getString(ROOM_INFO_KEY, null);
        if (jsonStr == null) {
            return null;
        }
        demoRoomInfo = new DemoRoomInfo(jsonStr);
        return demoRoomInfo;
    }

    public static AccountInfo getAccountInfo() {

        if (accountInfo != null) {
            return accountInfo;
        }

        String jsonStr = getSharedPreferences().getString(ACCOUNT_INFO_KEY, null);
        if (jsonStr == null) {
            return null;
        }
        accountInfo = new AccountInfo(jsonStr);
        return accountInfo;
    }


    private static SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences("audio_demo", Context.MODE_PRIVATE);
    }


}
