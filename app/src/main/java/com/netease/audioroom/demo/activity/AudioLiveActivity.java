package com.netease.audioroom.demo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.audioroom.demo.R;
import com.netease.audioroom.demo.base.BaseAudioActivity;
import com.netease.audioroom.demo.base.LoginManager;
import com.netease.audioroom.demo.base.action.INetworkReconnection;
import com.netease.audioroom.demo.cache.DemoCache;
import com.netease.audioroom.demo.cache.RoomMemberCache;
import com.netease.audioroom.demo.custom.P2PNotificationHelper;
import com.netease.audioroom.demo.dialog.BottomMenuDialog;
import com.netease.audioroom.demo.dialog.RequestLinkDialog;
import com.netease.audioroom.demo.dialog.TopTipsDialog;
import com.netease.audioroom.demo.http.ChatRoomHttpClient;
import com.netease.audioroom.demo.model.AccountInfo;
import com.netease.audioroom.demo.model.AudioMixingInfo;
import com.netease.audioroom.demo.model.DemoRoomInfo;
import com.netease.audioroom.demo.model.QueueInfo;
import com.netease.audioroom.demo.model.QueueMember;
import com.netease.audioroom.demo.permission.MPermission;
import com.netease.audioroom.demo.permission.MPermissionUtil;
import com.netease.audioroom.demo.permission.annotation.OnMPermissionDenied;
import com.netease.audioroom.demo.permission.annotation.OnMPermissionGranted;
import com.netease.audioroom.demo.permission.annotation.OnMPermissionNeverAskAgain;
import com.netease.audioroom.demo.util.CommonUtil;
import com.netease.audioroom.demo.util.JsonUtil;
import com.netease.audioroom.demo.util.ToastHelper;
import com.netease.audioroom.demo.widget.unitepage.loadsir.callback.ErrorCallback;
import com.netease.audioroom.demo.widget.unitepage.loadsir.callback.LoadingCallback;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.model.AVChatChannelInfo;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomQueueChangeAttachment;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomRoomMemberInAttachment;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomTempMuteAddAttachment;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomTempMuteRemoveAttachment;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomUpdateInfo;
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomResultData;
import com.netease.nimlib.sdk.msg.constant.ChatRoomQueueChangeType;
import com.netease.nimlib.sdk.msg.model.CustomNotification;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.netease.audioroom.demo.dialog.BottomMenuDialog.BOTTOMMENUS;

/**
 * 主播页
 */
public class AudioLiveActivity extends BaseAudioActivity implements LoginManager.IAudioLive, View.OnClickListener {
    //    boolean isMicrophone;
    BottomMenuDialog bottomMenuDialog;

