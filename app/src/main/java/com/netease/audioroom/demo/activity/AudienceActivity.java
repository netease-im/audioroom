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
import com.netease.audioroom.demo.base.BaseAudioActivity;
import com.netease.audioroom.demo.base.IAudience;
import com.netease.audioroom.demo.cache.DemoCache;
import com.netease.audioroom.demo.custom.CloseRoomAttach;
import com.netease.audioroom.demo.custom.P2PNotificationHelper;
import com.netease.audioroom.demo.dialog.BottomMenuDialog;
import com.netease.audioroom.demo.dialog.TipsDialog;
import com.netease.audioroom.demo.dialog.TopTipsDialog;
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
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomQueueChangeAttachment;
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
    public static String AUDIENCEACTIVITY = "AudienceActivity";
    /**
     * 是否正在申请连麦中
     */
    private boolean isRequestingLink = false;
    /**
     * 是否主动下麦
     */
    private boolean isCancelLink = false;

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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableAudienceRole(true);
        joinChannel(audioUid);
        requestLivePermission();
    }

    @Override
    protected void initView() {

    }


    @Override
    protected void enterRoomSuccess(EnterChatRoomResultData resultData) {
        super.enterRoomSuccess(resultData);
        String creatorId = resultData.getRoomInfo().getCreator();
        ArrayList<String> accountList = new ArrayList<>();
        accountList.add(creatorId);
        chatRoomService.fetchRoomMembersByIds(resultData.getRoomId(), accountList)
                .setCallback(new RequestCallback<List<ChatRoomMember>>() {
                    @Override
                    public void onSuccess(List<ChatRoomMember> chatRoomMembers) {
                        loadService.showSuccess();
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
    protected int getContentViewID() {
        return R.layout.activity_audience;
    }


    @Override
    protected void setupBaseView() {
        ivMuteOtherText.setVisibility(View.GONE);
        ivAudioQuality.setVisibility(View.GONE);
        ivSelfAudioSwitch.setVisibility(View.GONE);
        ivCancelLink.setVisibility(View.GONE);

        ivSelfAudioSwitch.setOnClickListener(this);
        ivCancelLink.setOnClickListener(this);
        ivRoomAudioSwitch.setOnClickListener(this);
        ivExistRoom.setOnClickListener(this);
    }

    @Override
    protected void onQueueItemClick(QueueInfo model, int position) {
        switch (model.getStatus()) {
            case QueueInfo.STATUS_INIT:
                //申请上麦
                requestLink(model);
                break;
            case QueueInfo.STATUS_NORMAL:
                if (TextUtils.equals(model.getQueueMember().getAccount(), DemoCache.getAccountId())) {
                    //下麦
                    removed(model);
                }

                break;
            case QueueInfo.STATUS_FORBID:
            case QueueInfo.STATUS_BE_MUTED_AUDIO:
                //麦位被禁止
                TipsDialog tipsDialog = new TipsDialog();
                Bundle bundle = new Bundle();
                bundle.putString(TipsDialog.TIPSDIALOG, "该麦位被主播“屏蔽语音”\n现在您已无法进行语音互动");
                tipsDialog.setArguments(bundle);
                tipsDialog.show(getSupportFragmentManager(), AUDIENCEACTIVITY);
                break;
        }
        if (model.getStatus() != QueueInfo.STATUS_INIT) {
            return;
        }
        if (isRequestingLink) {
            ToastHelper.showToast("您正在连麦中，无法申请上麦");
            return;
        }

        //自己已经在麦上了
        if (selfQueue != null) {
            return;
            //下麦
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
            //todo
        }
    }

    @Override
    public void requestLink(QueueInfo model) {
        P2PNotificationHelper.requestLink(model, DemoCache.getAccountInfo(), roomInfo.getCreator(), new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                isRequestingLink = true;
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
        if (status == QueueInfo.STATUS_NORMAL) {
            queueLinkNormal(queueInfo);
        } else if (status == QueueInfo.STATUS_BE_MUTED_AUDIO) {
            beMutedAudio(queueInfo);
        } else if (status == QueueInfo.STATUS_INIT || status == QueueInfo.STATUS_CLOSE) {
            removed(queueInfo);
        }


    }


    //取消连麦
    @Override
    public void cancelLinkRequest(QueueInfo queueInfo) {
        //todo
        P2PNotificationHelper.cancelLinkRequest(queueInfo, DemoCache.getAccountId(), roomInfo.getCreator(), new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                isRequestingLink = false;
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
        isRequestingLink = false;
        ToastHelper.showToast("主播拒绝了你的连麦请求");
        TipsDialog tipsDialog = new TipsDialog();
        Bundle bundle = new Bundle();
        bundle.putString(TipsDialog.TIPSDIALOG, "您的申请已被拒绝");
        tipsDialog.setArguments(bundle);
        tipsDialog.show(getSupportFragmentManager(), "TipsDialog");
    }

    @Override
    public void queueLinkNormal(QueueInfo queueInfo) {
        if (isRequestingLink) {
            if (topTipsDialog != null && topTipsDialog.isVisible()) {
                topTipsDialog.dismiss();
            }
            topTipsDialog = new TopTipsDialog();
            Bundle bundle = new Bundle();
            TopTipsDialog.Style style = topTipsDialog.new Style("申请通过!",
                    R.color.color_0888ff,
                    R.drawable.right,
                    R.color.color_ffffff);
            bundle.putParcelable(TopTipsDialog.TOPTIPSDIALOG, style);
            topTipsDialog.setArguments(bundle);
            topTipsDialog.show(getFragmentManager(), TopTipsDialog.TOPTIPSDIALOG);
            new Handler().postDelayed(() -> topTipsDialog.dismiss(), 2000); // 延时2秒


        } else if (selfQueue == null) {
            //TODO 你被主播抱麦
            ToastHelper.showToast("你被主播抱麦");
        } else if (selfQueue.getStatus() == QueueInfo.STATUS_BE_MUTED_AUDIO) {
            //todo 主播解除对你的语音屏蔽
            ToastHelper.showToast("主播解除对你的语音屏蔽");
        }

        ivCancelLink.setVisibility(View.VISIBLE);
        enableAudienceRole(false);
        selfQueue = queueInfo;
        isCancelLink = false;
        isRequestingLink = false;
    }


    @Override
    public void removed(QueueInfo queueInfo) {
        if (isCancelLink) {
            ToastHelper.showToast("主动下麦成功");
            isCancelLink = false;
        } else {
            ToastHelper.showToast("你被主播下麦");
        }
        ivCancelLink.setVisibility(View.GONE);
        enableAudienceRole(true);
        selfQueue = null;
    }

    @Override
    public void cancelLink() {
        if (selfQueue == null) {
            return;
        }
        isCancelLink = true;
        P2PNotificationHelper.cancelLink(selfQueue.getIndex(),
                DemoCache.getAccountInfo().account,
                roomInfo.getCreator(),
                new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        ToastHelper.showToast("申请下麦成功");
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
    public void beMutedText() {

    }

    @Override
    public void beMutedAudio(QueueInfo queueInfo) {
        //TODO 主播屏蔽了你的语音
        ToastHelper.showToast("主播屏蔽了你的语音");
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
                cancelLink();
                break;
            case "取消":
                dialog.dismiss();
                break;
        }
        if (dialog.isVisible()) {
            dialog.dismiss();
        }
    }
}
