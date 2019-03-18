package com.netease.audioroom.demo.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.audioroom.demo.R;
import com.netease.audioroom.demo.widget.HeadImageView;

public class QueueViewHolder extends RecyclerView.ViewHolder {
    public HeadImageView ivAvatar;
    public ImageView ivStatusHint;
    public ImageView iv_user_status;
    public TextView tvNick;

    public QueueViewHolder(@NonNull View itemView) {
        super(itemView);
        ivAvatar = itemView.findViewById(R.id.iv_user_avatar);
        ivStatusHint = itemView.findViewById(R.id.iv_user_status_hint);
        tvNick = itemView.findViewById(R.id.tv_user_nick);
        iv_user_status = itemView.findViewById(R.id.iv_user_stats);
    }


}
