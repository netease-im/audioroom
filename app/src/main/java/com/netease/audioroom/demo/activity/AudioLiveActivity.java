package com.netease.audioroom.demo.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.netease.audioroom.demo.R;
import com.netease.audioroom.demo.base.BaseAudioActivity;
import com.netease.audioroom.demo.base.IAudioLive;
import com.netease.audioroom.demo.cache.DemoCache;
import com.netease.audioroom.demo.cache.RoomMemberCache;
import com.netease.audioroom.demo.custom.CloseRoomAttach;
import com.netease.audioroom.demo.custom.P2PNotificationHelper;
import com.netease.audioroom.demo.dialog.RequestLinkDialog;
import com.netease.audioroom.demo.http.ChatRoomHttpClient;
import com.netease.audioroom.demo.model.AccountInfo;
import com.netease.audioroom.demo.model.DemoRoomInfo;
import com.netease.audioroom.demo.model.QueueInfo;
import com.netease.audioroom.demo.model.QueueMember;
import com.netease.audioroom.demo.model.RequestMember;
import com.netease.audioroom.demo.permission.MPermission;
import com.netease.audioroom.demo.permission.MPermissionUtil;
import com.netease.audioroom.demo.permission.annotation.OnMPermissionDenied;
import com.netease.audioroom.demo.permission.annotation.OnMPermissionGranted;
import com.netease.audioroom.demo.permission.annotation.OnMPermissionNeverAskAgain;
import com.netease.audioroom.demo.util.JsonUtil;
import com.netease.audioroom.demo.util.ToastHelper;
import com.netease.audioroom.demo.widget.SemicircleView;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.chatroom.ChatRoomMessageBuilder;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomQueueChangeAttachment;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomRoomMemberInAttachment;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomTempMuteAddAttachment;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomTempMuteRemoveAttachment;
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomResultData;
import com.netease.nimlib.sdk.msg.constant.ChatRoomQueueChangeType;
import com.netease.nimlib.sdk.msg.model.CustomNotification;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.netease.audioroom.demo.dialog.RequestLinkDialog.QUEUEINFOLIST;

/**
 * 主播页
 */
public class AudioLiveActivity extends BaseAudioActivity implements IAudioLive, View.OnClickListener {

