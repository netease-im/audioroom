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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.audioroom.demo.R;
import com.netease.audioroom.demo.audio.SimpleNRtcCallback;
import com.netease.audioroom.demo.base.BaseAudioActivity;
import com.netease.audioroom.demo.base.IAudioLive;
import com.netease.audioroom.demo.cache.DemoCache;
import com.netease.audioroom.demo.cache.RoomMemberCache;
import com.netease.audioroom.demo.custom.CloseRoomAttach;
import com.netease.audioroom.demo.custom.P2PNotificationHelper;
import com.netease.audioroom.demo.dialog.BottomMenuDialog;
import com.netease.audioroom.demo.dialog.RequestLinkDialog;
import com.netease.audioroom.demo.http.ChatRoomHttpClient;
import com.netease.audioroom.demo.model.AccountInfo;
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
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.chatroom.ChatRoomMessageBuilder;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomQueueChangeAttachment;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomRoomMemberInAttachment;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomTempMuteAddAttachment;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomTempMuteRemoveAttachment;
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomResultData;
import com.netease.nimlib.sdk.msg.constant.ChatRoomQueueChangeType;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nrtc.sdk.NRtcCallback;
import com.netease.nrtc.sdk.NRtcConstants;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.netease.audioroom.demo.dialog.BottomMenuDialog.BOTTOMMENUS;
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

    private String[] musicPathArray;
    private int currentPlayIndex;

    private int inviteIndex = -1;//抱麦位置


    //聊天室队列元素
    private HashMap<String, QueueInfo> queueMap = new HashMap<>();

    TextView semicircleView;

    ArrayList<QueueInfo> requestMemberList;//申请麦位列表


    private TextView tvMusicPlayHint;
    private ImageView ivPauseOrPlay;
    private ImageView ivNext;
    private RequestLinkDialog requestLinkDialog;

    @Override
    protected int getContentViewID() {
        return R.layout.activity_live;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestLivePermission();
        enterChatRoom(roomInfo.getRoomId());
        enableAudienceRole(false);
        joinChannel(audioUid);
        checkFile();
    }


    @Override
    protected void initView() {
        semicircleView = findViewById(R.id.semicircleView);
        tvMusicPlayHint = findViewById(R.id.tv_music_play_hint);
        ivPauseOrPlay = findViewById(R.id.iv_pause_or_play);
        ivNext = findViewById(R.id.iv_next);
        ivPauseOrPlay.setOnClickListener(this);
        ivNext.setOnClickListener(this);
        requestMemberList = new ArrayList<>();
        semicircleView.setVisibility(View.GONE);
        semicircleView.setClickable(true);
        updateMusicPlayHint();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //抱麦
            ChatRoomMember chatRoomMember = data.getParcelableExtra(MemberActivity.MEMBERACTIVITY);
            QueueMember queueMember = new QueueMember(chatRoomMember.getAccount(), chatRoomMember.getNick(), chatRoomMember.getAvatar());
            invitedLink(new QueueInfo(inviteIndex, queueMember, QueueInfo.STATUS_NORMAL, QueueInfo.Reason.inviteByHost));
        }
    }

    @Override
    protected void setupBaseView() {
        ivCancelLink.setVisibility(View.GONE);
        ivMuteOtherText.setOnClickListener(this);
        ivSelfAudioSwitch.setOnClickListener(this);
        ivRoomAudioSwitch.setOnClickListener(this);
        ivExistRoom.setOnClickListener(this);
        semicircleView.setOnClickListener(this);
    }


    @Override
    protected void onQueueItemClick(QueueInfo queueInfo, int position) {
        Bundle bundle = new Bundle();
        final BottomMenuDialog bottomMenuDialog = new BottomMenuDialog();
        ArrayList<String> mune = new ArrayList<>();
        //当前麦位有人了
        switch (queueInfo.getStatus()) {
            case QueueInfo.STATUS_INIT:
                ToastHelper.showToast("麦位初始化状态");
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
                ToastHelper.showToast("麦位上有人，且能正常发言");
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
                ToastHelper.showToast("麦位关闭");
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
                ToastHelper.showToast("麦位上没人，但是被主播屏蔽");
                mune.add("解除语音屏蔽");
                mune.add("取消");
                bundle.putStringArrayList(BOTTOMMENUS, mune);
                bottomMenuDialog.setArguments(bundle);
                bottomMenuDialog.setItemClickListener((d, p) -> {
                    switch (d.get(p)) {
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
                ToastHelper.showToast(" 麦位上有人，但是他关闭了自己的语音");
                mune.add("将TA踢下麦位");
                mune.add("取消");
                bundle.putStringArrayList(BOTTOMMENUS, mune);
                bottomMenuDialog.setArguments(bundle);
                bottomMenuDialog.setItemClickListener((d, p) -> {
                    switch (d.get(p)) {
                        case "将TA踢下麦位":
                            bottomButtonAction(bottomMenuDialog, queueInfo, "将TA踢下麦位");
                            break;
                        case "取消":
                            bottomButtonAction(bottomMenuDialog, queueInfo, "取消");
                            break;

                    }
                });
                break;

        }
        bottomMenuDialog.show(getFragmentManager(), BOTTOMMENUS);
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
                    //更新为请求状态
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
                queueInfo.setReason(QueueInfo.Reason.cancelApplyBySelf);
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
                    ToastHelper.showToast("有人请求下麦");
                }
                break;
        }

    }

    @Override
    protected void exitRoom() {
        release();
        if (roomInfo != null) {
            ChatRoomMessage closeRoomMessage = ChatRoomMessageBuilder
                    .createChatRoomCustomMessage(roomInfo.getRoomId(), new CloseRoomAttach());
            chatRoomService.sendMessage(closeRoomMessage, false)
                    .setCallback(new RequestCallbackWrapper<Void>() {
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
        queueInfo.setStatus(QueueInfo.STATUS_INIT);
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
        chatRoomService.updateQueue(roomInfo.getRoomId(), queueInfo.getKey(),
                queueInfo.toString()).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                requestMemberList.remove(queueInfo);
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
        if (view == ivMuteOtherText) {
            //禁言
            MuteMemberListActivity.start(mContext, roomInfo.getRoomId());

        } else if (view == ivSelfAudioSwitch) {
            boolean mutex = ivSelfAudioSwitch.isSelected();
            ivSelfAudioSwitch.setSelected(!mutex);
            muteSelfAudio(!mutex);
            if (mutex) {
                ToastHelper.showToast("话筒已开启");
            } else {
                ToastHelper.showToast("话筒已关闭");
            }

        } else if (view == ivRoomAudioSwitch) {
            boolean close = ivRoomAudioSwitch.isSelected();
            ivRoomAudioSwitch.setSelected(!close);
            muteRoomAudio(!close);
            if (close) {
                ToastHelper.showToast("已关闭“聊天室声音”");
            } else {
                ToastHelper.showToast("已打开“聊天室声音”");
            }
        } else if (view == ivExistRoom) {
            BottomMenuDialog bottomMenuDialog = new BottomMenuDialog();
            Bundle bundle1 = new Bundle();
            ArrayList<String> mune = new ArrayList<>();
            mune.add("<font color=\"#ff4f4f\">确认取消申请上麦</color>");
            mune.add("取消");
            bundle1.putStringArrayList(BOTTOMMENUS, mune);
            bottomMenuDialog.setArguments(bundle1);
            bottomMenuDialog.show(getFragmentManager(), "BottomMenuDialog");
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

        } else if (view == semicircleView) {
            requestLinkDialog = new RequestLinkDialog();
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(QUEUEINFOLIST, requestMemberList);
            requestLinkDialog.setArguments(bundle);
            requestLinkDialog.show(getFragmentManager(), "RequestLinkDialog");
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

        } else if (view == ivPauseOrPlay) {
            playOrPauseMusic();
        } else if (view == ivNext) {
            playNextMusic();
        }
    }


    private void playMusicErr() {
        ToastHelper.showToast("伴音发现错误");
        ivPauseOrPlay.setTag(null);
        ivPauseOrPlay.setSelected(false);
        nrtcEx.stopAudioMixing();
        updateMusicPlayHint();
    }

    private void playNextMusic() {
        currentPlayIndex = (currentPlayIndex + 1) % musicPathArray.length;
        nrtcEx.startAudioMixing(musicPathArray[currentPlayIndex], true, false, 0, 1.0f);
        ivPauseOrPlay.setTag(musicPathArray[currentPlayIndex]);
        ivPauseOrPlay.setSelected(true);
        updateMusicPlayHint();
    }

    private void playOrPauseMusic() {
        boolean isPlaying = ivPauseOrPlay.isSelected();
        String oldPath = (String) ivPauseOrPlay.getTag();
        // 如果正在播放，暂停
        if (isPlaying) {
            nrtcEx.pauseAudioMixing();
        }
        //如果已经暂停了，重新播放
        else if (!TextUtils.isEmpty(oldPath)) {
            nrtcEx.resumeAudioMixing();
        }
        //之前没有设置任何音乐在播放或暂停
        else {
            nrtcEx.startAudioMixing(musicPathArray[currentPlayIndex], true, false, 100, 1.0f);
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
        if (queueInfo.getStatus() == QueueInfo.STATUS_CLOSE || queueInfo.getStatus() == QueueInfo.STATUS_FORBID) {
            queueInfo.setStatus(QueueInfo.STATUS_INIT);
        } else if (queueInfo.getStatus() == QueueInfo.STATUS_BE_MUTED_AUDIO) {
            queueInfo.setStatus(QueueInfo.STATUS_NORMAL);
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
                ToastHelper.showToast("");
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
        if (QueueInfo.hasOccupancy(queueInfo)) {
            ToastHelper.showToast("有人");
            queueInfo.setStatus(QueueInfo.STATUS_BE_MUTED_AUDIO);
        } else {
            ToastHelper.showToast("没人");
            queueInfo.setStatus(QueueInfo.STATUS_FORBID);
        }
        chatRoomService.updateQueue(roomInfo.getRoomId(), queueInfo.getKey(), queueInfo.toString()).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ToastHelper.showToast("");
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
                BottomMenuDialog bottomMenuDialog = new BottomMenuDialog();
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
                //主播退出房间
                release();
                ChatRoomMessage closeRoomMessage = ChatRoomMessageBuilder.createChatRoomCustomMessage(roomInfo.getRoomId(), new CloseRoomAttach());
                chatRoomService.sendMessage(closeRoomMessage, false).setCallback(new RequestCallbackWrapper<Void>() {
                    @Override
                    public void onResult(int i, Void aVoid, Throwable throwable) {
                        ChatRoomHttpClient.getInstance().closeRoom(DemoCache.getAccountId(), roomInfo.getRoomId(), null);
                        ToastHelper.showToast("房间已解散");
                    }
                });
                finish();
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
        String musicRootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getPackageName() + File.separator + "music" + File.separator;

        musicPathArray = new String[2];
        musicPathArray[0] = musicRootPath + "first_song.mp3";
        musicPathArray[1] = musicRootPath + "second_song.mp3";
        currentPlayIndex = 0;

        new Thread(() -> {
            CommonUtil.copyAssetToFile(this, "music/first_song.mp3", musicRootPath, "first_song.mp3");
            CommonUtil.copyAssetToFile(this, "music/second_song.mp3", musicRootPath, "second_song.mp3");
        }).start();
    }

    @Override
    protected NRtcCallback createNrtcCallBack() {

        return new InnerNRtcCallBack();
    }

    private class InnerNRtcCallBack extends SimpleNRtcCallback {
        @Override
        public void onDeviceEvent(int event, String desc) {
            super.onDeviceEvent(event, desc);
            if (event == NRtcConstants.DeviceEvent.AUDIO_MIXING_ERROR) {

                playMusicErr();
            } else if (event == NRtcConstants.DeviceEvent.AUDIO_MIXING_FINISHED) {
                playNextMusic();
            }


        }
    }


}
