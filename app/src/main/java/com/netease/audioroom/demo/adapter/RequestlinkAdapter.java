package com.netease.audioroom.demo.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.audioroom.demo.R;
import com.netease.audioroom.demo.base.BaseAdapter;
import com.netease.audioroom.demo.model.QueueMember;
import com.netease.audioroom.demo.util.CommonUtil;
import com.netease.audioroom.demo.widget.HeadImageView;

import java.util.ArrayList;

public class RequestlinkAdapter extends BaseAdapter<QueueMember> {


    public RequestlinkAdapter(ArrayList<QueueMember> queueInfoList, Context context) {
        super(queueInfoList, context);
    }


    @Override
    protected RecyclerView.ViewHolder onCreateBaseViewHolder(ViewGroup parent, int viewType) {
        return new QueueViewHolder(layoutInflater.inflate(R.layout.item_requestlink, parent, false));
    }

    @Override
    protected void onBindBaseViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        QueueMember queueMember = getItem(position);
        if (queueMember == null) {
            return;
        }
        QueueViewHolder viewHolder = (QueueViewHolder) holder;
        CommonUtil.loadImage(context, queueMember.getAvatar(), viewHolder.ivAvatar, R.drawable.chat_room_default_bg, 0);
        viewHolder.tvContent.setText(queueMember.getNick() + "\t申请麦位" + "");


    }

    private class QueueViewHolder extends RecyclerView.ViewHolder {


        HeadImageView ivAvatar;
        ImageView ivRefuse;
        ImageView ivAfree;
        TextView tvContent;

        public QueueViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.item_requestlink_headicon);
            ivRefuse = itemView.findViewById(R.id.requester);
            ivAfree = itemView.findViewById(R.id.refuse);
            tvContent = itemView.findViewById(R.id.item_requestlink_content);
        }
    }

}
