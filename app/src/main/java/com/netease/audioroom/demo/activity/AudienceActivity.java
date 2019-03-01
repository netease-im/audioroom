package com.netease.audioroom.demo.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.netease.audioroom.demo.R;
import com.netease.audioroom.demo.audio.SimpleNRtcCallback;
import com.netease.audioroom.demo.base.BaseAudioActivity;
import com.netease.audioroom.demo.base.LoginManager;
import com.netease.audioroom.demo.base.action.IAudience;
import com.netease.audioroom.demo.base.action.INetworkReconnection;
import com.netease.audioroom.demo.cache.DemoCache;
import com.netease.audioroom.demo.custom.CloseRoomAttach;
import com.netease.audioroom.demo.custom.P2PNotificationHelper;
import com.netease.audioroom.demo.dialog.BottomMenuDialog;
import com.netease.audioroom.demo.dialog.TipsDialog;
import com.netease.audioroom.demo.dialog.TopTipsDialog;
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
import com.netease.audioroom.demo.widget.unitepage.loadsir.callback.ErrorCallback;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomInfo;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomQueueChangeAttachment;
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomResultData;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.ChatRoomQueueChangeType;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.util.Entry;
import com.netease.nrtc.sdk.NRtcCallback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.netease.audioroom.demo.dialog.BottomMenuDialog.BOTTOMMENUS;


/***
 * 观众页
 */
public class AudienceActivity extends BaseAudioActivity implements IAudience, View.OnClickListener {

    TopTipsDialog topTipsDialog;
    String creator;

