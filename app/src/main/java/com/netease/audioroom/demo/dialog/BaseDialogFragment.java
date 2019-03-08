package com.netease.audioroom.demo.dialog;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

public class BaseDialogFragment extends DialogFragment {
    public String TAG = getClass().getName();

    @Override
    public void show(FragmentManager manager, String tag) {
        if (manager.findFragmentByTag(TAG) != null && manager.findFragmentByTag(TAG).isVisible()) {
            ((DialogFragment) manager.findFragmentByTag(TAG)).dismiss();
        }
        super.showNow(manager, tag);
    }
}
