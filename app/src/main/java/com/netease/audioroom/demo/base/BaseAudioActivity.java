package com.netease.audioroom.demo.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.audioroom.demo.R;
import com.netease.audioroom.demo.adapter.QueueAdapter;
import com.netease.audioroom.demo.model.DemoRoomInfo;
import com.netease.audioroom.demo.model.QueueInfo;
import com.netease.audioroom.demo.util.ToastHelper;
import com.netease.audioroom.demo.widget.HeadImageView;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomInfo;

import java.util.ArrayList;


/**
 * 主播与观众基础页，包含所有的基本UI元素
 */
public abstract class BaseAudioActivity extends PermissionActivity {


    public static final String ROOM_INFO_KEY = "room_info_key";
    private static final String TAG = "BaseAudioActivity";

    //主播基础信息
    protected HeadImageView ivLiverAvatar;
    protected ImageView ivLiverAudioCloseHint;
    protected TextView tvLiverNick;
    protected TextView tvRoomName;

    // 各种控制开关
    protected ImageView ivMuteOtherText;
    protected ImageView ivAudioQuality;
    protected ImageView ivCloseSelfAudio;
    protected ImageView ivCloseRoomAudio;
    protected ImageView ivCancelLink;
    protected ImageView ivExistRoom;

    //聊天室队列（麦位）
    protected RecyclerView rcyQueueList;

    //消息列表
    protected RecyclerView rcyChatMsgList;


    // 聊天室信息
    protected DemoRoomInfo demoRoomInfo;


    // 聊天室服务
    protected ChatRoomService chatRoomService;

    private QueueAdapter queueAdapter;


    private BaseAdapter.ItemClickListener<QueueInfo> itemClickListener = new BaseAdapter.ItemClickListener<QueueInfo>() {
        @Override
        public void onItemClick(QueueInfo model, int position) {
            onQueueItemClick(model, position);
        }
    };
    private BaseAdapter.ItemLongClickListener<QueueInfo> itemLongClickListener = new BaseAdapter.ItemLongClickListener<QueueInfo>() {
        @Override
        public boolean onItemLongClick(QueueInfo model, int position) {
            return onQueueItemLongClick(model, position);
        }
    };



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getContentViewID());

        demoRoomInfo = getIntent().getParcelableExtra(ROOM_INFO_KEY);
        if (demoRoomInfo == null) {
            ToastHelper.showToast("聊天室信息不能为空");
            finish();
            return;
        }
        chatRoomService = NIMClient.getService(ChatRoomService.class);
        findBaseView();
        setupBaseViewInner();
        setupBaseView();
    }


    private void findBaseView() {

        View baseAudioView = findViewById(R.id.rl_base_audio_ui);

        if (baseAudioView == null) {
            throw new IllegalStateException("xml layout must include base_audio_ui.xml layout");
        }

        ivLiverAvatar = baseAudioView.findViewById(R.id.iv_liver_avatar);
        ivLiverAudioCloseHint = baseAudioView.findViewById(R.id.iv_liver_audio_close_hint);
        tvLiverNick = baseAudioView.findViewById(R.id.tv_liver_nick);

        tvRoomName = baseAudioView.findViewById(R.id.tv_chat_room_name);

        ivMuteOtherText = baseAudioView.findViewById(R.id.iv_mute_other_text);
        ivAudioQuality = baseAudioView.findViewById(R.id.iv_audio_quality_switch);
        ivCloseSelfAudio = baseAudioView.findViewById(R.id.iv_close_self_audio_switch);
        ivCloseRoomAudio = baseAudioView.findViewById(R.id.iv_close_room_audio_switch);
        ivCancelLink = baseAudioView.findViewById(R.id.iv_cancel_link);
        ivExistRoom = baseAudioView.findViewById(R.id.iv_exist_room);


        rcyQueueList = baseAudioView.findViewById(R.id.rcy_queue_list);
        rcyChatMsgList = baseAudioView.findViewById(R.id.rcy_chat_message_list);


    }

    private void setupBaseViewInner() {


        String name = demoRoomInfo.getName();
        name = "房间：" + (TextUtils.isEmpty(name) ? demoRoomInfo.getRoomId() : name) + "（" + demoRoomInfo.getOnlineUserCount() + "人）";

        tvRoomName.setText(name);

        rcyQueueList.setLayoutManager(new GridLayoutManager(this, 4));

        //todo
        ArrayList<QueueInfo> queueInfo = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            QueueInfo queue = new QueueInfo();
            queue.setIndex(i + 1);
            queueInfo.add(queue);
        }
        queueAdapter = new QueueAdapter(queueInfo, this);
        rcyQueueList.setAdapter(queueAdapter);


        queueAdapter.setItemClickListener(itemClickListener);
        queueAdapter.setItemLongClickListener(itemLongClickListener);


        chatRoomService.fetchRoomInfo(demoRoomInfo.getRoomId()).setCallback(new RequestCallback<ChatRoomInfo>() {
            @Override
            public void onSuccess(ChatRoomInfo chatRoomInfo) {

            }

            @Override
            public void onFailed(int i) {
            }

            @Override
            public void onException(Throwable throwable) {

            }
        });

//        LoginInfo loginInfo = new LoginInfo("wen01", "e10adc3949ba59abbe56e057f20f883e");
//        NIMClient.getService(AuthService.class).login(loginInfo).setCallback(new RequestCallback() {
//            @Override
//            public void onSuccess(Object o) {
//
//            }
//
//            @Override
//            public void onFailed(int i) {
//
//            }
//
//            @Override
//            public void onException(Throwable throwable) {
//
//            }
//        });

    }




    protected abstract int getContentViewID();

    protected abstract void setupBaseView();

    protected abstract void onQueueItemClick(QueueInfo model, int position);

    protected abstract boolean onQueueItemLongClick(QueueInfo model, int position);


}