    public static void start(Context context, DemoRoomInfo demoRoomInfo) {
        Intent intent = new Intent(context, AudioLiveActivity.class);
        intent.putExtra(BaseAudioActivity.ROOM_INFO_KEY, demoRoomInfo);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_from_left);
        }
    }

    private String[] musicPathArray;
    private int currentPlayIndex;
    private int inviteIndex = -1;//抱麦位置
    TopTipsDialog topTipsDialog;

    //聊天室队列元素
    private HashMap<String, QueueInfo> queueMap = new HashMap<>();

    TextView semicircleView;

    ArrayList<QueueInfo> requestMemberList;//申请麦位列表


    private TextView tvMusicPlayHint;
    private ImageView ivPauseOrPlay;
    private ImageView ivNext;
    private RequestLinkDialog requestLinkDialog;
    protected AudioMixingInfo mixingInfo;

    @Override
    protected int getContentViewID() {
        return R.layout.activity_live;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enterChatRoom(roomInfo.getRoomId());
        createRoom(roomInfo.getRoomId());
        checkFile();
    }

    private void createRoom(String roomId) {
        AVChatManager.getInstance().createRoom(roomId, "", new AVChatCallback<AVChatChannelInfo>() {
            @Override
            public void onSuccess(AVChatChannelInfo avChatChannelInfo) {
                ToastHelper.showToast("创建房间成功");
                //加入房间
                joinChannel();
            }

            @Override
            public void onFailed(int code) {
                ToastHelper.showToast("创建房间失败" + code);
            }

            @Override
            public void onException(Throwable exception) {
                ToastHelper.showToast("创建房间失败" + exception.getMessage());
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
//        initRoomInfo();
        setNetworkReconnection(new INetworkReconnection() {
            @Override
            public void onNetworkReconnection() {
                LoginManager loginManager = LoginManager.getInstance();
                loginManager.tryLogin();
                loginManager.setCallback(new LoginManager.Callback() {
                    @Override
                    public void onSuccess(AccountInfo accountInfo) {
                        enterChatRoom(roomInfo.getRoomId());
                    }

                    @Override
                    public void onFailed(int code, String errorMsg) {
                        loadService.showCallback(ErrorCallback.class);
                    }
                });

            }

            @Override
            public void onNetworkInterrupt() {
                Bundle bundle = new Bundle();
                TopTipsDialog.Style style = topTipsDialog.new Style(
                        "网络断开",
                        0,
                        0,
                        0);
                bundle.putParcelable(topTipsDialog.TAG, style);
                topTipsDialog.setArguments(bundle);
                topTipsDialog.show(getSupportFragmentManager(), topTipsDialog.TAG);
                topTipsDialog.setClickListener(() -> {
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //抱麦
            ChatRoomMember chatRoomMember = data.getParcelableExtra(MemberActivity.MEMBERACTIVITY);
            QueueMember queueMember = new QueueMember(chatRoomMember.getAccount(), chatRoomMember.getNick(), chatRoomMember.getAvatar());
            invitedLink(new QueueInfo(inviteIndex, queueMember, QueueInfo.STATUS_NORMAL, QueueInfo.Reason.inviteByHost));
            //TODO 去重啊！！！！！！！！！！！！！！！！！！！！！！！
//            boolean inQueue = false;//判断当前用户是不是在麦上
//            for (int i = 0; i < queueAdapter.getItemCount(); i++) {
//                if (queueAdapter.getItem(i) != null) {
//                    if (queueMember.getAccount().equals(queueAdapter.getItem(i).getQueueMember().toString())) {
//                        inQueue = true;
//                        break;
//                    }
//                }
//
//            }
//            if (!inQueue){
//
//            }

        }
    }

    @Override
    protected void setupBaseView() {
        mixingInfo = new AudioMixingInfo();
        topTipsDialog = new TopTipsDialog();
        semicircleView = findViewById(R.id.semicircleView);
        tvMusicPlayHint = findViewById(R.id.tv_music_play_hint);
        ivPauseOrPlay = findViewById(R.id.iv_pause_or_play);
        ivNext = findViewById(R.id.iv_next);
        ivCancelLink.setVisibility(View.GONE);
        ivMuteOtherText.setOnClickListener(this);
        ivSelfAudioSwitch.setOnClickListener(this);
        ivRoomAudioSwitch.setOnClickListener(this);
        ivExistRoom.setOnClickListener(this);
        semicircleView.setOnClickListener(this);
        ivPauseOrPlay.setOnClickListener(this);
        ivNext.setOnClickListener(this);
        ivPauseOrPlay.setOnClickListener(this);
        ivNext.setOnClickListener(this);
        requestMemberList = new ArrayList<>();
        semicircleView.setVisibility(View.GONE);
        semicircleView.setClickable(true);
        updateMusicPlayHint();
    }


    @Override
    protected void onQueueItemClick(QueueInfo queueInfo, int position) {
        Bundle bundle = new Bundle();
        bottomMenuDialog = new BottomMenuDialog();
        ArrayList<String> mune = new ArrayList<>();
        //当前麦位有人了
        switch (queueInfo.getStatus()) {
            case QueueInfo.STATUS_INIT:
                mune.add("将成员抱上麦位");
                mune.add("屏蔽麦位");
                mune.add("关闭麦位");
                mune.add("取消");
                bundle.putStringArrayList(BOTTOMMENUS, mune);
                bottomMenuDialog.setArguments(bundle);
                bottomMenuDialog.setItemClickListener((d, p) -> {
                    switch (d.get(p)) {
                        case "将成员抱上麦位":
                            bottomButtonAction(bottomMenuDialog, queueInfo, "将成员抱上麦位");
                            break;
                        case "屏蔽麦位":
                            bottomButtonAction(bottomMenuDialog, queueInfo, "屏蔽麦位");
                            break;
                        case "关闭麦位":
                            bottomButtonAction(bottomMenuDialog, queueInfo, "关闭麦位");
                            break;
                        case "取消":
                            bottomButtonAction(bottomMenuDialog, queueInfo, "取消");
                            break;
                    }
                });
                break;
            case QueueInfo.STATUS_LOAD:
                ToastHelper.showToast("正在申请");
                break;
            case QueueInfo.STATUS_NORMAL:
                mune.add("将TA踢下麦位");
                mune.add("屏蔽麦位");
                mune.add("取消");
                bundle.putStringArrayList(BOTTOMMENUS, mune);
                bottomMenuDialog.setArguments(bundle);
                bottomMenuDialog.setItemClickListener((d, p) -> {
                    switch (d.get(p)) {
                        case "将TA踢下麦位":
                            bottomButtonAction(bottomMenuDialog, queueInfo, "将TA踢下麦位");
                            break;
                        case "屏蔽麦位":
                            bottomButtonAction(bottomMenuDialog, queueInfo, "屏蔽麦位");
                            break;
                        case "取消":
                            bottomButtonAction(bottomMenuDialog, queueInfo, "取消");
                            break;
                    }
                });
                break;
            case QueueInfo.STATUS_CLOSE:
                mune.add("打开麦位");
                mune.add("取消");
                bundle.putStringArrayList(BOTTOMMENUS, mune);
                bottomMenuDialog.setArguments(bundle);
                bottomMenuDialog.setItemClickListener((d, p) -> {
                    switch (d.get(p)) {
                        case "打开麦位":
                            bottomButtonAction(bottomMenuDialog, queueInfo, "打开麦位");
                            break;
                        case "取消":
                            bottomButtonAction(bottomMenuDialog, queueInfo, "取消");
                            break;
                    }
                });
                break;
            case QueueInfo.STATUS_FORBID:
                mune.add("将成员抱上麦位");
                mune.add("解除语音屏蔽");
                mune.add("取消");
                bundle.putStringArrayList(BOTTOMMENUS, mune);
                bottomMenuDialog.setArguments(bundle);
                bottomMenuDialog.setItemClickListener((d, p) -> {
                    switch (d.get(p)) {
                        case "将成员抱上麦位":
                            bottomButtonAction(bottomMenuDialog, queueInfo, "将成员抱上麦位");
                            break;
                        case "解除语音屏蔽":
                            bottomButtonAction(bottomMenuDialog, queueInfo, "解除语音屏蔽");
                            break;
                        case "取消":
                            bottomButtonAction(bottomMenuDialog, queueInfo, "取消");
                            break;

                    }
                });
                break;
            case QueueInfo.STATUS_BE_MUTED_AUDIO:
                ToastHelper.showToast("麦位上有人，但是语音被屏蔽");
                mune.add("将TA踢下麦位");
                mune.add("解除语音屏蔽");
                mune.add("取消");
                bundle.putStringArrayList(BOTTOMMENUS, mune);
                bottomMenuDialog.setArguments(bundle);
                bottomMenuDialog.setItemClickListener((d, p) -> {
                    switch (d.get(p)) {
                        case "将TA踢下麦位":
                            bottomButtonAction(bottomMenuDialog, queueInfo, "将TA踢下麦位");
                            break;
                        case "解除语音屏蔽":
                            bottomButtonAction(bottomMenuDialog, queueInfo, "解除语音屏蔽");
                            break;
                        case "取消":
                            bottomButtonAction(bottomMenuDialog, queueInfo, "取消");
                            break;

                    }
                });
                break;
            case QueueInfo.STATUS_CLOSE_SELF_AUDIO:
                mune.add("将TA踢下麦位");
                mune.add("屏蔽麦位");
                mune.add("取消");
                bundle.putStringArrayList(BOTTOMMENUS, mune);
                bottomMenuDialog.setArguments(bundle);
                bottomMenuDialog.setItemClickListener((d, p) -> {
                    switch (d.get(p)) {
                        case "将TA踢下麦位":
                            bottomButtonAction(bottomMenuDialog, queueInfo, "将TA踢下麦位");
                            break;
                        case "屏蔽麦位":
                            bottomButtonAction(bottomMenuDialog, queueInfo, "屏蔽麦位");
                        case "取消":
                            bottomButtonAction(bottomMenuDialog, queueInfo, "取消");
                            break;
                    }
                });
                break;
            case QueueInfo.STATUS_CLOSE_SELF_AUDIO_AND_MUTED:
                mune.add("将TA踢下麦位");
                mune.add("解除语音屏蔽");
                mune.add("取消");
                bundle.putStringArrayList(BOTTOMMENUS, mune);
                bottomMenuDialog.setArguments(bundle);
                bottomMenuDialog.setItemClickListener((d, p) -> {
                    switch (d.get(p)) {
                        case "将TA踢下麦位":
                            bottomButtonAction(bottomMenuDialog, queueInfo, "将TA踢下麦位");
                            break;
                        case "解除语音屏蔽":
                            bottomButtonAction(bottomMenuDialog, queueInfo, "解除语音屏蔽");
                            break;
                        case "取消":
                            bottomButtonAction(bottomMenuDialog, queueInfo, "取消");
                            break;
                    }
                });
                break;

        }
        bottomMenuDialog.show(getSupportFragmentManager(), bottomMenuDialog.TAG);
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
        int index;
        QueueInfo queueInfo;
        String nick;
        String avatar;
        QueueMember queueMember;
        switch (command) {
            case P2PNotificationHelper.REQUEST_LINK://请求连麦
                index = jsonObject.optInt(P2PNotificationHelper.INDEX);
                nick = jsonObject.optString(P2PNotificationHelper.NICK);
                avatar = jsonObject.optString(P2PNotificationHelper.AVATAR);
                queueMember = new QueueMember(customNotification.getFromAccount(), nick, avatar);
                queueInfo = queueMap.get(QueueInfo.getKeyByIndex(index));
                if (queueInfo != null) {
                    if (queueInfo.getStatus() == QueueInfo.STATUS_FORBID) {
                        queueInfo.setReason(QueueInfo.Reason.applyInMute);
                    } else {
                        queueInfo.setReason(QueueInfo.Reason.init);
                    }
                    queueInfo.setStatus(QueueInfo.STATUS_LOAD);
                } else {
                    queueInfo = new QueueInfo(index, queueMember, QueueInfo.STATUS_LOAD, QueueInfo.Reason.init);
                }
                chatRoomService.updateQueue(roomInfo.getRoomId(), queueInfo.getKey(), queueInfo.toString());
                linkRequest(queueInfo);
                break;
            case P2PNotificationHelper.CANCEL_REQUEST_LINK://取消请求
                index = jsonObject.optInt(P2PNotificationHelper.INDEX);
                queueInfo = queueMap.get(QueueInfo.getKeyByIndex(index));
                if (queueInfo == null) {
                    queueInfo = new QueueInfo(index, null, QueueInfo.STATUS_INIT, QueueInfo.Reason.init);
                }
                linkRequestCancel(queueInfo);
                break;
            case P2PNotificationHelper.CANCEL_LINK://主动下麦
                index = jsonObject.optInt(P2PNotificationHelper.INDEX, -1);
                queueInfo = queueMap.get(QueueInfo.getKeyByIndex(index));
                if (queueInfo != null) {
                    queueInfo.setStatus(QueueInfo.STATUS_INIT);
                    queueInfo.setReason(QueueInfo.Reason.kickedBySelf);
                    chatRoomService.updateQueue(roomInfo.getRoomId(), queueInfo.getKey(), queueInfo.toString());
                }
                break;
        }

    }

    @Override
    protected void exitRoom() {
        loadService.showCallback(LoadingCallback.class);
        //离开聊天室
        AVChatManager.getInstance().disableRtc();
        AVChatManager.getInstance().leaveRoom2(roomInfo.getRoomId(), new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //关闭应用服务器聊天室
                ChatRoomHttpClient.getInstance().closeRoom(DemoCache.getAccountId(),
                        roomInfo.getRoomId(), new ChatRoomHttpClient.ChatRoomHttpCallback() {
                            @Override
                            public void onSuccess(Object o) {
                                loadService.showSuccess();
                                ToastHelper.showToast("房间已解散");
                                if (roomInfo != null) {
                                    RoomMemberCache.getInstance().removeCache(roomInfo.getRoomId());
                                    roomInfo = null;
                                }
                                finish();
                            }

                            @Override
                            public void onFailed(int code, String errorMsg) {
                                ToastHelper.showToast("房间解散失败" + errorMsg);
                            }
                        });

            }

            @Override
            public void onFailed(int code) {
                ToastHelper.showToast("解散失败code：" + code);
            }

            @Override
            public void onException(Throwable exception) {
                ToastHelper.showToast("解散失败code：" + exception.getMessage());
            }
        });

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
        }
    }

    @Override
    protected void enterRoomSuccess(EnterChatRoomResultData resultData) {
        AccountInfo accountInfo = DemoCache.getAccountInfo();
        ivLiverAvatar.loadAvatar(accountInfo.avatar);
        ivLiverAvatar.startWaveAnimation();
        tvLiverNick.setText(accountInfo.nick);
        initQueue(null);
        RoomMemberCache.getInstance().fetchMembers(roomInfo.getRoomId(), 0, 100, null);
    }

    @Override
    public void linkRequest(QueueInfo queueInfo) {
        requestMemberList.add(queueInfo);
        if (requestMemberList.size() > 0) {
            semicircleView.setVisibility(View.VISIBLE);
            semicircleView.setText(requestMemberList.size() + "");
        } else {
            semicircleView.setVisibility(View.GONE);
        }

    }

    @Override
    public void linkRequestCancel(QueueInfo queueInfo) {
        if (queueInfo.getReason() == QueueInfo.Reason.applyInMute) {
            queueInfo.setStatus(QueueInfo.STATUS_FORBID);
        } else {
            queueInfo.setStatus(QueueInfo.STATUS_INIT);
        }
        queueInfo.setReason(QueueInfo.Reason.cancelApplyBySelf);
        chatRoomService.updateQueue(roomInfo.getRoomId(), queueInfo.getKey(),
                queueInfo.toString()).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                requestMemberList.remove(requestMemberList.get(queueInfo.getIndex()));
                if (requestMemberList.size() == 0) {
                    semicircleView.setVisibility(View.GONE);
                } else {
                    semicircleView.setText(requestMemberList.size());
                }

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


    @Override
    public void rejectLink(QueueInfo queueInfo) {
        queueInfo.setReason(QueueInfo.Reason.cancelApplyByHost);
        queueInfo.setStatus(QueueInfo.STATUS_INIT);
        requestMemberList.remove(queueInfo);
        chatRoomService.updateQueue(roomInfo.getRoomId(), queueInfo.getKey(), queueInfo.toString()).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ToastHelper.showToast("已拒绝" + queueInfo.getQueueMember().getNick() + "的申请");
                if (requestMemberList.size() == 0) {
                    if (requestLinkDialog != null && requestLinkDialog.isVisible()) {
                        requestLinkDialog.dismiss();
                    }
                    semicircleView.setVisibility(View.GONE);
                } else {
                    if (requestLinkDialog != null && requestLinkDialog.isVisible()) {
                        requestLinkDialog.updateDate();
                    }
                }
            }

            @Override
            public void onFailed(int i) {

            }

            @Override
            public void onException(Throwable throwable) {

            }
        });
    }

    @Override
    public void acceptLink(QueueInfo queueInfo) {
        //当前麦位有人了
        if (QueueInfo.hasOccupancy(queueInfo)) {
            rejectLink(queueInfo);
        } else {
            if (queueInfo.getReason() == QueueInfo.Reason.applyInMute) {
                queueInfo.setStatus(QueueInfo.STATUS_FORBID);
            } else {
                queueInfo.setStatus(QueueInfo.STATUS_NORMAL);
            }
            queueInfo.setReason(QueueInfo.Reason.agreeApply);
            chatRoomService.updateQueue(roomInfo.getRoomId(), queueInfo.getKey(),
                    queueInfo.toString()).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    ToastHelper.showToast("成功通过连麦请求");
                    requestMemberList.remove(queueInfo);
                    if (requestMemberList.size() == 0) {
                        requestLinkDialog.dismiss();
                        semicircleView.setVisibility(View.GONE);
                    } else {
                        requestLinkDialog.updateDate();
                    }
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
        switch (view.getId()) {
            case R.id.iv_mute_other_text:
                //禁言
                MuteMemberListActivity.start(mContext, roomInfo);
                break;

            case R.id.iv_close_room_audio_switch:
                boolean close = ivRoomAudioSwitch.isSelected();
                ivRoomAudioSwitch.setSelected(!close);
                muteRoomAudio(!close);
                if (close) {
                    ToastHelper.showToast("已关闭“聊天室声音”");
                } else {
                    ToastHelper.showToast("已打开“聊天室声音”");
                }
                break;
            case R.id.iv_exist_room:
                bottomMenuDialog = new BottomMenuDialog();
                Bundle bundle1 = new Bundle();
                ArrayList<String> mune = new ArrayList<>();
                mune.add("<font color=\"#ff4f4f\">退出并解散房间</color>");
                mune.add("取消");
                bundle1.putStringArrayList(BOTTOMMENUS, mune);
                bottomMenuDialog.setArguments(bundle1);
                bottomMenuDialog.show(getSupportFragmentManager(), bottomMenuDialog.TAG);
                bottomMenuDialog.setItemClickListener((d, p) -> {
                    switch (d.get(p)) {
                        case "<font color=\"#ff4f4f\">退出并解散房间</color>":
                            bottomButtonAction(bottomMenuDialog, null, "退出并解散房间");
                            break;
                        case "取消":
                            bottomButtonAction(bottomMenuDialog, null, "取消");
                            break;
                    }


                });
                break;
            case R.id.semicircleView:
                requestLinkDialog = new RequestLinkDialog();
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(requestLinkDialog.TAG, requestMemberList);
                requestLinkDialog.setArguments(bundle);
                requestLinkDialog.show(getSupportFragmentManager(), requestLinkDialog.TAG);
                requestLinkDialog.setRequestAction(new RequestLinkDialog.IRequestAction() {
                    @Override
                    public void refuse(QueueInfo queueInfo) {
                        //拒绝上麦
                        rejectLink(queueInfo);
                    }

                    @Override
                    public void agree(QueueInfo queueInfo) {
                        //同意上麦
                        acceptLink(queueInfo);


                    }
                });
                break;
            case R.id.iv_pause_or_play:
                playOrPauseMusic();
                break;
            case R.id.iv_next:
                playNextMusic();
                break;
        }

    }


    private void playMusicErr() {
        ToastHelper.showToast("伴音发现错误");
        ivPauseOrPlay.setTag(null);
        ivPauseOrPlay.setSelected(false);
        AVChatManager.getInstance().stopAudioMixing();
        updateMusicPlayHint();
    }

    private void playNextMusic() {
        currentPlayIndex = (currentPlayIndex + 1) % musicPathArray.length;
        AVChatManager.getInstance().startAudioMixing(musicPathArray[currentPlayIndex], true, false, 0, 0.3f);
        ivPauseOrPlay.setTag(musicPathArray[currentPlayIndex]);
        ivPauseOrPlay.setSelected(true);
        updateMusicPlayHint();
    }

    private void playOrPauseMusic() {
        boolean isPlaying = ivPauseOrPlay.isSelected();
        String oldPath = (String) ivPauseOrPlay.getTag();
        // 如果正在播放，暂停
        if (isPlaying) {
            AVChatManager.getInstance().pauseAudioMixing();
        }
        //如果已经暂停了，重新播放
        else if (!TextUtils.isEmpty(oldPath)) {
            AVChatManager.getInstance().resumeAudioMixing();
        }
        //之前没有设置任何音乐在播放或暂停
        else {
            AVChatManager.getInstance().startAudioMixing(musicPathArray[currentPlayIndex], true, false, 100, 0.3f);
            ivPauseOrPlay.setTag(musicPathArray[currentPlayIndex]);
        }
        ivPauseOrPlay.setSelected(!isPlaying);

        updateMusicPlayHint();
    }

    private void updateMusicPlayHint() {
        boolean isPlaying = ivPauseOrPlay.isSelected();
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder("音乐" + (currentPlayIndex + 1));
        stringBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#ffa410")),
                0, stringBuilder.length(),
                SpannableStringBuilder.SPAN_INCLUSIVE_EXCLUSIVE);

        stringBuilder.append(isPlaying ? "播放中" : "已暂停");
        tvMusicPlayHint.setText(stringBuilder);

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

    //邀请上麦
    @Override
    public void invitedLink(QueueInfo queueInfo) {
        queueInfo.setStatus(QueueInfo.STATUS_NORMAL);
        queueInfo.setReason(QueueInfo.Reason.inviteByHost);
        chatRoomService.updateQueue(roomInfo.getRoomId(), queueInfo.getKey(), queueInfo.toString()).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ToastHelper.showToast("已将" + queueInfo.getQueueMember().getNick() + "抱上麦位" + inviteIndex + 1);
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

    //踢人下麦
    @Override
    public void removeLink(QueueInfo queueInfo) {
        queueInfo.setStatus(QueueInfo.STATUS_INIT);
        queueInfo.setReason(QueueInfo.Reason.kickByHost);
        chatRoomService.updateQueue(roomInfo.getRoomId(), queueInfo.getKey(),
                queueInfo.toString()).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ToastHelper.showToast("已将“" + queueInfo.getQueueMember().getNick() + "”踢下麦位");
            }

            @Override
            public void onFailed(int i) {
            }

            @Override
            public void onException(Throwable throwable) {

            }
        });

    }

    //有人主动下麦
    @Override
    public void linkCanceled() {

    }

    //
    @Override
    public void mutedText() {

    }

    //
    @Override
    public void muteTextAll() {

    }

    @Override
    public void openAudio(QueueInfo queueInfo) {
        //麦上没人
        switch (queueInfo.getStatus()) {
            case QueueInfo.STATUS_CLOSE:
            case QueueInfo.STATUS_FORBID:
                queueInfo.setStatus(QueueInfo.STATUS_INIT);
                break;
            case QueueInfo.STATUS_BE_MUTED_AUDIO:
                queueInfo.setStatus(QueueInfo.STATUS_NORMAL);
                queueInfo.setReason(QueueInfo.Reason.cancelMuted);
                break;
            case QueueInfo.STATUS_CLOSE_SELF_AUDIO_AND_MUTED:
                queueInfo.setStatus(QueueInfo.STATUS_CLOSE_SELF_AUDIO);
                break;
            case QueueInfo.STATUS_CLOSE_SELF_AUDIO:
                queueInfo.setStatus(QueueInfo.STATUS_CLOSE_SELF_AUDIO_AND_MUTED);
                break;
        }
        chatRoomService.updateQueue(roomInfo.getRoomId(), queueInfo.getKey(),
                queueInfo.toString()).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ToastHelper.showToast("“麦位" + queueInfo.getIndex() + 1 + "”已打开”");
            }

            @Override
            public void onFailed(int i) {

            }

            @Override
            public void onException(Throwable throwable) {

            }
        });

    }

    //关闭麦位
    @Override
    public void closeAudio(QueueInfo queueInfo) {
        queueInfo.setStatus(QueueInfo.STATUS_CLOSE);
        chatRoomService.updateQueue(roomInfo.getRoomId(), queueInfo.getKey(),
                queueInfo.toString()).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ToastHelper.showToast("\"麦位" + queueInfo.getIndex() + "\"已关闭");
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

    //屏蔽某个麦位的语音
    @Override
    public void mutedAudio(QueueInfo queueInfo) {
        switch (queueInfo.getStatus()) {
            case QueueInfo.STATUS_CLOSE_SELF_AUDIO:
                queueInfo.setStatus(QueueInfo.STATUS_CLOSE_SELF_AUDIO_AND_MUTED);
                break;
            case QueueInfo.STATUS_CLOSE_SELF_AUDIO_AND_MUTED:
                queueInfo.setStatus(QueueInfo.STATUS_CLOSE_SELF_AUDIO);
                break;
            default:
                if (QueueInfo.hasOccupancy(queueInfo)) {
                    queueInfo.setStatus(QueueInfo.STATUS_BE_MUTED_AUDIO);
                } else {
                    queueInfo.setStatus(QueueInfo.STATUS_FORBID);
                }
                break;
        }

        chatRoomService.updateQueue(roomInfo.getRoomId(), queueInfo.getKey(), queueInfo.toString()).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
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

    /**
     * 判断麦位是否有人
     * return true:有人
     * false:没人
     */
    private void bottomButtonAction(BottomMenuDialog dialog, QueueInfo queueInfo, String s) {
        switch (s) {
            case "确定踢下麦位":
                bottomMenuDialog = new BottomMenuDialog();
                ArrayList arrayList = new ArrayList();
                arrayList.add("<font color = \"#ff4f4f\">确定踢下麦位</color>");
                arrayList.add("取消");
                removeLink(queueInfo);
                bottomMenuDialog.setItemClickListener((d, p) -> {
                    switch (d.get(p)) {
                        case "<font color = \"#ff4f4f\">确定踢下麦位</color>":
                            removeLink(queueInfo);
                            break;
                        case "取消":
                            bottomMenuDialog.dismiss();
                            break;
                    }
                });
                break;
            case "关闭麦位":
                closeAudio(queueInfo);
                break;
            case "将成员抱上麦位":
                inviteIndex = queueInfo.getIndex();
                MemberActivity.start(AudioLiveActivity.this, roomInfo.getRoomId());
                break;
            case "将TA踢下麦位":
                removeLink(queueInfo);
                break;
            case "屏蔽麦位":
                mutedAudio(queueInfo);
                break;
            case "解除语音屏蔽":
                openAudio(queueInfo);
                break;
            case "打开麦位":
                openAudio(queueInfo);
                break;
            case "退出并解散房间":
                exitRoom();
                break;
            case "取消":
                dialog.dismiss();
                break;
        }
        if (dialog.isVisible()) {
            dialog.dismiss();
        }
    }

    private void checkFile() {
        mixingInfo.path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getPackageName() + File.separator + "music" + File.separator;
        mixingInfo.cycle = 3;
        mixingInfo.loop = true;
        mixingInfo.replace = false;
        mixingInfo.volume = 1f;
        musicPathArray = new String[2];
        musicPathArray[0] = mixingInfo.path + "first_song.mp3";
        musicPathArray[1] = mixingInfo.path + "second_song.mp3";
        currentPlayIndex = 0;
        new Thread(() -> {
            CommonUtil.copyAssetToFile(this, "music/first_song.mp3", mixingInfo.path, "first_song.mp3");
            CommonUtil.copyAssetToFile(this, "music/second_song.mp3", mixingInfo.path, "second_song.mp3");
        }).start();
    }

    @Override
    public void onBackPressed() {
        exitRoom();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
