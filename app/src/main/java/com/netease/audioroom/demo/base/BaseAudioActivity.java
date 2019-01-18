package com.netease.audioroom.demo.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.audioroom.demo.R;
import com.netease.audioroom.demo.adapter.QueueAdapter;
import com.netease.audioroom.demo.cache.DemoCache;
import com.netease.audioroom.demo.model.AccountInfo;
import com.netease.audioroom.demo.model.DemoRoomInfo;
import com.netease.audioroom.demo.model.QueueInfo;
import com.netease.audioroom.demo.util.CommonUtil;
import com.netease.audioroom.demo.util.ToastHelper;
import com.netease.audioroom.demo.widget.HeadImageView;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.chatroom.ChatRoomMessageBuilder;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.ChatRoomServiceObserver;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomNotificationAttachment;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomPartClearAttachment;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomQueueChangeAttachment;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomRoomMemberInAttachment;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomTempMuteAddAttachment;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomTempMuteRemoveAttachment;
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomData;
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomResultData;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.NotificationType;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.util.Entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 主播与观众基础页，包含所有的基本UI元素
 */
public abstract class BaseAudioActivity extends PermissionActivity {

    public static final String ROOM_INFO_KEY = "room_info_key";
    private static final String TAG = "AudioRoom";

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

    private EditText edtInput;


    //聊天室队列（麦位）
    protected RecyclerView rcyQueueList;

    //消息列表
    protected RecyclerView rcyChatMsgList;


    // 聊天室信息
    protected DemoRoomInfo roomInfo;


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

    // 自定义通知观察者
    private Observer<CustomNotification> customNotification = new Observer<CustomNotification>() {
        @Override
        public void onEvent(CustomNotification customNotification) {
            receiveNotification(customNotification);
        }
    };


