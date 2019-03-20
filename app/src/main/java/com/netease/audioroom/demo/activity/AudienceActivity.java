package com.netease.audioroom.demo.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.View;

import com.netease.audioroom.demo.R;
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
import com.netease.audioroom.demo.model.SimpleMessage;
import com.netease.audioroom.demo.util.CommonUtil;
import com.netease.audioroom.demo.util.JsonUtil;
import com.netease.audioroom.demo.util.ToastHelper;
import com.netease.audioroom.demo.widget.unitepage.loadsir.callback.ErrorCallback;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatUserRole;
import com.netease.nimlib.sdk.avchat.model.AVChatParameters;
import com.netease.nimlib.sdk.chatroom.ChatRoomMessageBuilder;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomInfo;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomQueueChangeAttachment;
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomData;
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomResultData;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.ChatRoomQueueChangeType;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.util.Entry;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.netease.audioroom.demo.dialog.BottomMenuDialog.BOTTOMMENUS;


/***
 * 观众页
 */
public class AudienceActivity extends BaseAudioActivity implements IAudience, View.OnClickListener {
    String creator;
    TopTipsDialog topTipsDialog;


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
                if (param.isMute()) {
                    beMutedText();
                }
            }

            @Override
            public void onFailed(int code) {
                creator = "获取当前聊天室信息失败";
                ToastHelper.showToast("获取当前聊天室信息失败code" + code);
            }

            @Override
            public void onException(Throwable exception) {
                // 错误
                ToastHelper.showToast("获取当前聊天室信息失败" + exception.getMessage());
            }
        });
        enterChatRoom(roomInfo.getRoomId());
        joinAudioRoom();
    }

    @Override
    public void onBackPressed() {
        exitRoom();
        super.onBackPressed();

    }

    @Override
    public void enterChatRoom(String roomId) {
        AccountInfo accountInfo = DemoCache.getAccountInfo();
        EnterChatRoomData roomData = new EnterChatRoomData(roomId);
        roomData.setAvatar(accountInfo.avatar);
        roomData.setNick(accountInfo.nick);
        chatRoomService.enterChatRoomEx(roomData, 1).setCallback(new RequestCallback<EnterChatRoomResultData>() {
            @Override
            public void onSuccess(EnterChatRoomResultData resultData) {
                loadService.showSuccess();
                enterRoomSuccess(resultData);
            }

            @Override
            public void onFailed(int i) {
                loadService.showCallback(ErrorCallback.class);
                ToastHelper.showToast("进入聊天室失败 ， code = " + i);
                exitRoom();
            }

            @Override
            public void onException(Throwable throwable) {
                loadService.showCallback(ErrorCallback.class);
                ToastHelper.showToast("进入聊天室异常 ，  e = " + throwable);
                finish();
            }
        });

    }

    @Override
    protected AVChatParameters getRtcParameters() {
        AVChatParameters parameters = new AVChatParameters();
        parameters.setInteger(AVChatParameters.KEY_SESSION_MULTI_MODE_USER_ROLE, AVChatUserRole.AUDIENCE);
        return parameters;
    }


    @Override
    protected void onResume() {
        super.onResume();
        setNetworkReconnection(new INetworkReconnection() {
            @Override
            public void onNetworkReconnection() {
                if (getFragmentManager().findFragmentByTag("TopTipsDialog") != null
                        && getFragmentManager().findFragmentByTag("TopTipsDialog").isVisible()) {
                    ((DialogFragment) getSupportFragmentManager().findFragmentByTag("TopTipsDialog")).dismiss();
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
                Bundle bundle = new Bundle();
                TopTipsDialog topTipsDialog = new TopTipsDialog();
                TopTipsDialog.Style style = topTipsDialog.new Style(
                        "网络断开",
                        0,
                        R.drawable.neterrricon,
                        0);
                bundle.putParcelable(topTipsDialog.TAG, style);
                topTipsDialog.setArguments(bundle);
                topTipsDialog.show(getSupportFragmentManager(), topTipsDialog.TAG);

            }
        });

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
        updateAudioSwitchVisible(false);
        ivSelfAudioSwitch.setSelected(AVChatManager.getInstance().isLocalAudioMuted());
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
            case QueueInfo.STATUS_CLOSE_SELF_AUDIO:
            case QueueInfo.STATUS_CLOSE_SELF_AUDIO_AND_MUTED:
                if (TextUtils.equals(model.getQueueMember().getAccount(), DemoCache.getAccountId())) {
                    //主动下麦
                    cancelLink();
                } else {
                    ToastHelper.showToast("当前麦位有人");
                }
                break;
            case QueueInfo.STATUS_CLOSE:
                ToastHelper.showToast("该麦位已被关闭");
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
    }

    @Override
    protected void initQueue(List<Entry<String, String>> entries) {
        super.initQueue(entries);
        if (selfQueue != null) {
            updateAudioSwitchVisible(true);
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
                Bundle bundle = new Bundle();
                topTipsDialog = new TopTipsDialog();
                TopTipsDialog.Style style = topTipsDialog.new Style(
                        "已申请上麦，等待通过...  <font color=\"#0888ff\">取消</color>",
                        0,
                        0,
                        0);
                bundle.putParcelable(topTipsDialog.TAG, style);
                topTipsDialog.setArguments(bundle);
                topTipsDialog.show(getSupportFragmentManager(), topTipsDialog.TAG);
                topTipsDialog.setClickListener(() -> {
                    topTipsDialog.dismiss();
                    BottomMenuDialog bottomMenuDialog = new BottomMenuDialog();
                    Bundle bundle1 = new Bundle();
                    ArrayList<String> mune = new ArrayList<>();
                    mune.add("<font color=\"#ff4f4f\">确认取消申请上麦</color>");
                    mune.add("取消");
                    bundle1.putStringArrayList(BOTTOMMENUS, mune);
                    bottomMenuDialog.setArguments(bundle1);
                    bottomMenuDialog.show(getSupportFragmentManager(), bottomMenuDialog.TAG);
                    bottomMenuDialog.setItemClickListener((d, p) -> {
                        switch (d.get(p)) {
                            case "<font color=\"#ff4f4f\">确认取消申请上麦</color>":
                                bottomButtonAction(bottomMenuDialog, model, "确认取消申请上麦");
                                break;
                            case "取消":
                                bottomButtonAction(bottomMenuDialog, model, "取消");
                                topTipsDialog.show(getSupportFragmentManager(), topTipsDialog.TAG);
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
            case QueueInfo.STATUS_CLOSE_SELF_AUDIO:
                selfQueue = queueInfo;
                break;
            case QueueInfo.STATUS_CLOSE_SELF_AUDIO_AND_MUTED:
                selfQueue = queueInfo;
                break;

        }

    }


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
        TipsDialog tipsDialog = new TipsDialog();
        Bundle bundle = new Bundle();
        bundle.putString(tipsDialog.TAG, "您的申请已被拒绝");
        tipsDialog.setArguments(bundle);
        tipsDialog.show(getSupportFragmentManager(), "TipsDialog");
        tipsDialog.setClickListener(() -> {
            tipsDialog.dismiss();
            if (topTipsDialog != null && topTipsDialog.isVisible()) {
                topTipsDialog.dismiss();
            }

        });
    }

    @Override
    public void queueLinkNormal(QueueInfo queueInfo) {
        Bundle bundle = new Bundle();
        switch (queueInfo.getReason()) {
            case QueueInfo.Reason.inviteByHost:
                int position = queueInfo.getIndex() + 1;
                TipsDialog tipsDialog = new TipsDialog();
                bundle.putString(tipsDialog.TAG,
                        "您已被主播抱上“麦位”" + position + "\n" +
                                "现在可以进行语音互动啦\n" +
                                "如需下麦，可点击自己的头像或下麦按钮");
                tipsDialog.setArguments(bundle);
                tipsDialog.show(getSupportFragmentManager(), tipsDialog.TAG);
                tipsDialog.setClickListener(() -> tipsDialog.dismiss());
                break;
            case QueueInfo.Reason.agreeApply:
                //主动申请上麦
                TopTipsDialog topTipsDialog = new TopTipsDialog();
                TopTipsDialog.Style style = topTipsDialog.new Style("申请通过!",
                        R.color.color_0888ff,
                        R.drawable.right,
                        R.color.color_ffffff);
                bundle.putParcelable(topTipsDialog.TAG, style);
                topTipsDialog.setArguments(bundle);
                topTipsDialog.show(getSupportFragmentManager(), topTipsDialog.TAG);
                new Handler().postDelayed(() -> topTipsDialog.dismiss(), 2000); // 延时2秒
                break;
            case QueueInfo.Reason.cancelMuted:
                TipsDialog tipsDialog2 = new TipsDialog();
                bundle.putString(tipsDialog2.TAG,
                        "该麦位被主播“解除语音屏蔽”\n" +
                                "现在您可以再次进行语音互动了");
                tipsDialog2.setArguments(bundle);
                tipsDialog2.show(getSupportFragmentManager(), tipsDialog2.TAG);
                tipsDialog2.setClickListener(() -> tipsDialog2.dismiss());
                AVChatManager.getInstance().muteLocalAudio(false);
                break;
        }
        updateAudioSwitchVisible(true);
        updateRole(false);
        selfQueue = queueInfo;
        String cancelTips = DemoCache.getAccountInfo().nick + "进入了麦位" + selfQueue.getIndex();
        ChatRoomMessage chatRoomMessage = ChatRoomMessageBuilder.createChatRoomTextMessage(roomInfo.getRoomId(), cancelTips);
        chatRoomService.sendMessage(chatRoomMessage, false);
        msgAdapter.appendItem(new SimpleMessage(DemoCache.getAccountInfo().nick, cancelTips, SimpleMessage.TYPE_NORMAL_MESSAGE));
    }

    @Override
    public void removed(QueueInfo queueInfo) {
        switch (queueInfo.getReason()) {
            case QueueInfo.Reason.kickByHost:
                TipsDialog tipsDialog = new TipsDialog();
                Bundle bundle = new Bundle();
                bundle.putString(tipsDialog.TAG, "您已被主播请下麦位");
                tipsDialog.setArguments(bundle);
                tipsDialog.show(getSupportFragmentManager(), tipsDialog.TAG);
                tipsDialog.setClickListener(() -> tipsDialog.dismiss());
                updateAudioSwitchVisible(false);
                updateRole(true);
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
        bottomMenuDialog.show(getSupportFragmentManager(), bottomMenuDialog.TAG);
        bottomMenuDialog.setItemClickListener((d, p) -> {
            switch (d.get(p)) {
                case "<font color=\"#ff4f4f\">下麦</color>":
                    bottomButtonAction(bottomMenuDialog, null, "下麦");
                    break;
                case "取消":
                    bottomButtonAction(bottomMenuDialog, null, "取消");
                    break;
            }
        });
    }


    @Override
    public void beMutedAudio(QueueInfo queueInfo) {
        TipsDialog tipsDialog = new TipsDialog();
        Bundle bundle = new Bundle();
        bundle.putString(tipsDialog.TAG,
                "该麦位被主播“屏蔽语音”\n 现在您已无法进行语音互动");
        tipsDialog.setArguments(bundle);
        tipsDialog.show(getSupportFragmentManager(), tipsDialog.TAG);
        tipsDialog.setClickListener(() -> tipsDialog.dismiss());
        selfQueue = queueInfo;
        AVChatManager.getInstance().muteLocalAudio(true);
    }


    @Override
    public void onClick(View view) {
        //事件点击
        if (view == ivSelfAudioSwitch) {
            muteSelfAudio();
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
    protected void muteSelfAudio() {
        super.muteSelfAudio();
        switch (selfQueue.getStatus()) {
            case QueueInfo.STATUS_CLOSE_SELF_AUDIO:
                selfQueue.setStatus(QueueInfo.STATUS_NORMAL);
                break;
            case QueueInfo.STATUS_CLOSE_SELF_AUDIO_AND_MUTED:
                selfQueue.setStatus(QueueInfo.STATUS_BE_MUTED_AUDIO);
                break;
            case QueueInfo.STATUS_BE_MUTED_AUDIO:
                selfQueue.setStatus(QueueInfo.STATUS_CLOSE_SELF_AUDIO_AND_MUTED);
                break;
            default:
                selfQueue.setStatus(QueueInfo.STATUS_CLOSE_SELF_AUDIO);
                break;
        }
        chatRoomService.updateQueue(roomInfo.getRoomId(), selfQueue.getKey(), selfQueue.toString()).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                ivSelfAudioSwitch.setSelected(!AVChatManager.getInstance().isMicrophoneMute());
            }

            @Override
            public void onFailed(int code) {

            }

            @Override
            public void onException(Throwable exception) {

            }
        });


    }

    @Override
    protected void messageInComing(ChatRoomMessage message) {
        super.messageInComing(message);
        MsgAttachment msgAttachment = message.getAttachment();
        if (msgAttachment instanceof CloseRoomAttach) {
            TipsDialog tipsDialog = new TipsDialog();
            Bundle bundle = new Bundle();
            bundle.putString(tipsDialog.TAG, "该房间已被主播解散");
            tipsDialog.setArguments(bundle);
            tipsDialog.show(getSupportFragmentManager(), tipsDialog.TAG);
            tipsDialog.setClickListener(() -> {
                tipsDialog.dismiss();
                release();
            });
        }
    }

    @Override
    protected void beMutedText() {
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
                                updateAudioSwitchVisible(false);
                                updateRole(true);
                                String cancelTips = DemoCache.getAccountInfo().nick + "退出了麦位" + selfQueue.getIndex() + 1;
                                selfQueue = null;
                                ChatRoomMessage chatRoomMessage = ChatRoomMessageBuilder.createChatRoomTextMessage(roomInfo.getRoomId(), cancelTips);
                                chatRoomService.sendMessage(chatRoomMessage, false);
                                msgAdapter.appendItem(new SimpleMessage(DemoCache.getAccountInfo().nick, cancelTips, SimpleMessage.TYPE_NORMAL_MESSAGE));
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


    private void updateAudioSwitchVisible(boolean visible) {
        if (visible) {
            ivCancelLink.setVisibility(View.VISIBLE);
            ivSelfAudioSwitch.setVisibility(View.VISIBLE);
        } else {
            ivCancelLink.setVisibility(View.GONE);
            ivSelfAudioSwitch.setVisibility(View.GONE);
        }
    }


    private void updateRole(boolean isAudience) {
        AVChatParameters parameters = new AVChatParameters();
        parameters.setInteger(AVChatParameters.KEY_SESSION_MULTI_MODE_USER_ROLE, isAudience ? AVChatUserRole.AUDIENCE : AVChatUserRole.NORMAL);
        AVChatManager.getInstance().setParameters(parameters);
    }

}
