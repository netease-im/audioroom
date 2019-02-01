package com.netease.audioroom.demo.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.netease.audioroom.demo.R;

public class TipsDialog extends DialogFragment {
    public final static String TIPSDIALOG = "TipsDialog";
    View mConentView;

    TextView tvContent;
    TextView tvTips;
    String content;

    public interface IClickListener {
        void onClick();
    }

    IClickListener clickListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(android.app.DialogFragment.STYLE_NO_TITLE, R.style.create_dialog_fragment);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        if (getArguments() != null) {
            content = getArguments().getString(TIPSDIALOG);
        }

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
        tvContent = mConentView.findViewById(R.id.content);
        tvTips = mConentView.findViewById(R.id.tips);
        if (TextUtils.isEmpty(content))
            tvContent.setText(content);
    }

    private void initListener() {
        tvTips.setOnClickListener(v -> clickListener.onClick());
    }

    public void setClickListener(IClickListener clickListener) {
        this.clickListener = clickListener;
    }
}
