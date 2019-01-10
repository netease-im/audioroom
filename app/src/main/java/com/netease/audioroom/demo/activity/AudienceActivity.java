package com.netease.audioroom.demo.activity;


import android.view.View;

import com.netease.audioroom.demo.R;
import com.netease.audioroom.demo.base.BaseAudioActivity;
import com.netease.audioroom.demo.base.IAudience;
import com.netease.audioroom.demo.model.QueueInfo;
import com.netease.audioroom.demo.permission.MPermission;
import com.netease.audioroom.demo.permission.MPermissionUtil;
import com.netease.audioroom.demo.util.ToastHelper;

import java.util.List;


/***
 * 观众页
 */
public class AudienceActivity extends BaseAudioActivity implements IAudience {


    @Override
    public void enterChatRoom(String roomId) {

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
    }

    @Override
    protected void onQueueItemClick(QueueInfo model, int position) {

    }

    @Override
    protected boolean onQueueItemLongClick(QueueInfo model, int position) {
        return false;
    }


    @Override
    public void requestLink() {

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

}