    //聊天室消息观察者
    private Observer<List<ChatRoomMessage>> messageObserver = new Observer<List<ChatRoomMessage>>() {
        @Override
        public void onEvent(List<ChatRoomMessage> chatRoomMessages) {
            if (chatRoomMessages == null || chatRoomMessages.isEmpty()) {
                return;
            }

            StringBuffer stringBuffer = new StringBuffer();
            for (ChatRoomMessage message : chatRoomMessages) {

                if (message.getSessionType() != SessionTypeEnum.ChatRoom
                        || !TextUtils.equals(message.getSessionId(), roomInfo.getRoomId())) {
                    continue;
                }

                if (message.getAttachment() instanceof ChatRoomNotificationAttachment) {
                    NotificationType type = ((ChatRoomNotificationAttachment) message.getAttachment()).getType();
                    switch (type) {
                        // 成员进入聊天室
                        case ChatRoomMemberIn:
                            ChatRoomRoomMemberInAttachment memberIn = (ChatRoomRoomMemberInAttachment) message.getAttachment();
                            stringBuffer.append("chat room member in :").append(memberIn.isMuted())
                                    .append(", isTempMuted:").append(memberIn.isTempMuted())
                                    .append(", temp muted time:").append(memberIn.getTempMutedTime()).append("///");
                            break;

                        // 成员退出聊天室
                        //{"opeNick":"wenD1","target":["wen01"],"tarNick":["wenD1"],"operator":"wen01"}
                        case ChatRoomMemberExit:
                            ChatRoomQueueChangeAttachment memberExit = (ChatRoomQueueChangeAttachment) message.getAttachment();
                            break;

                        //成员被禁言
                        case ChatRoomMemberTempMuteAdd:
                            ChatRoomTempMuteAddAttachment addMuteMember = (ChatRoomTempMuteAddAttachment) message.getAttachment();
                            stringBuffer.append("chat room temp mute add :").append(addMuteMember.getMuteDuration());
                            break;

                        //成员被解除禁言
                        case ChatRoomMemberTempMuteRemove:
                            ChatRoomTempMuteRemoveAttachment muteRemove = (ChatRoomTempMuteRemoveAttachment) message.getAttachment();
                            stringBuffer.append("chat room temp mute remove :").append(muteRemove.getMuteDuration());
                            break;

                        //队列变更
                        case ChatRoomQueueChange:
                            ChatRoomQueueChangeAttachment queueChange = (ChatRoomQueueChangeAttachment) message.getAttachment();
                            stringBuffer.append("chat room queue change :").append(queueChange.getChatRoomQueueChangeType())
                                    .append(", key:").append(queueChange.getKey())
                                    .append(", content:").append(queueChange.getContent())
                                    .append(", ext : ").append(queueChange.getExtension() == null ? " is null " : queueChange.getExtension().toString());
                            break;

                        //队列批量变更，好像没用了
                        case ChatRoomQueueBatchChange:
                            ChatRoomPartClearAttachment queuePartClear = (ChatRoomPartClearAttachment) message.getAttachment();
                            stringBuffer.append("chat room part clear :").append(queuePartClear.getChatRoomQueueChangeType());
                            for (String key : queuePartClear.getContentMap().keySet()) {
                                stringBuffer.append("key= " + key + " and value= " + queuePartClear.getContentMap().get(key));
                            }
                            break;
                    }
                } else {
                    messageInComing(message);
                }
            }
            if (stringBuffer.length() > 0) {
                Log.i(TAG, stringBuffer.toString());
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getContentViewID());

        roomInfo = getIntent().getParcelableExtra(ROOM_INFO_KEY);
        if (roomInfo == null) {
            ToastHelper.showToast("聊天室信息不能为空");
            finish();
            return;
        }
        chatRoomService = NIMClient.getService(ChatRoomService.class);
        enterChatRoom(roomInfo.getRoomId());
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

        edtInput = baseAudioView.findViewById(R.id.edt_input_text);
        baseAudioView.findViewById(R.id.tv_send_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendTextMessage();
            }
        });


    }


    private void setupBaseViewInner() {


        String name = roomInfo.getName();
        name = "房间：" + (TextUtils.isEmpty(name) ? roomInfo.getRoomId() : name) + "（" + roomInfo.getOnlineUserCount() + "人）";

        tvRoomName.setText(name);

        rcyQueueList.setLayoutManager(new GridLayoutManager(this, 4));
        queueAdapter = new QueueAdapter(null, this);
        rcyQueueList.setAdapter(queueAdapter);

        queueAdapter.setItemClickListener(itemClickListener);
        queueAdapter.setItemLongClickListener(itemLongClickListener);
    }

    protected void initQueue(List<Entry<String, String>> entries) {
        ArrayList<QueueInfo> queueInfoList = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            QueueInfo queue = new QueueInfo();
            queue.setIndex(i);
            queueInfoList.add(queue);
        }

        if (entries == null) {
            queueAdapter.setItems(queueInfoList);
            return;
        }

        for (Entry<String, String> entry : entries) {
            if (entry.key.startsWith(QueueInfo.QUEUE_KEY_PREFIX)) {
                QueueInfo queueInfo = QueueInfo.fromJson(entry.value);
                if (queueInfo == null) {
                    continue;
                }
                queueInfoList.set(queueInfo.getIndex(), queueInfo);
            }

        }
        queueAdapter.setItems(queueInfoList);
    }


    private void sendTextMessage() {
        String content = edtInput.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            ToastHelper.showToast("请输入消息内容");
            return;
        }
        ChatRoomMessage chatRoomMessage = ChatRoomMessageBuilder.createChatRoomTextMessage(roomInfo.getRoomId(), content);
        chatRoomService.sendMessage(chatRoomMessage, false).setCallback(new RequestCallback<Void>() {
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
        //todo update  update rcyChatMsgList


    }


    protected void messageInComing(ChatRoomMessage message) {
        if (message.getMsgType() != MsgTypeEnum.text) {

            return;
        }
        Log.i(TAG, "messageInComing ，nick =  " + message.getFromNick() + " , content = " + message.getContent());


        //todo  update rcyChatMsgList 聊天室消息


    }


    public void enterChatRoom(String roomId) {
        AccountInfo accountInfo = DemoCache.getAccountInfo();
        EnterChatRoomData roomData = new EnterChatRoomData(roomId);
        roomData.setAvatar(accountInfo.avatar);
        roomData.setNick(accountInfo.nick);
        chatRoomService.enterChatRoomEx(roomData, 2).setCallback(new RequestCallback<EnterChatRoomResultData>() {
            @Override
            public void onSuccess(EnterChatRoomResultData resultData) {
                enterRoomSuccess(resultData);
            }

            @Override
            public void onFailed(int i) {
                ToastHelper.showToast("进入聊天室失败 ， code = " + i);
            }

            @Override
            public void onException(Throwable throwable) {
                ToastHelper.showToast("进入聊天室异常 ，  e = " + throwable);
            }
        });

    }

    protected void enterRoomSuccess(EnterChatRoomResultData resultData) {

        chatRoomService.fetchQueue(roomInfo.getRoomId()).setCallback(new RequestCallback<List<Entry<String, String>>>() {
            @Override
            public void onSuccess(List<Entry<String, String>> entries) {
                initQueue(entries);
            }

            @Override
            public void onFailed(int i) {
                ToastHelper.showToast("获取队列失败 ，  code = " + i);
            }

            @Override
            public void onException(Throwable throwable) {
                ToastHelper.showToast("获取队列异常，  e = " + throwable);
            }
        });


    }


    @Override
    protected void registerObserver(boolean register) {
        super.registerObserver(register);

        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(customNotification, register);
        NIMClient.getService(ChatRoomServiceObserver.class).observeReceiveMessage(messageObserver, register);
    }


    protected abstract int getContentViewID();

    protected abstract void setupBaseView();

    protected abstract void onQueueItemClick(QueueInfo model, int position);

    protected abstract boolean onQueueItemLongClick(QueueInfo model, int position);

    protected abstract void receiveNotification(CustomNotification customNotification);


}
