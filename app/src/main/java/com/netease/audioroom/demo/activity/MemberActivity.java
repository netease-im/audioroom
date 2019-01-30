package com.netease.audioroom.demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.netease.audioroom.demo.R;
import com.netease.audioroom.demo.adapter.MemberListAdapter;
import com.netease.audioroom.demo.base.BaseActivity;
import com.netease.audioroom.demo.cache.RoomMemberCache;
import com.netease.audioroom.demo.util.ScreenUtil;
import com.netease.audioroom.demo.util.ToastHelper;
import com.netease.audioroom.demo.widget.VerticalItemDecoration;
import com.netease.audioroom.demo.widget.unitepage.loadsir.callback.EmptyChatRoomListCallback;
import com.netease.audioroom.demo.widget.unitepage.loadsir.callback.ErrorCallback;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择成员
 */

public class MemberActivity extends BaseActivity {
    public static Integer REQUESTCODE = 0x1001;

    public static String MEMBERACTIVITY = "memberactivity";

    RecyclerView recyclerView;

    String roomId;

    MemberListAdapter adapter;

    public static void start(Activity activity, String roomId) {
        Intent intent = new Intent(activity, MemberActivity.class);
        intent.putExtra(MEMBERACTIVITY, roomId);
        activity.startActivityForResult(intent, REQUESTCODE);
    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_member;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            roomId = getIntent().getStringExtra(MEMBERACTIVITY);
            if (!TextUtils.isEmpty(roomId)) {
                getlist();
            } else {
                ToastHelper.showToast("房间Id为空");
            }
        }

    }

    @Override
    protected void initView() {
        View view = findViewById(R.id.toolsbar);
        TextView title = view.findViewById(R.id.title);
        title.setText("选择成员");
        TextView back = view.findViewById(R.id.icon);
        back.setOnClickListener((v) -> finish());
        recyclerView = findViewById(R.id.member_recyclerView);
        adapter = new MemberListAdapter(null, mContext);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.addItemDecoration(new VerticalItemDecoration(Color.GRAY, ScreenUtil.dip2px(this, 1)));
        recyclerView.setAdapter(adapter);
    }


    private void getlist() {
        RoomMemberCache.getInstance().fetchMembers(roomId, 0, 10, new RequestCallback<List<ChatRoomMember>>() {
            @Override
            public void onSuccess(List<ChatRoomMember> chatRoomMembers) {
                if (chatRoomMembers.size() == 0) {
                    loadService.showCallback(EmptyChatRoomListCallback.class);
                } else {
                    loadService.showSuccess();
                    adapter = new MemberListAdapter((ArrayList<ChatRoomMember>) chatRoomMembers, mContext);
                    recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                    recyclerView.setAdapter(adapter);
                    adapter.setItemClickListener((m, p) -> {
                        //数据是使用Intent返回
                        ChatRoomMember roomMember = chatRoomMembers.get(p);
                        Intent intent = new Intent();
                        intent.putExtra(MEMBERACTIVITY, (Parcelable) roomMember);
                        setResult(RESULT_OK, intent);
                        finish();
                    });
                }

            }

            @Override
            public void onFailed(int i) {
                loadService.showCallback(ErrorCallback.class);
            }

            @Override
            public void onException(Throwable throwable) {
                loadService.showCallback(ErrorCallback.class);

            }
        });

    }
}
