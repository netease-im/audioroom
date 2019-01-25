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
import com.netease.audioroom.demo.model.RequestMember;
import com.netease.audioroom.demo.util.CommonUtil;
import com.netease.audioroom.demo.widget.HeadImageView;

import java.util.ArrayList;

public class RequestlinkAdapter extends BaseAdapter<RequestMember> {
    public interface IRequestAction {
        void refuse(RequestMember request);

        void agree(RequestMember request);

    }

    IRequestAction requestAction;
    ArrayList<RequestMember> queueMemberList;

    public RequestlinkAdapter(ArrayList<RequestMember> queueMemberList, Context context) {
        super(queueMemberList, context);
        this.queueMemberList = queueMemberList;
    }


    @Override
    protected RecyclerView.ViewHolder onCreateBaseViewHolder(ViewGroup parent, int viewType) {
        return new QueueViewHolder(layoutInflater.inflate(R.layout.item_requestlink, parent, false));
    }

    @Override
    protected void onBindBaseViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RequestMember queueMember = getItem(position);
        if (queueMember == null) {
            return;
        }
        QueueViewHolder viewHolder = (QueueViewHolder) holder;
        QueueMember member = queueMember.getQueueMember();
        CommonUtil.loadImage(context, member.getAvatar(), viewHolder.ivAvatar, R.drawable.chat_room_default_bg, 0);
        viewHolder.tvContent.setText(member.getNick() + "\t申请麦位(" + queueMember.getIndex() + 1 + ")");
        viewHolder.ivRefuse.setOnClickListener((v) -> requestAction.refuse(queueMember));
        viewHolder.ivAfree.setOnClickListener((v) -> requestAction.agree(queueMember));
    }

    private class QueueViewHolder extends RecyclerView.ViewHolder {
        HeadImageView ivAvatar;
        ImageView ivRefuse;
        ImageView ivAfree;
        TextView tvContent;

        public QueueViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.item_requestlink_headicon);
            ivRefuse = itemView.findViewById(R.id.refuse);
            ivAfree = itemView.findViewById(R.id.agree);
            tvContent = itemView.findViewById(R.id.item_requestlink_content);
        }
    }

    public void setRequestAction(IRequestAction requestAction) {
        this.requestAction = requestAction;
    }
    
}
