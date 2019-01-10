package com.netease.audioroom.demo.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.netease.audioroom.demo.R;
import com.netease.audioroom.demo.adapter.ChatRoomListAdapter;
import com.netease.audioroom.demo.base.BaseActivity;
import com.netease.audioroom.demo.base.BaseAdapter;
import com.netease.audioroom.demo.base.BaseAudioActivity;
import com.netease.audioroom.demo.http.ChatRoomHttpClient;
import com.netease.audioroom.demo.model.DemoRoomInfo;
import com.netease.audioroom.demo.util.ScreenUtil;
import com.netease.audioroom.demo.util.ToastHelper;
import com.netease.audioroom.demo.widget.HeadImageView;
import com.netease.audioroom.demo.widget.VerticalItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener, BaseAdapter.ItemClickListener<DemoRoomInfo> {


    private HeadImageView ivAvatar;
    private TextView tvNick;
    private RecyclerView rcyChatList;

    private ChatRoomListAdapter chatRoomListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupView();
        fetchChatRoomList();
    }

    private void setupView() {
        ivAvatar = findViewById(R.id.iv_self_avatar);
        tvNick = findViewById(R.id.tv_self_nick);
        rcyChatList = findViewById(R.id.rcy_chat_room_list);
        findViewById(R.id.iv_create_chat_room).setOnClickListener(this);

        rcyChatList.setLayoutManager(new LinearLayoutManager(this));
        //todo
        ArrayList<DemoRoomInfo> roomList = new ArrayList<>();
        DemoRoomInfo demoRoomInfo1 = new DemoRoomInfo();
        demoRoomInfo1.setOnlineUserCount(12);
        demoRoomInfo1.setName("临时测试1");
        roomList.add(demoRoomInfo1);

        DemoRoomInfo demoRoomInfo2 = new DemoRoomInfo();
        demoRoomInfo2.setOnlineUserCount(1143);
        demoRoomInfo2.setName("临时测试2");
        roomList.add(demoRoomInfo2);


        chatRoomListAdapter = new ChatRoomListAdapter(roomList, this);
        // 每个item 16dp 的间隔
        rcyChatList.addItemDecoration(new VerticalItemDecoration(Color.TRANSPARENT, ScreenUtil.dip2px(this, 16)));
        rcyChatList.setAdapter(chatRoomListAdapter);
        chatRoomListAdapter.setItemClickListener(this);


    }


    private void fetchChatRoomList() {

        ChatRoomHttpClient.getInstance().fetchChatRoomList(new ChatRoomHttpClient.ChatRoomHttpCallback<List<DemoRoomInfo>>() {
            @Override
            public void onSuccess(List<DemoRoomInfo> demoRoomInfoList) {

            }

            @Override
            public void onFailed(int code, String errorMsg) {
                ToastHelper.showToast("获取聊天室列表失败 ， code = " + code);
            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.iv_create_chat_room) {
            showCreateChatRoomDialog();
        }

    }

    private void showCreateChatRoomDialog() {

        //todo

        createChatRoom("测试");

    }

    private void createChatRoom(String roomName) {
        if (TextUtils.isEmpty(roomName) || roomName.trim().length() == 0) {

            ToastHelper.showToast("房间名不能为空");
            return;
        }

        //todo
//        ChatRoomHttpClient.getInstance().masterEnterRoom("wen01", roomName, new ChatRoomHttpClient.ChatRoomHttpCallback<DemoRoomInfo>() {
//            @Override
//            public void onSuccess(DemoRoomInfo demoRoomInfo) {
//                if (demoRoomInfo == null) {
//                    ToastHelper.showToast("创建房间失败，返回信息为空");
//                    return;
//                }
//                if (isActivityPaused()) {
//                    return;
//                }
//
//
//                Intent intent = new Intent(MainActivity.this, AudioLiveActivity.class);
//                intent.putExtra(BaseAudioActivity.ROOM_INFO_KEY, demoRoomInfo);
//                MainActivity.this.startActivity(intent);
//            }
//
//            @Override
//            public void onFailed(int code, String errorMsg) {
//                ToastHelper.showToast("创建房间失败");
//            }
//        });


        //todo  测试代码
        DemoRoomInfo demoRoomInfo = new DemoRoomInfo();
        demoRoomInfo.setCreator("wen01");
        demoRoomInfo.setRoomId("60379482");
        Intent intent = new Intent(MainActivity.this, AudioLiveActivity.class);
        intent.putExtra(BaseAudioActivity.ROOM_INFO_KEY, demoRoomInfo);
        MainActivity.this.startActivity(intent);


    }

    @Override
    public void onItemClick(DemoRoomInfo model, int position) {
        //todo 进入聊天室
    }


//    private static class FetchChatRoomListCallback implements ChatRoomHttpClient.ChatRoomHttpCallback<List<DemoRoomInfo>> {
//
//        private final WeakReference<MainActivity> mainActivityWeakReference;
//
//        FetchChatRoomListCallback(MainActivity activity) {
//            this.mainActivityWeakReference = new WeakReference<>(activity);
//        }
//
//        @Override
//        public void onSuccess(List<DemoRoomInfo> chatRoomInfoList) {
//
//        }
//
//        @Override
//        public void onFailed(int code, String errorMsg) {
//            MainActivity activity = mainActivityWeakReference.get();
//            if (activity == null) {
//                return;
//            }
//            ToastHelper.showToast(activity, "获取聊天室列表失败 ， code = " + code);
//        }
//    }

}
