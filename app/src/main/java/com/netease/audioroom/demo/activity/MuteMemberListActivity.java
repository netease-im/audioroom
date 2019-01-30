package com.netease.audioroom.demo.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.audioroom.demo.R;
import com.netease.audioroom.demo.adapter.MuteMemberListAdapter;
import com.netease.audioroom.demo.base.BaseActivity;
import com.netease.audioroom.demo.cache.RoomMemberCache;
import com.netease.audioroom.demo.util.ScreenUtil;
import com.netease.audioroom.demo.util.ToastHelper;
import com.netease.audioroom.demo.widget.VerticalItemDecoration;
import com.netease.audioroom.demo.widget.unitepage.loadsir.callback.EmptyMuteRoomListCallback;
import com.netease.audioroom.demo.widget.unitepage.loadsir.callback.ErrorCallback;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;
import com.netease.nimlib.sdk.chatroom.model.MemberOption;

import java.util.ArrayList;
import java.util.List;

/**
 * 禁言成员页面（可侧滑）
 */
public class MuteMemberListActivity extends BaseActivity {
    public static String MUTEMEMBERLISTACTIVITY = "MuteMemberListActivity";

    String roomId;

    TextView addMuteMember, muteAllMember;
    RecyclerView recyclerView;
    MuteMemberListAdapter adapter;

    List<ChatRoomMember> muteList;
    LinearLayout empty_view;


    public static void start(Context context, String roomId) {
        Intent intent = new Intent(context, MuteMemberListActivity.class);
        intent.putExtra(MUTEMEMBERLISTACTIVITY, roomId);
        context.startActivity(intent);
    }


    @Override
    protected int getContentViewID() {
        return R.layout.activity_mute_member;
    }

    @Override
    protected void initView() {
        addMuteMember = findViewById(R.id.addMuteMember);
        muteAllMember = findViewById(R.id.muteAllMember);
        recyclerView = findViewById(R.id.member_recyclerView);
        empty_view = findViewById(R.id.empty_view);
        muteList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.addItemDecoration(new VerticalItemDecoration(Color.WHITE, 1));
        if (getIntent() != null) {
            roomId = getIntent().getStringExtra(MUTEMEMBERLISTACTIVITY);
            getMuteList();
        } else {
            ToastHelper.showToast("传值错误");
        }
        addMuteMember.setOnClickListener(v -> addMuteMember());
        muteAllMember.setOnClickListener(v -> muteAllMember());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            muteList.add(data.getParcelableExtra(MemberActivity.MEMBERACTIVITY));
            //禁言
            if (muteList.size() == 0) {
                recyclerView.setVisibility(View.GONE);
                empty_view.setVisibility(View.VISIBLE);
            } else {
                empty_view.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                MemberOption option = new MemberOption(roomId, muteList.get(0).getAccount());
                NIMClient.getService(ChatRoomService.class).markChatRoomMutedList(true, option)
                        .setCallback(new RequestCallback<ChatRoomMember>() {
                            @Override
                            public void onSuccess(ChatRoomMember param) {
                                ToastHelper.showToast("禁言成功");
                                // 成功
                                ArrayList<String> accountList = new ArrayList<>();
                                for (String account : accountList) {
                                    accountList.add(account);
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

                adapter = new MuteMemberListAdapter(mContext, muteList);
                recyclerView.setAdapter(adapter);


            }

        }
    }


    private void getMuteList() {
        RoomMemberCache.getInstance().fetchMembers(roomId, 0, 100, new RequestCallback<List<ChatRoomMember>>() {
            @Override
            public void onSuccess(List<ChatRoomMember> chatRoomMembers) {
                loadService.showSuccess();
                for (ChatRoomMember c : chatRoomMembers) {
                    if (c.isMuted()) muteList.add(c);
                }
                if (muteList.size() != 0) {
                    empty_view.setVisibility(View.GONE);
                    adapter = new MuteMemberListAdapter(mContext, muteList);
                    recyclerView.setAdapter(adapter);

                } else {
                    recyclerView.setVisibility(View.GONE);
                    empty_view.setVisibility(View.VISIBLE);

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

    //添加禁言成员
    private void addMuteMember() {
        MemberActivity.start(this, roomId);
    }

    //禁言所有成员
    private void muteAllMember() {


    }
}
