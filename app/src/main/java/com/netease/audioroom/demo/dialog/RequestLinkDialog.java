package com.netease.audioroom.demo.dialog;

import android.app.DialogFragment;
import android.os.Bundle;

import com.netease.audioroom.demo.R;

public class RequestLinkDialog extends DialogFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(android.app.DialogFragment.STYLE_NO_TITLE, R.style.request_dialog_fragment);
    }


}
