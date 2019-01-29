package com.netease.audioroom.demo.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.netease.audioroom.demo.R;

public class TipsDialog extends DialogFragment {
    View mConentView;

    TextView content;
    TextView tips;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(android.app.DialogFragment.STYLE_NO_TITLE, R.style.create_dialog_fragment);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mConentView = inflater.inflate(R.layout.dialog_tips, container, false);
        return mConentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        initView();
        initListener();
    }

    private void initView() {
        content = mConentView.findViewById(R.id.content);
        tips = mConentView.findViewById(R.id.tips);
    }

    private void initListener() {
        tips.setOnClickListener(v -> dismiss());
    }

    public void setTips(String s) {
        tips.setText(s);
    }

}
