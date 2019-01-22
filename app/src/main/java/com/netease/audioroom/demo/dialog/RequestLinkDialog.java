package com.netease.audioroom.demo.dialog;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.netease.audioroom.demo.R;
import com.netease.audioroom.demo.adapter.RequestlinkAdapter;
import com.netease.audioroom.demo.base.BaseAdapter;
import com.netease.audioroom.demo.model.QueueMember;

import java.util.ArrayList;

public class RequestLinkDialog extends DialogFragment {
    public static final String QUEUEINFOLIST = "queueInfoList";

    RecyclerView requesterRecyclerView;
    RequestlinkAdapter adapter;

    ArrayList<QueueMember> queueMemberList;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(android.app.DialogFragment.STYLE_NO_TITLE, R.style.request_dialog_fragment);

    }

    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_requestlink, container, false);
        return view;
    }


    private void initView() {
        requesterRecyclerView = view.findViewById(R.id.requesterRecyclerView);
        adapter = new RequestlinkAdapter(queueMemberList, getActivity());
    }

    private void buidHeadView(int num) {
        TextView headView = new TextView(getActivity());
        headView.setText("申请上麦（" + num + "）");

    }


    private void initLisener() {
        adapter.setItemClickListener(new BaseAdapter.ItemClickListener<QueueMember>() {
            @Override
            public void onItemClick(QueueMember model, int position) {

            }
        });
    }

    public void updateData() {

    }


}
