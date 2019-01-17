package com.netease.audioroom.demo.activity;


import android.view.View;
import android.widget.ImageView;

import com.netease.audioroom.demo.R;
import com.netease.audioroom.demo.base.BaseAudioActivity;
import com.netease.audioroom.demo.base.IAudience;
import com.netease.audioroom.demo.cache.DemoCache;
import com.netease.audioroom.demo.custom.CloseRoomAttach;
import com.netease.audioroom.demo.custom.P2PNotificationHelper;
import com.netease.audioroom.demo.model.QueueInfo;
import com.netease.audioroom.demo.permission.MPermission;
import com.netease.audioroom.demo.permission.MPermissionUtil;
import com.netease.audioroom.demo.util.CommonUtil;
import com.netease.audioroom.demo.util.ToastHelper;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomResultData;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;

import java.util.ArrayList;
import java.util.List;


/***
 * 观众页
 */
public class AudienceActivity extends BaseAudioActivity implements IAudience, View.OnClickListener {

    @Override
    protected void enterRoomSuccess(EnterChatRoomResultData resultData) {
        super.enterRoomSuccess(resultData);

        String creatorId = resultData.getRoomInfo().getCreator();
        ArrayList<String> accountList = new ArrayList();
        accountList.add(creatorId);
        chatRoomService.fetchRoomMembersByIds(resultData.getRoomId(), accountList).setCallback(new RequestCallback<List<ChatRoomMember>>() {
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
    protected int getContentViewID() {
        return R.layout.activity_audience;
    }

    @Override
    protected void setupBaseView() {
        ivMuteOtherText.setVisibility(View.GONE);
        ivAudioQuality.setVisibility(View.GONE);
        ivCloseSelfAudio.setVisibility(View.GONE);
        ivCancelLink.setVisibility(View.GONE);

        ivCloseSelfAudio.setOnClickListener(this);
        ivCancelLink.setOnClickListener(this);
        ivExistRoom.setOnClickListener(this);
    }

    @Override
    protected void onQueueItemClick(QueueInfo model, int position) {

        if (model.getStatus() != QueueInfo.INIT_STATUS) {
            return;
        }
        requestLink(model);
    }

    @Override
    protected boolean onQueueItemLongClick(QueueInfo model, int position) {
        return false;
    }

    @Override
    protected void receiveNotification(CustomNotification customNotification) {
        //todo

    }


    @Override
    public void requestLink(QueueInfo model) {
        P2PNotificationHelper.requestLink(model, DemoCache.getAccountInfo(), roomInfo.getCreator(), new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //todo 请求连接成功，等待主播同意
                ToastHelper.showToast("请求连麦成功");

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
    public void cancelLinkRequest() {

    }

    @Override
    public void linkBeRejected() {

    }

    @Override
    public void linkBeAccept() {

    }

    @Override
    public void beInvitedLink() {

    }

    @Override
    public void beRemoved() {

    }

    @Override
    public void cancelLink() {

    }

    @Override
    public void beMutedText() {

    }

    @Override
    public void beMutedAudio() {

    }


    protected void onLivePermissionGranted() {

        ToastHelper.showToast("授权成功");

    }

    protected void onLivePermissionDenied() {
        List<String> deniedPermissions = MPermission.getDeniedPermissions(this, LIVE_PERMISSIONS);
        String tip = "您拒绝了权限" + MPermissionUtil.toString(deniedPermissions) + "，无法开启直播";
        ToastHelper.showToast(tip);
    }

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

        if (view == ivCloseSelfAudio) {

        } else if (view == ivCancelLink) {

        } else if (view == ivExistRoom) {
            //todo
            chatRoomService.exitChatRoom(roomInfo.getRoomId());
            finish();
        }

    }

    @Override
    protected void messageInComing(ChatRoomMessage message) {
        super.messageInComing(message);

        //
        MsgAttachment msgAttachment = message.getAttachment();
        if (msgAttachment != null && msgAttachment instanceof CloseRoomAttach) {
            //todo UI & 释放资源
            ToastHelper.showToast("主播关闭了房间");
            finish();
            return;

        }

//        if(message.)
    }
}
