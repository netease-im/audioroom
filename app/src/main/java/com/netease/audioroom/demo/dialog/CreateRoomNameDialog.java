package com.netease.audioroom.demo.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.netease.audioroom.demo.R;
import com.netease.audioroom.demo.activity.AudioLiveActivity;
import com.netease.audioroom.demo.cache.DemoCache;
import com.netease.audioroom.demo.http.ChatRoomHttpClient;
import com.netease.audioroom.demo.model.DemoRoomInfo;
import com.netease.audioroom.demo.util.ToastHelper;

public class CreateRoomNameDialog extends BaseDialogFragment {

    View mConentView;

    EditText mEditText;
    Button mBtnCancal;
    Button mBtnCreaterRoom;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(android.app.DialogFragment.STYLE_NO_TITLE, R.style.create_dialog_fragment);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mConentView = inflater.inflate(R.layout.dialog_creater_roomname, container, false);
        return mConentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        initView();
        initListener();
    }

    private void initView() {
        mEditText = mConentView.findViewById(R.id.eturl);
        mBtnCancal = mConentView.findViewById(R.id.btnCancal);
        mBtnCreaterRoom = mConentView.findViewById(R.id.btnCreaterRoom);
        mBtnCreaterRoom.setEnabled(false);

    }

    @Override
    public int show(FragmentTransaction transaction, String tag) {
        return super.show(transaction, tag);
    }

    private void initListener() {
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s)) {
                    mBtnCreaterRoom.setEnabled(true);
                    mBtnCreaterRoom.setTextColor(getContext().getResources().getColor(R.color.color_2799ff));
                } else {
                    mBtnCreaterRoom.setEnabled(false);
                    mBtnCreaterRoom.setTextColor(getContext().getResources().getColor(R.color.color_8fb5e1));
                }
            }
        });
        mBtnCancal.setOnClickListener((v) -> dismiss());
        mBtnCreaterRoom.setOnClickListener((v) -> createRoom(mEditText.getText().toString()));
    }



    //创建房间
    private void createRoom(String roomName) {
        ChatRoomHttpClient.getInstance().createRoom(DemoCache.getAccountId(), roomName,
                new ChatRoomHttpClient.ChatRoomHttpCallback<DemoRoomInfo>() {
            @Override
            public void onSuccess(DemoRoomInfo roomInfo) {
                if (roomInfo != null) {
                    mEditText.setText("");
                    dismiss();
                    AudioLiveActivity.start(getContext(), roomInfo);
                } else {
                    ToastHelper.showToast("创建房间失败，返回信息为空");
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                ToastHelper.showToast("创建房间失败");
            }
        });
    }

}