    public static void start(Context context, DemoRoomInfo model) {
        Intent intent = new Intent(context, AudienceActivity.class);
        intent.putExtra(BaseAudioActivity.ROOM_INFO_KEY, model);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_from_left);
        }
    }


    @Override
    protected int getContentViewID() {
        return R.layout.activity_audience;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // roomId为聊天室ID
        NIMClient.getService(ChatRoomService.class).fetchRoomInfo(roomInfo.getRoomId()).setCallback(new RequestCallback<ChatRoomInfo>() {
            @Override
            public void onSuccess(ChatRoomInfo param) {
                // 成功
                creator = param.getCreator();
            }

            @Override
            public void onFailed(int code) {
                // 失败
                ToastHelper.showToast(code + "");
            }

            @Override
            public void onException(Throwable exception) {
                // 错误
                ToastHelper.showToast(exception.getMessage() + "");
            }
        });
        enterChatRoom(roomInfo.getRoomId());
        enableAudienceRole(true);
        joinChannel(audioUid);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //获取聊天室主播麦克风状态
        NIMClient.getService(ChatRoomService.class).fetchRoomInfo(roomInfo.getRoomId())
                .setCallback(new RequestCallback<ChatRoomInfo>() {
                    @Override
                    public void onSuccess(ChatRoomInfo param) {
                        // 成功
                        if (param.getExtension() != null) {
                            for (Map.Entry<String, Object> entry : param.getExtension().entrySet()) {
                                if (entry.getKey().equals(ROOM__INFO_KEY_MICROPHONE)) {
                                    if ((Boolean) entry.getValue()) {
                                        ivLiverAvatar.startWaveAnimation();
                                    } else {
                                        ivLiverAvatar.stopWaveAnimation();
                                    }
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailed(int code) {
                        // 失败
                    }

                    @Override
                    public void onException(Throwable exception) {
                        // 错误
                    }
                });
        if (roomInfo.isMicrophoneOpen()) {
            ivLiverAvatar.startWaveAnimation();
        } else {
            ivLiverAvatar.stopWaveAnimation();
        }
        setNetworkReconnection(new INetworkReconnection() {
            @Override
            public void onNetworkReconnection() {
                if (topTipsDialog != null && topTipsDialog.isVisible()) {
                    topTipsDialog.dismiss();
                }
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
                topTipsDialog = new TopTipsDialog();
                Bundle bundle = new Bundle();
                TopTipsDialog.Style style = topTipsDialog.new Style(
                        "网络断开",
                        0,
                        R.drawable.neterrricon,
                        0);
                bundle.putParcelable(TopTipsDialog.TOPTIPSDIALOG, style);
                topTipsDialog.setArguments(bundle);
                topTipsDialog.show(getFragmentManager(), "TopTipsDialog");
                topTipsDialog.setClickListener(() -> {
                });
            }
        });

//        ((SimpleNRtcCallback)createNrtcCallBack()).onReportSpeaker(creator,);
    }

    @Override
    protected void enterRoomSuccess(EnterChatRoomResultData resultData) {
        super.enterRoomSuccess(resultData);
        loadService.showSuccess();
        String creatorId = resultData.getRoomInfo().getCreator();
        ArrayList<String> accountList = new ArrayList<>();
        accountList.add(creatorId);
        chatRoomService.fetchRoomMembersByIds(resultData.getRoomId(), accountList)
                .setCallback(new RequestCallback<List<ChatRoomMember>>() {
                    @Override
                    public void onSuccess(List<ChatRoomMember> chatRoomMembers) {
                        if (CommonUtil.isEmpty(chatRoomMembers)) {
                            ToastHelper.showToast("获取主播信息失败 ， 结果为空");
                            return;
                        }
                        ChatRoomMember roomMember = chatRoomMembers.get(0);
                        ivLiverAvatar.loadAvatar(roomMember.getAvatar());
                        tvLiverNick.setText(roomMember.getNick());
                    }

                    @Override
                    public void onFailed(int i) {

                        ToastHelper.showToast("获取主播信息失败 ， code = " + i);
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        ToastHelper.showToast("获取主播信息异常 ， e = " + throwable);
                    }
                });

    }


    @Override
    protected void setupBaseView() {
        ivMuteOtherText.setVisibility(View.GONE);
        hasMicrophone(false);
        ivSelfAudioSwitch.setOnClickListener(this);
        ivCancelLink.setOnClickListener(this);
        ivRoomAudioSwitch.setOnClickListener(this);
        ivExistRoom.setOnClickListener(this);
    }

    @Override
    protected void onQueueItemClick(QueueInfo model, int position) {
        switch (model.getStatus()) {
            case QueueInfo.STATUS_INIT:
            case QueueInfo.STATUS_FORBID:
                //申请上麦
                requestLink(model);
                break;
            case QueueInfo.STATUS_LOAD:
                ToastHelper.showToast("该麦位正在被申请,\n请尝试申请其他麦位");
                break;
            case QueueInfo.STATUS_NORMAL:
            case QueueInfo.STATUS_BE_MUTED_AUDIO:
                if (TextUtils.equals(model.getQueueMember().getAccount(), DemoCache.getAccountId())) {
                    //主动下麦
                    cancelLink();
                } else {
                    ToastHelper.showToast("当前麦位有人");
                }
                break;
            case QueueInfo.STATUS_CLOSE:
                //麦位被禁止
                TipsDialog tipsDialog = new TipsDialog();
                Bundle bundle = new Bundle();
                bundle.putString(TipsDialog.TIPSDIALOG, "该麦位被主播“屏蔽语音”\n现在您已无法进行语音互动");
                tipsDialog.setArguments(bundle);
                tipsDialog.show(getSupportFragmentManager(), TipsDialog.TIPSDIALOG);
                tipsDialog.setClickListener(() -> tipsDialog.dismiss());
                break;
        }

    }

    @Override
    protected boolean onQueueItemLongClick(QueueInfo model, int position) {
        return false;
    }

    @Override
    protected void receiveNotification(CustomNotification customNotification) {
        if (!TextUtils.equals(customNotification.getFromAccount(), roomInfo.getCreator())) {
            return;
        }
        String content = customNotification.getContent();
        if (TextUtils.isEmpty(content)) {
            return;
        }
        JSONObject jsonObject = JsonUtil.parse(content);
        if (jsonObject == null) {
            return;
        }
    }

    @Override
    protected void exitRoom() {
        release();
        //退出
        if (roomInfo != null) {
            chatRoomService.exitChatRoom(roomInfo.getRoomId());
            roomInfo = null;
        }
        finish();
    }

    @Override
    protected void initQueue(List<Entry<String, String>> entries) {
        super.initQueue(entries);
        if (selfQueue != null) {
            hasMicrophone(true);
        }
    }

    /**
     * 请求连麦
     */
    @Override
    public void requestLink(QueueInfo model) {
        if (selfQueue != null) {
            ToastHelper.showToast("您已在麦上");
            return;
        }
        P2PNotificationHelper.requestLink(model, DemoCache.getAccountInfo(), roomInfo.getCreator(), new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                topTipsDialog = new TopTipsDialog();
                Bundle bundle = new Bundle();
                TopTipsDialog.Style style = topTipsDialog.new Style(
                        "已申请上麦，等待通过...  <font color=\"#0888ff\">取消</color>",
                        0,
                        0,
                        0);
                bundle.putParcelable(TopTipsDialog.TOPTIPSDIALOG, style);
                topTipsDialog.setArguments(bundle);
                topTipsDialog.show(getFragmentManager(), "TopTipsDialog");
                topTipsDialog.setClickListener(() -> {
                    topTipsDialog.dismiss();
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
                            case "<font color=\"#ff4f4f\">确认取消申请上麦</color>":
                                bottomButtonAction(bottomMenuDialog, model, "确认取消申请上麦");
                                break;
                            case "取消":
                                bottomButtonAction(bottomMenuDialog, model, "取消");
                                topTipsDialog.show(getFragmentManager(), "TopTipsDialog");
                                break;
                        }


                    });
                });
            }

            @Override
            public void onFailed(int i) {
                ToastHelper.showToast("请求连麦失败 ， code = " + i);
            }

            @Override
            public void onException(Throwable throwable) {
                ToastHelper.showToast("请求连麦异常 ， e = " + throwable);
            }
        });
    }


    @Override
    protected void onQueueChange(ChatRoomQueueChangeAttachment queueChange) {
        super.onQueueChange(queueChange);
        ChatRoomQueueChangeType changeType = queueChange.getChatRoomQueueChangeType();
        String value = queueChange.getContent();
        //只关心新增元素或更新
        if (changeType != ChatRoomQueueChangeType.OFFER || TextUtils.isEmpty(value)) {
            return;
        }
        QueueInfo queueInfo = new QueueInfo(value);
        QueueMember member = queueInfo.getQueueMember();
        //与自己无关
        if (member == null || !TextUtils.equals(member.getAccount(), DemoCache.getAccountId())) {
            return;
        }
        int status = queueInfo.getStatus();

        switch (status) {
            case QueueInfo.STATUS_NORMAL:
                queueLinkNormal(queueInfo);
                break;
            case QueueInfo.STATUS_BE_MUTED_AUDIO:
                beMutedAudio(queueInfo);
                break;
            case QueueInfo.STATUS_INIT:
                if (queueInfo.getReason() == QueueInfo.Reason.cancelApplyByHost) {
                    linkBeRejected();
                } else if (queueInfo.getReason() == QueueInfo.Reason.kickByHost) {
                    removed(queueInfo);
                }
                break;
            case QueueInfo.STATUS_CLOSE:
                removed(queueInfo);
                break;
        }

    }


    //取消连麦
    @Override
    public void cancelLinkRequest(QueueInfo queueInfo) {
        P2PNotificationHelper.cancelLinkRequest(queueInfo, DemoCache.getAccountId(), roomInfo.getCreator(), new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

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
    public void linkBeRejected() {
        if (topTipsDialog != null && topTipsDialog.isVisible()) {
            topTipsDialog.dismiss();
        }
        TipsDialog tipsDialog = new TipsDialog();
        Bundle bundle = new Bundle();
        bundle.putString(TipsDialog.TIPSDIALOG, "您的申请已被拒绝");
        tipsDialog.setArguments(bundle);
        tipsDialog.show(getSupportFragmentManager(), "TipsDialog");
        tipsDialog.setClickListener(() -> tipsDialog.dismiss());
    }

    @Override
    public void queueLinkNormal(QueueInfo queueInfo) {
        Bundle bundle = new Bundle();
        switch (queueInfo.getReason()) {
            case QueueInfo.Reason.inviteByHost:
                int position = queueInfo.getIndex() + 1;
                TipsDialog tipsDialog = new TipsDialog();
                bundle.putString(TipsDialog.TIPSDIALOG,
                        "您已被主播抱上“麦位”" + position + "\n" +
                                "现在可以进行语音互动啦\n" +
                                "如需下麦，可点击自己的头像或下麦按钮");
                tipsDialog.setArguments(bundle);

                tipsDialog.show(getSupportFragmentManager(), TipsDialog.TIPSDIALOG);
                tipsDialog.setClickListener(() -> tipsDialog.dismiss());
                break;
            case QueueInfo.Reason.agreeApply:
                //主动申请上麦
                if (topTipsDialog != null && topTipsDialog.isVisible()) {
                    topTipsDialog.dismiss();
                }
                topTipsDialog = new TopTipsDialog();
                TopTipsDialog.Style style = topTipsDialog.new Style("申请通过!",
                        R.color.color_0888ff,
                        R.drawable.right,
                        R.color.color_ffffff);
                bundle.putParcelable(TopTipsDialog.TOPTIPSDIALOG, style);
                topTipsDialog.setArguments(bundle);
                topTipsDialog.show(getFragmentManager(), TopTipsDialog.TOPTIPSDIALOG);

                new Handler().postDelayed(() -> topTipsDialog.dismiss(), 2000); // 延时2秒
                break;
            case QueueInfo.Reason.cancelMuted:
                TipsDialog tipsDialog2 = new TipsDialog();
                bundle.putString(TipsDialog.TIPSDIALOG,
                        "该麦位被主播“解除语音屏蔽”\n" +
                                "现在您可以再次进行语音互动了");
                tipsDialog2.setArguments(bundle);

                tipsDialog2.show(getSupportFragmentManager(), TipsDialog.TIPSDIALOG);
                tipsDialog2.setClickListener(() -> tipsDialog2.dismiss());
                break;
        }

        hasMicrophone(true);
        enableAudienceRole(false);
        selfQueue = queueInfo;
    }


    @Override
    public void removed(QueueInfo queueInfo) {
        if (topTipsDialog != null && topTipsDialog.isVisible()) {
            topTipsDialog.dismiss();
        }
        switch (queueInfo.getReason()) {
            case QueueInfo.Reason.kickByHost:
                TipsDialog tipsDialog = new TipsDialog();
                Bundle bundle = new Bundle();
                bundle.putString(TipsDialog.TIPSDIALOG, "您已被主播请下麦位");
                tipsDialog.setArguments(bundle);
                tipsDialog.show(getSupportFragmentManager(), TipsDialog.TIPSDIALOG);
                tipsDialog.setClickListener(() -> tipsDialog.dismiss());
                hasMicrophone(false);
                enableAudienceRole(true);
                selfQueue = null;
                break;
        }

    }

    @Override
    public void cancelLink() {
        if (selfQueue == null) {
            return;
        }
        BottomMenuDialog bottomMenuDialog = new BottomMenuDialog();
        Bundle bundle1 = new Bundle();
        ArrayList<String> mune = new ArrayList<>();
        mune.add("<font color=\"#ff4f4f\">下麦</color>");
        mune.add("取消");
        bundle1.putStringArrayList(BOTTOMMENUS, mune);
        bottomMenuDialog.setArguments(bundle1);
        bottomMenuDialog.show(getFragmentManager(), "BottomMenuDialog");
        bottomMenuDialog.setItemClickListener((d, p) -> {
            switch (d.get(p)) {
                case "<font color=\"#ff4f4f\">下麦</color>":
                    bottomButtonAction(bottomMenuDialog, null, "下麦");
                    break;
                case "取消":
                    bottomButtonAction(bottomMenuDialog, null, "取消");
//                    topTipsDialog.show(getFragmentManager(), "TopTipsDialog");
                    break;
            }
        });
    }

    @Override
    public void beMutedText() {

    }

    @Override
    public void beMutedAudio(QueueInfo queueInfo) {
        TipsDialog tipsDialog = new TipsDialog();
        Bundle bundle = new Bundle();
        bundle.putString(TipsDialog.TIPSDIALOG,
                "该麦位被主播“屏蔽语音”\n 现在您已无法进行语音互动");
        tipsDialog.setArguments(bundle);
        tipsDialog.show(getSupportFragmentManager(), TipsDialog.TIPSDIALOG);
        tipsDialog.setClickListener(() -> tipsDialog.dismiss());
        selfQueue = queueInfo;
        enableAudienceRole(true);

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
        StringBuilder builder = new StringBuilder();
        builder.append("无法开启直播，请到系统设置页面开启权限");
        builder.append(MPermissionUtil.toString(neverAskAgainPermission));
        if (deniedPermissions != null && !deniedPermissions.isEmpty()) {
            builder.append(",下次询问请授予权限");
            builder.append(MPermissionUtil.toString(deniedPermissions));
        }

        ToastHelper.showToastLong(builder.toString());
    }

    @Override
    public void onClick(View view) {
        //事件点击
        if (view == ivSelfAudioSwitch) {
            boolean mutex = ivSelfAudioSwitch.isSelected();
            ivSelfAudioSwitch.setSelected(!mutex);
            muteSelfAudio(!mutex);
        } else if (view == ivCancelLink) {
            cancelLink();
        } else if (view == ivRoomAudioSwitch) {
            boolean close = ivRoomAudioSwitch.isSelected();
            ivRoomAudioSwitch.setSelected(!close);
            muteRoomAudio(!close);
        } else if (view == ivExistRoom) {
            exitRoom();
        }

    }

    @Override
    protected void messageInComing(ChatRoomMessage message) {
        super.messageInComing(message);
        MsgAttachment msgAttachment = message.getAttachment();
        if (msgAttachment != null && msgAttachment instanceof CloseRoomAttach) {
            release();
            TipsDialog tipsDialog = new TipsDialog();
            Bundle bundle = new Bundle();
            bundle.putString(TipsDialog.TIPSDIALOG, "该房间已被主播解散");
            tipsDialog.setArguments(bundle);
            tipsDialog.show(getSupportFragmentManager(), "TipsDialog");
            tipsDialog.setClickListener(() -> {
                finish();
                tipsDialog.dismiss();
            });
        }
    }

    @Override
    protected void mute() {
        super.mute();
        edtInput.setHint("您已被禁言");
        edtInput.setFocusable(false);
        edtInput.setFocusableInTouchMode(false);
        sendButton.setClickable(false);
        ToastHelper.showToast("您已被禁言");
    }

    @Override
    protected void cancelMute() {
        super.cancelMute();
        edtInput.setHint("唠两句~");
        edtInput.setFocusableInTouchMode(true);
        edtInput.setFocusable(true);
        edtInput.requestFocus();
        sendButton.setClickable(true);
        ToastHelper.showToast("您的禁言被解除");
    }

    private void bottomButtonAction(BottomMenuDialog dialog, QueueInfo queueInfo, String s) {
        switch (s) {
            case "确认取消申请上麦":
                cancelLinkRequest(queueInfo);
                break;
            case "下麦":
                P2PNotificationHelper.cancelLink(selfQueue.getIndex(),
                        DemoCache.getAccountInfo().account,
                        roomInfo.getCreator(),
                        new RequestCallback<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                ToastHelper.showToast("您已下麦");
                                hasMicrophone(false);
                                selfQueue = null;
                            }

                            @Override
                            public void onFailed(int i) {
                            }

                            @Override
                            public void onException(Throwable throwable) {

                            }
                        });
                break;
            case "取消":
                dialog.dismiss();
                break;
        }
        if (dialog.isVisible()) {
            dialog.dismiss();
        }
    }


    //是否是上麦者
    private void hasMicrophone(boolean hasMicrophone) {
        if (hasMicrophone) {
            ivCancelLink.setVisibility(View.VISIBLE);
            ivSelfAudioSwitch.setVisibility(View.VISIBLE);
        } else {
            ivCancelLink.setVisibility(View.GONE);
            ivSelfAudioSwitch.setVisibility(View.GONE);
        }
    }

    @Override
    protected NRtcCallback createNrtcCallBack() {

        return new InnerNRtcCallBack();
    }

    private class InnerNRtcCallBack extends SimpleNRtcCallback {

        @Override
        public void onReportSpeaker(int activated, long[] speakers, int[] energies, int mixedEnergy) {
            super.onReportSpeaker(activated, speakers, energies, mixedEnergy);
            ToastHelper.showToast("接收到回调");
        }

        @Override
        public void onUserMuteAudio(long uid, boolean muted) {
            super.onUserMuteAudio(uid, muted);
            ToastHelper.showToast("接收到回调");
        }
    }
}
