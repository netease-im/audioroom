package com.netease.audioroom.demo.dialog;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.netease.audioroom.demo.R;

public class TopTipsDialog extends DialogFragment {

    public static final String TOPTIPSDIALOG = "TopTipsDialog";

    View view;

    TextView content;

    String tips;

    public interface IClickListener {
        void onClickLister();
    }

    IClickListener clickListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(android.app.DialogFragment.STYLE_NO_TITLE, R.style.request_dialog_fragment);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            tips = getArguments().getString(TOPTIPSDIALOG);
        } else {
            dismiss();
        }
        view = inflater.inflate(R.layout.dialog_top_tips, container, false);
        // 设置宽度为屏宽、靠近屏幕底部。
        Window window = getDialog().getWindow();
        window.setBackgroundDrawableResource(R.color.color_e61D1D24);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.TOP;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initView();
    }


    private void initView() {
        content = view.findViewById(R.id.content);
        content.setText(Html.fromHtml(tips));
        content.setOnClickListener(v -> clickListener.onClickLister());
    }


    public void setClickListener(IClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public TextView getContent() {
        return content;
    }
}