    public static void start(Context context, DemoRoomInfo demoRoomInfo) {
        Intent intent = new Intent(context, AudioLiveActivity.class);
        intent.putExtra(BaseAudioActivity.ROOM_INFO_KEY, demoRoomInfo);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_from_left);
        }
    }

    //聊天室队列元素
    private HashMap<String, QueueInfo> queueMap = new HashMap<>();

    SemicircleView semicircleView;

    ArrayList<RequestMember> requestMemberList;//申请麦位列表

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestLivePermission();
        enterChatRoom(roomInfo.getRoomId());
        enableAudienceRole(false);
        joinChannel(audioUid);
        Log.e(BaseAudioActivity.TAG, "主播页");

    }

    @Override
    protected void initView() {
        semicircleView = findViewById(R.id.semicircleView);
        requestMemberList = new ArrayList<>();
//        semicircleView.setVisibility(View.GONE);
        semicircleView.setClickable(true);
    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_live;
    }

    @Override
    protected void setupBaseView() {
        ivCancelLink.setVisibility(View.GONE);
        ivMuteOtherText.setOnClickListener(this);
        ivAudioQuality.setOnClickListener(this);
        ivSelfAudioSwitch.setOnClickListener(this);
        ivRoomAudioSwitch.setOnClickListener(this);
        ivExistRoom.setOnClickListener(this);
        semicircleView.setOnClickListener(this);
    }


    @Override
    protected void onQueueItemClick(QueueInfo model, int position) {

    }

    @Override
    protected boolean onQueueItemLongClick(QueueInfo model, int position) {
        return false;
    }

    @Override
    protected void receiveNotification(CustomNotification customNotification) {
        String content = customNotification.getContent();
        if (TextUtils.isEmpty(content)) {
            return;
        }
        JSONObject jsonObject = JsonUtil.parse(content);
        if (jsonObject == null) {
            return;
        }
        int command = jsonObject.optInt(P2PNotificationHelper.COMMAND, 0);

        //有人请求连麦
        if (command == P2PNotificationHelper.REQUEST_LINK) {
            int index = jsonObject.optInt(P2PNotificationHelper.INDEX);
            String nick = jsonObject.optString(P2PNotificationHelper.NICK);
            String avatar = jsonObject.optString(P2PNotificationHelper.AVATAR);
            QueueMember queueMember = new QueueMember(customNotification.getFromAccount(), nick, avatar, false);
            ToastHelper.showToast("有人请求连麦");
            linkRequest(queueMember, index);
            return;
        }


        //有人请求下麦
        if (command == P2PNotificationHelper.CANCEL_LINK) {
            int index = jsonObject.optInt(P2PNotificationHelper.INDEX, -1);
            QueueInfo queueInfo = queueMap.get(QueueInfo.getKeyByIndex(index));
            if (queueInfo != null) {
                queueInfo.setStatus(QueueInfo.INIT_STATUS);
                chatRoomService.updateQueue(roomInfo.getRoomId(), queueInfo.getKey(), queueInfo.toString());
                ToastHelper.showToast("有人请求下麦");
            }

            return;
        }


        //有人取消连麦请求
        if (command == P2PNotificationHelper.CANCEL_REQUEST_LINK) {

            //谁取消了连麦请求
            String fromAccount = customNotification.getFromAccount();
            //todo
        }


    }

    @Override
    protected void exitRoom() {
        release();
        if (roomInfo != null) {
            ChatRoomMessage closeRoomMessage = ChatRoomMessageBuilder.createChatRoomCustomMessage(roomInfo.getRoomId(), new CloseRoomAttach());
            chatRoomService.sendMessage(closeRoomMessage, false).setCallback(new RequestCallbackWrapper<Void>() {
                @Override
                public void onResult(int i, Void aVoid, Throwable throwable) {
                    ChatRoomHttpClient.getInstance().closeRoom(DemoCache.getAccountId(), roomInfo.getRoomId(), null);
                }
            });
            RoomMemberCache.getInstance().removeCache(roomInfo.getRoomId());
            roomInfo = null;
        }
        finish();
    }

    @Override
    protected void onQueueChange(ChatRoomQueueChangeAttachment queueChange) {
        super.onQueueChange(queueChange);

        ChatRoomQueueChangeType changeType = queueChange.getChatRoomQueueChangeType();
        // 队列被清空
        if (changeType == ChatRoomQueueChangeType.DROP) {
            queueMap.clear();
            return;
        }
        String value = queueChange.getContent();
        //新增元素或更新
        if (changeType == ChatRoomQueueChangeType.OFFER && !TextUtils.isEmpty(value)) {
            QueueInfo queueInfo = new QueueInfo(value);
            if (queueInfo.getIndex() != -1) {
                queueMap.put(queueInfo.getKey(), queueInfo);
            }
            return;
        }
    }


    @Override
    protected void enterRoomSuccess(EnterChatRoomResultData resultData) {
//        super.enterRoomSuccess(resultData);
        // 主播进房间先清除一下原来的队列元素
        loadService.showSuccess();
        chatRoomService.dropQueue(roomInfo.getRoomId());
        AccountInfo accountInfo = DemoCache.getAccountInfo();
        ivLiverAvatar.loadAvatar(accountInfo.avatar);
        tvLiverNick.setText(accountInfo.nick);
        initQueue(null);
        RoomMemberCache.getInstance().fetchMembers(roomInfo.getRoomId(), 0, 100, null);
    }

    @Override
    public void linkRequest(QueueMember queueMember, int index) {
        //todo UI呈现
        requestMemberList.add(new RequestMember(queueMember, index));
        if (requestMemberList.size() > 0) {
            semicircleView.setVisibility(View.VISIBLE);
//            semicircleView.setText(requestMemberList.size() + "");
        } else {
            semicircleView.setVisibility(View.GONE);

        }

    }

    @Override
    public void linkRequestCancel() {

    }

    @Override
    public void rejectLink(QueueMember queueMember) {

        P2PNotificationHelper.rejectLink(DemoCache.getAccountId(), queueMember.getAccount(), new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ToastHelper.showToast("拒绝连麦成功");
            }

            @Override
            public void onFailed(int i) {

            }

            @Override
            public void onException(Throwable throwable) {

            }
        });


        //todo

    }

    @Override
    public void acceptLink(QueueInfo queueInfo) {

        QueueInfo oldQueue = queueMap.get(queueInfo.getKey());
        //当前麦位有人了
        if (oldQueue != null && oldQueue.getStatus() != QueueInfo.INIT_STATUS) {
            rejectLink(queueInfo.getQueueMember());
            return;
        }
        queueInfo.setStatus(QueueInfo.NORMAL_STATUS);
        //TODO 弹框

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


    @OnMPermissionGranted(LIVE_PERMISSION_REQUEST_CODE)
    protected void onLivePermissionGranted() {
        isPermissionGrant = true;
        ToastHelper.showToast("授权成功");
    }

    @OnMPermissionDenied(LIVE_PERMISSION_REQUEST_CODE)
    protected void onLivePermissionDenied() {
        List<String> deniedPermissions = MPermission.getDeniedPermissions(this, LIVE_PERMISSIONS);
        String tip = "您拒绝了权限" + MPermissionUtil.toString(deniedPermissions) + "，无法开启直播";
        ToastHelper.showToast(tip);
    }

    @OnMPermissionNeverAskAgain(LIVE_PERMISSION_REQUEST_CODE)
    protected void onLivePermissionDeniedAsNeverAskAgain() {
        List<String> deniedPermissions = MPermission.getDeniedPermissionsWithoutNeverAskAgain(this, LIVE_PERMISSIONS);
        List<String> neverAskAgainPermission = MPermission.getNeverAskAgainPermissions(this, LIVE_PERMISSIONS);
        StringBuilder sb = new StringBuilder();
        sb.append("无法开启直播，请到系统设置页面开启权限");
        sb.append(MPermissionUtil.toString(neverAskAgainPermission));
        if (deniedPermissions != null && !deniedPermissions.isEmpty()) {
            sb.append(",下次询问请授予权限");
            sb.append(MPermissionUtil.toString(deniedPermissions));
        }
        ToastHelper.showToastLong(sb.toString());
    }

    @Override
    public void onClick(View view) {
        if (view == ivMuteOtherText) {
        } else if (view == ivAudioQuality) {

        } else if (view == ivSelfAudioSwitch) {
            boolean mutex = ivSelfAudioSwitch.isSelected();
            ivSelfAudioSwitch.setSelected(!mutex);
            muteSelfAudio(!mutex);

        } else if (view == ivRoomAudioSwitch) {
            boolean close = ivRoomAudioSwitch.isSelected();
            ivRoomAudioSwitch.setSelected(!close);
            muteRoomAudio(!close);
        } else if (view == ivExistRoom) {
            release();
            ChatRoomMessage closeRoomMessage = ChatRoomMessageBuilder.createChatRoomCustomMessage(roomInfo.getRoomId(), new CloseRoomAttach());
            chatRoomService.sendMessage(closeRoomMessage, false).setCallback(new RequestCallbackWrapper<Void>() {
                @Override
                public void onResult(int i, Void aVoid, Throwable throwable) {
                    ChatRoomHttpClient.getInstance().closeRoom(DemoCache.getAccountId(), roomInfo.getRoomId(), null);
                }
            });
            finish();
        } else if (view == semicircleView) {
            //TODO 弹出请求上麦列表
            ToastHelper.showToast("点我了");
            RequestLinkDialog requestLinkDialog = new RequestLinkDialog();
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(QUEUEINFOLIST, requestMemberList);
            requestLinkDialog.setArguments(bundle);
            requestLinkDialog.show(getFragmentManager(), "RequestLinkDialog");

        }

    }

    protected void memberMuteRemove(ChatRoomTempMuteRemoveAttachment muteRemove) {
        super.memberMuteRemove(muteRemove);
        RoomMemberCache.getInstance().muteChange(roomInfo.getRoomId(), muteRemove.getTargets(), false);

    }

    protected void memberMuteAdd(ChatRoomTempMuteAddAttachment addMuteMember) {
        super.memberMuteAdd(addMuteMember);
        RoomMemberCache.getInstance().muteChange(roomInfo.getRoomId(), addMuteMember.getTargets(), true);
    }

    protected void memberExit(ChatRoomQueueChangeAttachment memberExit) {
        super.memberExit(memberExit);
        RoomMemberCache.getInstance().removeMember(roomInfo.getRoomId(), memberExit.getOperator());
    }

    protected void memberIn(ChatRoomRoomMemberInAttachment memberIn) {
        super.memberIn(memberIn);
        if (TextUtils.equals(memberIn.getOperator(), DemoCache.getAccountId())) {
            return;
        }
        RoomMemberCache.getInstance().fetchMember(roomInfo.getRoomId(), memberIn.getOperator(), null);
    }

    @Override
    public void invitedLink() {

    }

    @Override
    public void removeLink() {

    }

    @Override
    public void linkCanceled() {

    }

    @Override
    public void mutedText() {

    }

    @Override
    public void muteTextAll() {

    }


    @Override
    public void mutedAudio() {

    }
}
