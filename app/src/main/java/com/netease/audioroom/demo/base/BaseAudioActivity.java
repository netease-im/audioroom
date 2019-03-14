package com.netease.audioroom.demo.base;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.audioroom.demo.R;
import com.netease.audioroom.demo.adapter.MessageListAdapter;
import com.netease.audioroom.demo.adapter.QueueAdapter;
import com.netease.audioroom.demo.base.adapter.BaseAdapter;
import com.netease.audioroom.demo.cache.DemoCache;
import com.netease.audioroom.demo.cache.RoomMemberCache;
import com.netease.audioroom.demo.dialog.TipsDialog;
import com.netease.audioroom.demo.model.AccountInfo;
import com.netease.audioroom.demo.model.DemoRoomInfo;
import com.netease.audioroom.demo.model.QueueInfo;
import com.netease.audioroom.demo.model.QueueMember;
import com.netease.audioroom.demo.model.SimpleAVChatStateObserver;
import com.netease.audioroom.demo.model.SimpleMessage;
import com.netease.audioroom.demo.util.CommonUtil;
import com.netease.audioroom.demo.util.Network;
import com.netease.audioroom.demo.util.ScreenUtil;
import com.netease.audioroom.demo.util.ToastHelper;
import com.netease.audioroom.demo.widget.RippleImageView;
import com.netease.audioroom.demo.widget.VerticalItemDecoration;
import com.netease.audioroom.demo.widget.unitepage.loadsir.callback.ErrorCallback;
import com.netease.audioroom.demo.widget.unitepage.loadsir.callback.NetErrCallback;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.AVChatStateObserver;
import com.netease.nimlib.sdk.avchat.constant.AVChatAudioEffectMode;
import com.netease.nimlib.sdk.avchat.constant.AVChatChannelProfile;
import com.netease.nimlib.sdk.avchat.constant.AVChatMediaCodecMode;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.constant.AVChatUserRole;
import com.netease.nimlib.sdk.avchat.model.AVChatChannelInfo;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatParameters;
import com.netease.nimlib.sdk.chatroom.ChatRoomMessageBuilder;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.ChatRoomServiceObserver;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomInfo;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomKickOutEvent;
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
import com.netease.nimlib.sdk.msg.constant.ChatRoomQueueChangeType;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.NotificationType;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.util.Entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 主播与观众基础页，包含所有的通用UI元素
 */
public abstract class BaseAudioActivity extends BaseActivity implements ViewTreeObserver.OnGlobalLayoutListener {
    public static final String ROOM_INFO_KEY = "room_info_key";
    public static final String TAG = "AudioRoom";
    public static final String ROOM_INFO_KEY_MICROPHONE = "room_info_key_microphone";
    protected int channelProfile = AVChatChannelProfile.CHANNEL_PROFILE_DEFAULT;

    private static final int KEY_BOARD_MIN_SIZE = ScreenUtil.dip2px(DemoCache.getContext(), 80);

    //主播基础信息
    protected RippleImageView ivLiverAvatar;
    protected ImageView ivLiverAudioCloseHint;
    protected TextView tvLiverNick;
    protected TextView tvRoomName;

    // 各种控制开关
    protected ImageView ivMuteOtherText;
    protected ImageView ivSelfAudioSwitch;
    protected ImageView ivRoomAudioSwitch;
    protected ImageView ivCancelLink;
    protected ImageView ivExistRoom;

    protected EditText edtInput;
    protected TextView sendButton;

    //自己的麦位，只有观众有
    protected QueueInfo selfQueue;

    //聊天室队列（麦位）
    protected RecyclerView rcyQueueRecyclerView;

    protected QueueAdapter queueAdapter;

    //消息列表
    protected RecyclerView rcyChatMsgList;
    private LinearLayoutManager msgLayoutManager;
    protected MessageListAdapter msgAdapter;

    // 聊天室信息
    protected DemoRoomInfo roomInfo;

    // 聊天室服务
    protected ChatRoomService chatRoomService;
    private AVChatStateObserver stateObserver;

