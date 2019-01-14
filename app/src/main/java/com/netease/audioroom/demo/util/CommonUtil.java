package com.netease.audioroom.demo.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.Collection;

public class CommonUtil {

    public static void loadImage(Context context, String url, ImageView imageView, int errResId, int size) {

        RequestOptions requestOptions = new RequestOptions().centerCrop();

        if (errResId > 0) {
            requestOptions = requestOptions.error(errResId);
        }

        if (size > 0) {
            requestOptions = requestOptions.override(size);
        }

        Glide.with(context.getApplicationContext())
                .asBitmap()
                .load(url)
                .apply(requestOptions)
                .into(imageView);
    }


    public static void loadImage(Context context, String url, ImageView imageView) {
        loadImage(context, url, imageView, 0, 0);
    }


    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

}
