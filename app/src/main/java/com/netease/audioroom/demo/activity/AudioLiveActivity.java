package com.netease.audioroom.demo.activity;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.netease.audioroom.demo.R;
import com.netease.audioroom.demo.base.BaseAudioActivity;
import com.netease.audioroom.demo.base.IAudioLive;
import com.netease.audioroom.demo.model.QueueInfo;
import com.netease.audioroom.demo.permission.MPermission;
import com.netease.audioroom.demo.permission.MPermissionUtil;
import com.netease.audioroom.demo.util.ToastHelper;

import java.util.List;

/**
 * 主播页
 */
public class AudioLiveActivity extends BaseAudioActivity implements IAudioLive {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestLivePermission();
    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_live;
    }

    @Override
    protected void setupBaseView() {
        ivCancelLink.setVisibility(View.GONE);
    }


    @Override
    protected void onQueueItemClick(QueueInfo model, int position) {

    }

    @Override
    protected boolean onQueueItemLongClick(QueueInfo model, int position) {
        return false;
    }


    @Override
    public void enterChatRoom(String roomId) {

    }

    @Override
    public void linkRequest() {

    }

    @Override
    public void linkRequestCancel() {

    }

    @Override
    public void rejectLink() {

    }

    @Override
    public void acceptLink() {

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
        StringBuilder sb = new StringBuilder();
        sb.append("无法开启直播，请到系统设置页面开启权限");
        sb.append(MPermissionUtil.toString(neverAskAgainPermission));
        if (deniedPermissions != null && !deniedPermissions.isEmpty()) {
            sb.append(",下次询问请授予权限");
            sb.append(MPermissionUtil.toString(deniedPermissions));
        }
        ToastHelper.showToastLong(sb.toString());
    }

}
