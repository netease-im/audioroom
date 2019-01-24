package com.netease.audioroom.demo.helper;

import com.netease.audioroom.demo.model.DemoRoomInfo;
import com.netease.audioroom.demo.model.QueueInfo;
import com.netease.audioroom.demo.util.ToastHelper;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;

import java.util.HashMap;

/**
 * 这个类只是为了提出一些方法
 */

public class AudioHelper {

    //判断当前麦位是否有人
    public boolean hasOccupancy(HashMap<String, QueueInfo> queueMap, QueueInfo queueInfo) {
        QueueInfo oldQueue = queueMap.get(queueInfo.getKey());
        if (oldQueue != null && oldQueue.getStatus() != QueueInfo.INIT_STATUS) {
            return false;
        } else {
            return false;
        }

    }

    //更新麦的状态
    public void updateQueueStatus(ChatRoomService chatRoomService, DemoRoomInfo roomInfo, QueueInfo queueInfo) {
        chatRoomService.updateQueue(roomInfo.getRoomId(), queueInfo.getKey(), queueInfo.toString()).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ToastHelper.showToast("成功通过连麦请求");
            }

            @Override
            public void onFailed(int i) {
                ToastHelper.showToast("通过连麦请求失败 ， code = " + i);
            }

            @Override
            public void onException(Throwable throwable) {
                ToastHelper.showToast("通过连麦请求异常 ， e = " + throwable);
            }
        });
    }
}
