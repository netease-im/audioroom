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
import com.netease.audioroom.demo.model.QueueInfo;
import com.netease.audioroom.demo.widget.HeadImageView;

import java.util.ArrayList;

public class QueueAdapter extends BaseAdapter<QueueInfo> {


    public QueueAdapter(ArrayList<QueueInfo> queueInfoList, Context context) {
        super(queueInfoList, context);
    }


    @Override
    protected RecyclerView.ViewHolder onCreateBaseViewHolder(ViewGroup parent, int viewType) {
        return new QueueViewHolder(layoutInflater.inflate(R.layout.item_queue_list, parent, false));
    }

    @Override
    protected void onBindBaseViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        QueueInfo queueInfo = getItem(position);
        if (queueInfo == null) {
            return;
        }
        QueueViewHolder viewHolder = (QueueViewHolder) holder;
        int status = queueInfo.getStatus();
        QueueMember queueMember = queueInfo.getQueueMember();

        switch (status) {
            case QueueInfo.STATUS_INIT:
                viewHolder.ivAvatar.setImageResource(R.color.color_292929);
                viewHolder.ivStatusHint.setVisibility(View.GONE);
                viewHolder.iv_user_status.setVisibility(View.VISIBLE);
                viewHolder.iv_user_status.setImageResource(R.drawable.queue_add_member);
                viewHolder.tvNick.setText("麦位" + (queueInfo.getIndex() + 1));
                break;
            case QueueInfo.STATUS_LOAD:
                viewHolder.ivAvatar.setImageResource(R.color.color_292929);
                viewHolder.iv_user_status.setVisibility(View.GONE);
                if (queueInfo.getReason() != QueueInfo.Reason.applyInMute) {
                    viewHolder.ivStatusHint.setVisibility(View.GONE);
                } else {
                    viewHolder.ivStatusHint.setVisibility(View.VISIBLE);
                    viewHolder.ivStatusHint.setImageResource(R.drawable.audio_be_muted_status);
                }
                break;
            case QueueInfo.STATUS_NORMAL:
                viewHolder.iv_user_status.setVisibility(View.GONE);
                viewHolder.ivStatusHint.setVisibility(View.GONE);
                break;
            case QueueInfo.STATUS_CLOSE:
                viewHolder.iv_user_status.setVisibility(View.VISIBLE);
                viewHolder.ivStatusHint.setVisibility(View.GONE);
                viewHolder.iv_user_status.setImageResource(R.drawable.queue_close);
                break;
            case QueueInfo.STATUS_FORBID:
            case QueueInfo.STATUS_BE_MUTED_AUDIO:
                viewHolder.iv_user_status.setVisibility(View.GONE);
                viewHolder.iv_user_status.setVisibility(View.GONE);
                viewHolder.ivStatusHint.setVisibility(View.VISIBLE);
                viewHolder.ivStatusHint.setImageResource(R.drawable.audio_be_muted_status);
                break;
            case QueueInfo.STATUS_CLOSE_SELF_AUDIO:
                viewHolder.iv_user_status.setVisibility(View.GONE);
                viewHolder.ivStatusHint.setVisibility(View.VISIBLE);
                viewHolder.ivStatusHint.setImageResource(R.drawable.close_audio_status);
                break;
        }
        if (queueMember != null && status == QueueInfo.STATUS_LOAD) {//请求麦位
            viewHolder.tvNick.setText(queueMember.getNick());
        } else if (QueueInfo.hasOccupancy(queueInfo)) {//麦上有人
            viewHolder.ivAvatar.loadAvatar(queueMember.getAvatar());
            viewHolder.tvNick.setVisibility(View.VISIBLE);
            viewHolder.tvNick.setText(queueMember.getNick());
        } else {
            //麦上没人
            if (status != QueueInfo.STATUS_CLOSE) {
                viewHolder.iv_user_status.setVisibility(View.VISIBLE);
                viewHolder.iv_user_status.setImageResource(R.drawable.queue_add_member);
            }
            viewHolder.ivAvatar.setImageResource(R.color.color_292929);
            viewHolder.tvNick.setText("麦位" + (queueInfo.getIndex() + 1));
        }

    }


    private class QueueViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDefault;
        HeadImageView ivAvatar;
        ImageView ivStatusHint;
        ImageView iv_user_status;
        TextView tvNick;

        public QueueViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDefault = itemView.findViewById(R.id.iv_default_stats);
            ivAvatar = itemView.findViewById(R.id.iv_user_avatar);
            ivStatusHint = itemView.findViewById(R.id.iv_user_status_hint);
            tvNick = itemView.findViewById(R.id.tv_user_nick);
            iv_user_status = itemView.findViewById(R.id.iv_user_status);
        }
    }

}
