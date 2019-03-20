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
import com.netease.audioroom.demo.model.QueueInfo;
import com.netease.audioroom.demo.model.QueueMember;
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
    public static String MEMBERACTIVITYREPEATLIST = "memberRepeatlist";

    RecyclerView recyclerView;

    String roomId;

    MemberListAdapter adapter;

    ArrayList<QueueInfo> mQueueInfoList;//去除重复项


    public static void start(Activity activity, String roomId) {
        Intent intent = new Intent(activity, MemberActivity.class);
        intent.putExtra(MEMBERACTIVITY, roomId);
        activity.startActivityForResult(intent, REQUESTCODE);
    }

    //去除重复
    public static void startRepeat(Activity activity, String roomId, ArrayList<QueueInfo> queueInfoArrayList) {
        Intent intent = new Intent(activity, MemberActivity.class);
        intent.putExtra(MEMBERACTIVITY, roomId);
        intent.putExtra(MEMBERACTIVITYREPEATLIST, queueInfoArrayList);
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
            mQueueInfoList = (ArrayList<QueueInfo>) getIntent().getSerializableExtra(MEMBERACTIVITYREPEATLIST);
            if (!TextUtils.isEmpty(roomId)) {
                getlist();
            } else {
                ToastHelper.showToast("房间Id为空");
            }
        }

    }

    @Override
    protected void initViews() {
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
                    loadService.setCallBack(EmptyChatRoomListCallback.class, (context, view) -> {
                        ((TextView) (view.findViewById(R.id.toolsbar).findViewById(R.id.title))).setText("选择成员");
                        view.findViewById(R.id.toolsbar).findViewById(R.id.icon).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });
                    });
                } else {
                    loadService.showSuccess();
                    ArrayList<QueueMember> queueMembers;
                    if (mQueueInfoList != null) {
                        queueMembers = repeateLoad(chatRoomMembers);
                    } else {
                        queueMembers = repeatMuteList(chatRoomMembers);
                    }

                    if (queueMembers != null && queueMembers.size() != 0) {
                        adapter = new MemberListAdapter(queueMembers, mContext);
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
                    } else {
                        loadService.showCallback(EmptyChatRoomListCallback.class);
                        loadService.setCallBack(EmptyChatRoomListCallback.class, (context, view) -> {
                            ((TextView) (view.findViewById(R.id.toolsbar).findViewById(R.id.title))).setText("选择成员");
                            view.findViewById(R.id.toolsbar).findViewById(R.id.icon).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    finish();
                                }
                            });
                        });
                    }

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


        //去除已经禁言的用户

    }

    //去除麦位上除申请的用户
    private ArrayList<QueueMember> repeateLoad(List<ChatRoomMember> chatRoomMembers) {
        ArrayList<QueueMember> queueMembers = new ArrayList<>();
        ArrayList<QueueMember> mQueueMembers = new ArrayList<>();
        if (mQueueInfoList != null) {
            for (QueueInfo queueInfo : mQueueInfoList) {
                mQueueMembers.add(queueInfo.getQueueMember());
            }
        }

        for (ChatRoomMember chatRoomMember : chatRoomMembers) {
            QueueMember queueMember = new QueueMember(chatRoomMember.getAccount(), chatRoomMember.getNick(), chatRoomMember.getAvatar());
            if (!mQueueMembers.contains(queueMember)) {
                queueMembers.add(queueMember);
                continue;
            }
            for (QueueInfo queueInfo : mQueueInfoList) {
                if (queueInfo.getQueueMember() != null && queueInfo.getQueueMember().equals(queueMember) && queueInfo.getStatus() == QueueInfo.STATUS_LOAD) {
                    queueMembers.add(queueMember);
                }
            }
        }
        return queueMembers;
    }

    private ArrayList<QueueMember> repeatMuteList(List<ChatRoomMember> chatRoomMembers) {
        ArrayList<QueueMember> queueMembers = new ArrayList<>();
        for (ChatRoomMember chatRoomMember : chatRoomMembers) {
            if (!chatRoomMember.isMuted() && !chatRoomMember.isTempMuted()) {
                queueMembers.add(new QueueMember(chatRoomMember.getAccount(), chatRoomMember.getNick(), chatRoomMember.getAvatar()));
            }
        }


        return queueMembers;

    }

}
