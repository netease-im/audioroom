package com.netease.audioroom.demo.dialog;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.netease.audioroom.demo.R;
import com.netease.audioroom.demo.base.BaseAdapter;

import java.util.ArrayList;

public class BottomMenuDialog extends DialogFragment {
    public final static String BOTTOMMENUS = "bottommenus";

    ArrayList<String> dataList;

    View rootView;
    RecyclerView recyclerView;
    MyAdapter adapter;

    interface ItemClickListener {
        void onItemClick(String model, int position);
    }

    ItemClickListener itemClickListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(android.app.DialogFragment.STYLE_NO_TITLE, R.style.create_dialog_fragment);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_bottom_menu, container, false);
        final Window window = getDialog().getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);
        if (getArguments() != null) {
            dataList = getArguments().getStringArrayList(BOTTOMMENUS);
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        initView();
    }

    private void initView() {
        recyclerView = rootView.findViewById(R.id.dialog_bottom_menu_recyclerView);
        if (dataList != null) {
            adapter = new MyAdapter(dataList, getActivity());
            recyclerView.setAdapter(adapter);
            adapter.setItemClickListener(new BaseAdapter.ItemClickListener<String>() {
                @Override
                public void onItemClick(String model, int position) {
                    itemClickListener.onItemClick(model, position);
                }
            });
        }
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    class MyAdapter extends BaseAdapter<String> {

        public MyAdapter(ArrayList<String> dataList, Context context) {
            super(dataList, context);
        }

        @Override
        protected RecyclerView.ViewHolder onCreateBaseViewHolder(ViewGroup parent, int viewType) {

            TextView textView = new TextView(getActivity());
            textView.setTextColor(getActivity().getResources().getColor(R.color.color_000000));
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(16);

            return new MyViewHolder(textView);
        }

        @Override
        protected void onBindBaseViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            MyViewHolder myViewHolder = (MyViewHolder) holder;
            myViewHolder.textView.setText(getItem(position));

        }

        private class MyViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public MyViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView;
            }
        }
    }
}
