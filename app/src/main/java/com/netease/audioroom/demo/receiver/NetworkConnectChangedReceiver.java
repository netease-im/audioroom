package com.netease.audioroom.demo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.netease.audioroom.demo.util.Network;
import com.netease.audioroom.demo.util.NetworkChange;

public class NetworkConnectChangedReceiver extends BroadcastReceiver {
    Network network = new Network();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                if (activeNetworkInfo.isConnected()) {
                    network.setConnected(true);
                    //通知观察者网络状态已改变
                    NetworkChange.getInstance().notifyDataChange(network);
//                    if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
//                        network.setWifi(true);
//                        //通知观察者网络状态已改变
//                        NetworkChange.getInstance().notifyDataChange(network);
////                        ToastHelper.showToast("当前wifi连接可用");
//                    } else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
//
//                        network.setMobile(true);
//                        //通知观察者网络状态已改变
//                        NetworkChange.getInstance().notifyDataChange(network);
////                        ToastHelper.showToast("当前移动网络连接可用");
//                    }
                } else {
                    network.setConnected(false);
                    //通知观察者网络状态已改变
                    NetworkChange.getInstance().notifyDataChange(network);
//                    ToastHelper.showToast("当前没有网络连接，请确保你已经打开网络");
                }
            } else {
                network.setWifi(false);
                network.setMobile(false);
                network.setConnected(false);
                //通知观察者网络状态已改变
                NetworkChange.getInstance().notifyDataChange(network);
//                ToastHelper.showToast("当前没有网络连接，请确保你已经打开网络");
            }
        }


    }
}