    //音视频接口
    protected long audioUid;

    private int rootViewVisibleHeight;
    private View rootView;
    ArrayList<QueueInfo> queueInfoList;

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
            StringBuffer logInfo = new StringBuffer();
            for (ChatRoomMessage message : chatRoomMessages) {
                if (message.getSessionType() != SessionTypeEnum.ChatRoom
                        || !TextUtils.equals(message.getSessionId(), roomInfo.getRoomId())) {
                    continue;
                }
                if (message.getAttachment() instanceof ChatRoomNotificationAttachment) {
                    NotificationType type = ((ChatRoomNotificationAttachment) message.getAttachment()).getType();
                    switch (type) {
                        // 成员进入聊天室 , 自己进来也有通知
                        case ChatRoomMemberIn:
                            ChatRoomRoomMemberInAttachment memberIn = (ChatRoomRoomMemberInAttachment) message.getAttachment();
                            logInfo.append("成员进入聊天室：nick =  ").append(memberIn.getOperatorNick())
                                    .append(", account = ").append(memberIn.getOperator());
                            memberIn(memberIn);
                            updateRoonInfo();
                            break;
                        // 成员退出聊天室
                        case ChatRoomMemberExit:
                            ChatRoomQueueChangeAttachment memberExit = (ChatRoomQueueChangeAttachment) message.getAttachment();
                            logInfo.append("成员退出聊天室：nick = ").append(memberExit.getOperatorNick()).
                                    append(",  account = ").append(memberExit.getOperator());
                            memberExit(memberExit);

                            updateRoonInfo();
                            break;
                        //成员被禁言
                        case ChatRoomMemberTempMuteAdd:
                            ChatRoomTempMuteAddAttachment addMuteMember = (ChatRoomTempMuteAddAttachment) message.getAttachment();
                            logInfo.append("成员被禁言：nick list =  ").append(addMuteMember.getTargetNicks()).
                                    append(" , account list = ").append(addMuteMember.getTargets());
                            memberMuteAdd(addMuteMember);
                            mute();
                            break;
                        //成员被解除禁言
                        case ChatRoomMemberTempMuteRemove:
                            ChatRoomTempMuteRemoveAttachment muteRemove = (ChatRoomTempMuteRemoveAttachment) message.getAttachment();
                            logInfo.append("成员被解除禁言：nick list =  ").append(muteRemove.getTargetNicks()).
                                    append(" , account list = ").append(muteRemove.getTargets());
                            cancelMute();
                            memberMuteRemove(muteRemove);
                            break;
                        //队列变更
                        case ChatRoomQueueChange:
                            ChatRoomQueueChangeAttachment queueChange = (ChatRoomQueueChangeAttachment) message.getAttachment();
                            logInfo.append("队列变更：type = ").append(queueChange.getChatRoomQueueChangeType())
                                    .append(", key = ").append(queueChange.getKey())
                                    .append(", value = ").append(queueChange.getContent());
                            onQueueChange(queueChange);
                            break;
                        //队列批量变更，好像没用了
                        case ChatRoomQueueBatchChange:
                            ChatRoomPartClearAttachment queuePartClear = (ChatRoomPartClearAttachment) message.getAttachment();
                            logInfo.append("队列批量变更：").append(queuePartClear.getChatRoomQueueChangeType());
                            for (String key : queuePartClear.getContentMap().keySet()) {
                                logInfo.append("key = " + key + ", value= " + queuePartClear.getContentMap().get(key)).append(" ");
                            }
                            break;
                        case ChatRoomInfoUpdated:
                            NIMClient.getService(ChatRoomService.class).fetchRoomInfo(roomInfo.getRoomId())
                                    .setCallback(new RequestCallback<ChatRoomInfo>() {
                                        @Override
                                        public void onSuccess(ChatRoomInfo param) {
                                            // 成功
                                            if (param.getExtension() != null) {
                                                for (Map.Entry<String, Object> entry : param.getExtension().entrySet()) {
                                                    if (entry.getKey().equals(ROOM_INFO_KEY_MICROPHONE)) {
                                                        if ((Boolean) entry.getValue()) {
                                                            ivLiverAvatar.startWaveAnimation();
                                                        } else {
                                                            ivLiverAvatar.stopWaveAnimation();
                                                        }
                                                        break;
                                                    }
                                                }
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

                    }
                } else {
                    messageInComing(message);
                }
            }
            if (logInfo.length() > 0) {
                Log.i(TAG, logInfo.toString());
            }
        }
    };
    //被踢出通知
    Observer<ChatRoomKickOutEvent> kickOutObserver = new Observer<ChatRoomKickOutEvent>() {
        @Override
        public void onEvent(ChatRoomKickOutEvent chatRoomKickOutEvent) {
            TipsDialog tipsDialog = new TipsDialog();
            Bundle bundle = new Bundle();
            bundle.putString(tipsDialog.TAG, "该房间已被主播解散");
            tipsDialog.setArguments(bundle);
            tipsDialog.show(getSupportFragmentManager(), tipsDialog.TAG);
            tipsDialog.setClickListener(() -> {
                tipsDialog.dismiss();
                release();
            });
        }
    };


    @Override
    protected void initViews() {
        findBaseView();
        roomInfo = (DemoRoomInfo) getIntent().getSerializableExtra(ROOM_INFO_KEY);
        if (roomInfo == null) {
            ToastHelper.showToast("聊天室信息不能为空");
            finish();
        }
        chatRoomService = NIMClient.getService(ChatRoomService.class);
        AVChatManager.getInstance().createRoom(roomInfo.getRoomId(), CommonUtil.readAppKey(), new AVChatCallback<AVChatChannelInfo>() {
            @Override
            public void onSuccess(AVChatChannelInfo avChatChannelInfo) {

            }

            @Override
            public void onFailed(int code) {

            }

            @Override
            public void onException(Throwable exception) {

            }
        });
        audioUid = System.nanoTime();
        setupBaseViewInner();
        setupBaseView();
        rootView = getWindow().getDecorView();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(BaseAudioActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Network.getInstance().isConnected()) {
            loadService.showSuccess();
        } else {
            loadService.showCallback(NetErrCallback.class);
        }
    }

    protected void memberMuteRemove(ChatRoomTempMuteRemoveAttachment muteRemove) {
    }

    protected void memberMuteAdd(ChatRoomTempMuteAddAttachment addMuteMember) {
    }

    protected void mute() {

    }

    protected void cancelMute() {

    }

    protected void memberExit(ChatRoomQueueChangeAttachment memberExit) {
        ArrayList<String> exitNicks = memberExit.getTargetNicks();
        if (CommonUtil.isEmpty(exitNicks)) {
            return;
        }
        for (String nick : exitNicks) {
            SimpleMessage simpleMessage = new SimpleMessage("", "“" + nick + "”离开了房间", SimpleMessage.TYPE_MEMBER_CHANGE);
            msgAdapter.appendItem(simpleMessage);
            updateRoonInfo();
        }
        scrollToBottom();
    }

    protected void memberIn(ChatRoomRoomMemberInAttachment memberIn) {
        ArrayList<String> inNicks = memberIn.getTargetNicks();
        if (CommonUtil.isEmpty(inNicks)) {
            return;
        }
        for (String nick : inNicks) {
            SimpleMessage simpleMessage = new SimpleMessage("", "“" + nick + "”进了房间", SimpleMessage.TYPE_MEMBER_CHANGE);
            msgAdapter.appendItem(simpleMessage);
            updateRoonInfo();
        }
        scrollToBottom();
    }


    @Override
    protected void onDestroy() {
        if (rootView != null) {
            rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
        if (stateObserver != null) {
            AVChatManager.getInstance().observeAVChatState(stateObserver, false);
        }
        super.onDestroy();
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
        ivSelfAudioSwitch = baseAudioView.findViewById(R.id.iv_close_self_audio_switch);
        ivRoomAudioSwitch = baseAudioView.findViewById(R.id.iv_close_room_audio_switch);
        ivCancelLink = baseAudioView.findViewById(R.id.iv_cancel_link);
        ivExistRoom = baseAudioView.findViewById(R.id.iv_exist_room);


        rcyQueueRecyclerView = baseAudioView.findViewById(R.id.rcy_queue_list);
        rcyChatMsgList = baseAudioView.findViewById(R.id.rcy_chat_message_list);

        edtInput = baseAudioView.findViewById(R.id.edt_input_text);
        sendButton = baseAudioView.findViewById(R.id.tv_send_text);
        sendButton.setOnClickListener((view) -> sendTextMessage());


    }

    private void setupBaseViewInner() {
        String name = roomInfo.getName();
        name = "房间：" + (TextUtils.isEmpty(name) ? roomInfo.getRoomId() : name) + "（" + roomInfo.getOnlineUserCount() + "人）";
        tvRoomName.setText(name);

        rcyQueueRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        queueAdapter = new QueueAdapter(null, this);
        rcyQueueRecyclerView.setAdapter(queueAdapter);

        queueAdapter.setItemClickListener(itemClickListener);
        queueAdapter.setItemLongClickListener(itemLongClickListener);

        msgLayoutManager = new LinearLayoutManager(this);
        rcyChatMsgList.setLayoutManager(msgLayoutManager);
        msgAdapter = new MessageListAdapter(null, this);
        rcyChatMsgList.addItemDecoration(new VerticalItemDecoration(Color.TRANSPARENT, ScreenUtil.dip2px(this, 9)));
        rcyChatMsgList.setAdapter(msgAdapter);
    }

    protected void initQueue(List<Entry<String, String>> entries) {
        queueInfoList = new ArrayList<>();
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
            if (TextUtils.isEmpty(entry.key) || !entry.key.startsWith(QueueInfo.QUEUE_KEY_PREFIX)) {
                continue;
            }
            if (TextUtils.isEmpty(entry.value)) {
                continue;
            }
            QueueInfo queueInfo = new QueueInfo(entry.value);
            queueInfoList.set(queueInfo.getIndex(), queueInfo);
            QueueMember member = queueInfo.getQueueMember();
            if (member != null && TextUtils.equals(member.getAccount(), DemoCache.getAccountId())) {
                selfQueue = queueInfo;
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
        chatRoomService.sendMessage(chatRoomMessage, false);
        msgAdapter.appendItem(new SimpleMessage(DemoCache.getAccountInfo().nick, content, SimpleMessage.TYPE_NORMAL_MESSAGE));
        edtInput.setText("");
    }


    protected void messageInComing(ChatRoomMessage message) {
        if (message.getMsgType() != MsgTypeEnum.text) {
            return;
        }

        msgAdapter.appendItem(new SimpleMessage(message.getChatRoomMessageExtension().getSenderNick(),
                message.getContent(),
                SimpleMessage.TYPE_NORMAL_MESSAGE));
        scrollToBottom();
    }


    public void enterChatRoom(String roomId) {
        AccountInfo accountInfo = DemoCache.getAccountInfo();
        EnterChatRoomData roomData = new EnterChatRoomData(roomId);
        roomData.setAvatar(accountInfo.avatar);
        roomData.setNick(accountInfo.nick);
        chatRoomService.enterChatRoomEx(roomData, 2).setCallback(new RequestCallback<EnterChatRoomResultData>() {
            @Override
            public void onSuccess(EnterChatRoomResultData resultData) {
                loadService.showSuccess();
                enterRoomSuccess(resultData);
            }

            @Override
            public void onFailed(int i) {
                loadService.showCallback(ErrorCallback.class);
                ToastHelper.showToast("进入聊天室失败 ， code = " + i);
                finish();
            }

            @Override
            public void onException(Throwable throwable) {
                loadService.showCallback(ErrorCallback.class);
                ToastHelper.showToast("进入聊天室异常 ，  e = " + throwable);
                finish();
            }
        });

    }

    protected void enterRoomSuccess(EnterChatRoomResultData resultData) {
        chatRoomService.fetchQueue(roomInfo.getRoomId()).setCallback(new RequestCallback<List<Entry<String, String>>>() {
            @Override
            public void onSuccess(List<Entry<String, String>> entries) {
                loadService.showSuccess();
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
        NIMClient.getService(ChatRoomServiceObserver.class).observeKickOutEvent(kickOutObserver, register);
    }


    /**
     * 关闭自己的语音
     */
    protected void muteSelfAudio() {
        if (AVChatManager.getInstance().isLocalAudioMuted()) {
            AVChatManager.getInstance().muteLocalAudio(false);
        } else {
            AVChatManager.getInstance().muteLocalAudio(true);
        }


    }

    /**
     * 关闭聊天室的语音
     */
    protected void muteRoomAudio(boolean isMutex) {
        AVChatManager.getInstance().muteAllRemoteAudio(isMutex);
    }


    /**
     * 离开聊天室
     */
    protected void release() {
        AVChatManager.getInstance().leaveRoom2(roomInfo.getRoomId(), new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                AVChatManager.getInstance().disableRtc();
                //退出
                if (roomInfo != null) {
                    RoomMemberCache.getInstance().removeCache(roomInfo.getRoomId());
                    chatRoomService.exitChatRoom(roomInfo.getRoomId());
                    roomInfo = null;
                }
                finish();
            }

            @Override
            public void onFailed(int code) {
//                ToastHelper.showToast("解散失败code：" + code);
                finish();
            }

            @Override
            public void onException(Throwable exception) {
//                ToastHelper.showToast("解散失败code：" + exception.getMessage());
                finish();
            }
        });

    }

    private void scrollToBottom() {
        msgLayoutManager.scrollToPosition(msgAdapter.getItemCount() - 1);
    }


    protected abstract int getContentViewID();

    protected abstract void setupBaseView();

    protected abstract void onQueueItemClick(QueueInfo model, int position);

    protected abstract boolean onQueueItemLongClick(QueueInfo model, int position);

    protected abstract void receiveNotification(CustomNotification customNotification);

    protected abstract void exitRoom();


    protected void onQueueChange(ChatRoomQueueChangeAttachment queueChange) {
        ChatRoomQueueChangeType changeType = queueChange.getChatRoomQueueChangeType();
        // 队列被清空
        if (changeType == ChatRoomQueueChangeType.DROP) {
            initQueue(null);
            return;
        }
        String value = queueChange.getContent();
        //新增元素或更新
        if (changeType == ChatRoomQueueChangeType.OFFER && !TextUtils.isEmpty(value)) {
            QueueInfo queueInfo = new QueueInfo(value);
            queueAdapter.updateItem(queueInfo.getIndex(), queueInfo);
            return;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public void onGlobalLayout() {
        int preHeight = rootViewVisibleHeight;
        //获取当前根视图在屏幕上显示的大小
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        rootViewVisibleHeight = r.height();

        if (preHeight == 0 || preHeight == rootViewVisibleHeight) {
            return;
        }
        //根视图显示高度变大超过KEY_BOARD_MIN_SIZE，可以看作软键盘隐藏了
        if (rootViewVisibleHeight - preHeight >= KEY_BOARD_MIN_SIZE) {
            scrollToBottom();
            return;
        }
    }

    protected void joinChannel() {
        AVChatManager.getInstance().enableRtc();
        //设置场景, 如果需要高清音乐场景，设置 AVChatChannelProfile#CHANNEL_PROFILE_HIGH_QUALITY_MUSIC
        AVChatManager.getInstance().setChannelProfile(channelProfile);
        //设置通话可选参数
        try {
            AVChatParameters parameters = getRtcParameters();
            AVChatManager.getInstance().setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
            ToastHelper.showToast("参数设置错误");
            loadService.showCallback(ErrorCallback.class);
        }

        AVChatManager.getInstance().joinRoom2(roomInfo.getRoomId(), AVChatType.AUDIO, new AVChatCallback<AVChatData>() {
            @Override
            public void onSuccess(AVChatData avChatData) {
                AVChatManager.getInstance().setSpeaker(true);//使用扬声器
            }

            @Override
            public void onFailed(int code) {
                ToastHelper.showToast("加入聊天室失败" + code);
                Log.e(TAG, "加入聊天室失败" + roomInfo.getRoomId());
            }

            @Override
            public void onException(Throwable exception) {
                ToastHelper.showToast("加入聊天室失败" + exception.getMessage());
                Log.e(TAG, "加入聊天室失败" + roomInfo.getRoomId());
            }
        });

        // state observer
        if (stateObserver != null) {
            AVChatManager.getInstance().observeAVChatState(stateObserver, false);
        }
        stateObserver = new SimpleAVChatStateObserver() {

            @Override
            public void onReportSpeaker(Map<String, Integer> speakers, int mixedEnergy) {
                if (queueInfoList != null) {
                    onAudioVolume(speakers);
                }

            }
        };
        AVChatManager.getInstance().observeAVChatState(stateObserver, true);
    }

    private void updateRoonInfo() {
        String name = roomInfo.getName();
        name = "房间：" + (TextUtils.isEmpty(name) ? roomInfo.getRoomId() : name) + "（" + roomInfo.getOnlineUserCount() + "人）";
        tvRoomName.setText(name);
    }


    private AVChatParameters getRtcParameters() throws Exception {
        AVChatParameters parameters = new AVChatParameters();
        parameters.setBoolean(AVChatParameters.KEY_SERVER_AUDIO_RECORD, false);
        parameters.setBoolean(AVChatParameters.KEY_VIDEO_ROTATE_IN_RENDING, true);
        parameters.setString(AVChatParameters.KEY_AUDIO_EFFECT_ACOUSTIC_ECHO_CANCELER,
                AVChatAudioEffectMode.PLATFORM_BUILTIN);
        parameters.setString(AVChatParameters.KEY_AUDIO_EFFECT_NOISE_SUPPRESSOR,
                AVChatAudioEffectMode.PLATFORM_BUILTIN);
        parameters.setString(AVChatParameters.KEY_VIDEO_DECODER_MODE,
                AVChatMediaCodecMode.MEDIA_CODEC_SOFTWARE);
        parameters.setBoolean(AVChatParameters.KEY_SERVER_VIDEO_RECORD, false);
        parameters.setInteger(AVChatParameters.KEY_SERVER_RECORD_MODE, 0);
        parameters.setBoolean(AVChatParameters.KEY_AUDIO_CALL_PROXIMITY, true);
        parameters.setBoolean(AVChatParameters.KEY_AUDIO_EXTERNAL_CAPTURE, false);

        parameters.setBoolean(AVChatParameters.KEY_AUDIO_HIGH_QUALITY, false);
        parameters.setBoolean(AVChatParameters.KEY_AUDIO_CALL_PROXIMITY, true);
        parameters.setBoolean(AVChatParameters.KEY_SESSION_LIVE_MODE, false);
        parameters.setInteger(AVChatParameters.KEY_DEVICE_DEFAULT_ROTATION, 0);

        parameters.setBoolean(AVChatParameters.KEY_AUDIO_FRAME_FILTER, true);


        parameters.setString(AVChatParameters.KEY_AUDIO_EFFECT_AUTOMATIC_GAIN_CONTROL,
                AVChatAudioEffectMode.PLATFORM_BUILTIN);
        parameters.setInteger(AVChatParameters.KEY_SESSION_MULTI_MODE_USER_ROLE, AVChatUserRole.NORMAL);
        return parameters;
    }

    private void onAudioVolume(Map<String, Integer> speakers) {
        for (QueueInfo item : queueInfoList) {
            if (speakers.containsKey(item.getKey())) {
                if (speakers.get(item.getQueueMember().getAccount()) == 0) {
                    queueAdapter.updateVolume(item, false);
                } else {
                    queueAdapter.updateVolume(item, true);
                }

            }
        }
    }


}
