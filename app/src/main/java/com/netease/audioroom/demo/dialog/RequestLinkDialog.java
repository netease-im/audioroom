package com.netease.audioroom.demo.dialog;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.netease.audioroom.demo.R;
import com.netease.audioroom.demo.adapter.RequestlinkAdapter;
import com.netease.audioroom.demo.model.RequestMember;

import java.util.ArrayList;

public class RequestLinkDialog extends DialogFragment {
    public static final String QUEUEINFOLIST = "queueInfoList";

    RecyclerView requesterRecyclerView;
    RequestlinkAdapter adapter;

    ArrayList<RequestMember> queueMemberList;
    View view;

    public interface IRequestAction {
        void refuse(RequestMember request);

        void agree(RequestMember request);

    }

    IRequestAction requestAction;

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
            queueMemberList = getArguments().getParcelableArrayList(QUEUEINFOLIST);
        } else {
            dismiss();
        }

        view = inflater.inflate(R.layout.dialog_requestlink, container, false);
        // 设置宽度为屏宽、靠近屏幕底部。
        final Window window = getDialog().getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.TOP;
        wlp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initView();
        initListener();

    }


    private void initView() {
        requesterRecyclerView = view.findViewById(R.id.requesterRecyclerView);
        requesterRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        buidHeadView();
    }

    private void buidHeadView() {
//        TextView headView = new TextView(getActivity());
//        headView.setText("申请上麦（" + queueMemberList.size() + "）");
//        headView.setGravity(Gravity.CENTER);
//        requesterRecyclerView.addView(headView, 0);
        adapter = new RequestlinkAdapter(queueMemberList, getActivity());
        requesterRecyclerView.setAdapter(adapter);

    }

    public void initListener() {
        adapter.setRequestAction(new RequestlinkAdapter.IRequestAction() {
            @Override
            public void refuse(RequestMember request) {
                requestAction.refuse(request);
            }

            @Override
            public void agree(RequestMember request) {
                requestAction.agree(request);
            }
        });

    }

    public void setRequestAction(IRequestAction requestAction) {
        this.requestAction = requestAction;
    }
}
