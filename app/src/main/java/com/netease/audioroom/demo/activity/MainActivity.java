package com.netease.audioroom.demo.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.netease.audioroom.demo.R;
import com.netease.audioroom.demo.adapter.ChatRoomListAdapter;
import com.netease.audioroom.demo.base.BaseActivity;
import com.netease.audioroom.demo.base.BaseAdapter;
import com.netease.audioroom.demo.base.BaseAudioActivity;
import com.netease.audioroom.demo.cache.DemoCache;
import com.netease.audioroom.demo.http.ChatRoomHttpClient;
import com.netease.audioroom.demo.model.AccountInfo;
import com.netease.audioroom.demo.model.DemoRoomInfo;
import com.netease.audioroom.demo.util.ScreenUtil;
import com.netease.audioroom.demo.util.ToastHelper;
import com.netease.audioroom.demo.widget.HeadImageView;
import com.netease.audioroom.demo.widget.VerticalItemDecoration;
import com.netease.audioroom.demo.widget.unitepage.loadsir.callback.EmptyCallback;
import com.netease.audioroom.demo.widget.unitepage.loadsir.callback.ErrorCallback;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements View.OnClickListener, BaseAdapter.ItemClickListener<DemoRoomInfo> {


    private static final String TAG = "MainActivity";
    private HeadImageView ivAvatar;
    private TextView tvNick;
    private ChatRoomListAdapter chatRoomListAdapter;

    private StatusCode loginStatus = StatusCode.UNLOGIN;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupView();
        tryLogin();
        fetchChatRoomList();

    }


    private void setupView() {
        ivAvatar = findViewById(R.id.iv_self_avatar);
        tvNick = findViewById(R.id.tv_self_nick);
        RecyclerView rcyChatList = findViewById(R.id.rcy_chat_room_list);
        findViewById(R.id.iv_create_chat_room).setOnClickListener(this);
        rcyChatList.setLayoutManager(new LinearLayoutManager(this));
        chatRoomListAdapter = new ChatRoomListAdapter(null, this);
        // 每个item 16dp 的间隔
        rcyChatList.addItemDecoration(new VerticalItemDecoration(Color.TRANSPARENT, ScreenUtil.dip2px(this, 16)));
        rcyChatList.setAdapter(chatRoomListAdapter);
        chatRoomListAdapter.setItemClickListener(this);

    }

    private void tryLogin() {
        final AccountInfo accountInfo = DemoCache.getAccountInfo();
        if (accountInfo == null) {
            fetchLoginAccount(null);
            return;
        }
        LoginInfo loginInfo = new LoginInfo(accountInfo.account, accountInfo.token);

        NIMClient.getService(AuthService.class).login(loginInfo).setCallback(new RequestCallback() {
            @Override
            public void onSuccess(Object o) {
//
                afterLogin(accountInfo);
            }

            @Override
            public void onFailed(int i) {
                loadService.showCallback(ErrorCallback.class);
                fetchLoginAccount(accountInfo.account);
            }

            @Override
            public void onException(Throwable throwable) {
                loadService.showCallback(ErrorCallback.class);
                fetchLoginAccount(accountInfo.account);
            }
        });

    }

    private void fetchLoginAccount(String preAccount) {
        ChatRoomHttpClient.getInstance().fetchAccount(preAccount, new ChatRoomHttpClient.ChatRoomHttpCallback<AccountInfo>() {
            @Override
            public void onSuccess(AccountInfo accountInfo) {
                login(accountInfo);
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                ToastHelper.showToast("获取登录帐号失败 ， code = " + code);
            }
        });
    }

    private void login(final AccountInfo accountInfo) {
        LoginInfo loginInfo = new LoginInfo(accountInfo.account, accountInfo.token);
        NIMClient.getService(AuthService.class).login(loginInfo).setCallback(new RequestCallback() {
            @Override
            public void onSuccess(Object o) {
                loadService.showCallback(EmptyCallback.class);
                afterLogin(accountInfo);
            }

            @Override
            public void onFailed(int i) {
                loadService.showCallback(ErrorCallback.class);
                ToastHelper.showToast("SDK登录失败 , code = " + i);
            }

            @Override
            public void onException(Throwable throwable) {
                loadService.showCallback(ErrorCallback.class);
                ToastHelper.showToast("SDK登录异常 , e = " + throwable);
            }
        });
    }

    private void afterLogin(AccountInfo accountInfo) {
        ToastHelper.showToast("登录成功");
        DemoCache.setAccountId(accountInfo.account);
        DemoCache.saveAccountInfo(accountInfo);
        ivAvatar.loadAvatar(accountInfo.avatar);
        tvNick.setText(accountInfo.nick);
        Log.i(TAG, "after login  , account = " + accountInfo.account);

    }


    private void fetchChatRoomList() {
        ChatRoomHttpClient.getInstance().fetchChatRoomList(0, 20
                , new ChatRoomHttpClient.ChatRoomHttpCallback<ArrayList<DemoRoomInfo>>() {
                    @Override
                    public void onSuccess(ArrayList<DemoRoomInfo> roomList) {
                        if (roomList.size() > 0) {
                            loadService.showSuccess();
                            chatRoomListAdapter.appendItems(roomList);
                        } else {
                            loadService.showCallback(EmptyCallback.class);
                        }
                    }

                    @Override
                    public void onFailed(int code, String errorMsg) {
                        loadService.showCallback(ErrorCallback.class);
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
        if (loginStatus != StatusCode.LOGINED) {
            ToastHelper.showToast("登录失败，请杀掉APP重新登录");
            return;
        }

        //todo 弹窗

        createChatRoom("测试");

    }

    private void createChatRoom(String roomName) {

        if (TextUtils.isEmpty(roomName) || roomName.trim().length() == 0) {
            ToastHelper.showToast("房间名不能为空");
            return;
        }

//        ChatRoomHttpClient.getInstance().createRoom(DemoCache.getAccountId(), roomName, new ChatRoomHttpClient.ChatRoomHttpCallback<DemoRoomInfo>() {
//            @Override
//            public void onSuccess(DemoRoomInfo roomInfo) {
//                if (roomInfo == null) {
//                    ToastHelper.showToast("创建房间失败，返回信息为空");
//                    return;
//                }
//                if (isActivityPaused()) {
//                    return;
//                }
//
//                startLive(roomInfo);
//            }
//
//            @Override
//            public void onFailed(int code, String errorMsg) {
//                ToastHelper.showToast("创建房间失败");
//            }
//        });


        //todo  测试代码 ，直接改成自己创建的room 的id 即可
        DemoRoomInfo demoRoomInfo = new DemoRoomInfo();
        demoRoomInfo.setRoomId("60929944");
        startLive(demoRoomInfo);


    }

    private void startLive(DemoRoomInfo demoRoomInfo) {
        Intent intent = new Intent(MainActivity.this, AudioLiveActivity.class);
        intent.putExtra(BaseAudioActivity.ROOM_INFO_KEY, demoRoomInfo);
        startActivity(intent);
    }

    @Override
    public void onItemClick(DemoRoomInfo model, int position) {
        if (loginStatus != StatusCode.LOGINED) {
            ToastHelper.showToast("登录失败，请杀掉APP重新登录");
            return;
        }

        //当前帐号创建的房间
        if (TextUtils.equals(DemoCache.getAccountId(), model.getCreator())) {
            startLive(model);
            return;
        }

        Intent intent = new Intent(MainActivity.this, AudienceActivity.class);
        intent.putExtra(BaseAudioActivity.ROOM_INFO_KEY, model);
        startActivity(intent);

    }


    @Override
    protected void onLoginEvent(StatusCode statusCode) {
        loginStatus = statusCode;
    }
}
