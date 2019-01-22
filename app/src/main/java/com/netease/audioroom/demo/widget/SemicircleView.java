package com.netease.audioroom.demo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.Xfermode;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.netease.audioroom.demo.R;

public class SemicircleView extends View {

    Paint paint;
    Context context;
    Xfermode xfermode;
    int radius, width;
    Shader shader;

    String text;

    public SemicircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        xfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
        shader = new LinearGradient(100, 100, 500, 500, getResources().getColor(R.color.color_FF3257),
                getResources().getColor(R.color.color_FF6159), Shader.TileMode.CLAMP);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SemicircleView);
        text = typedArray.getString(R.styleable.SemicircleView_text);
        this.context = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getWidth();
        radius = width / 2;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(getResources().getColor(R.color.color_FF3257));
        paint.setAntiAlias(true);
        canvas.drawCircle(radius, 0, radius, paint);
        super.onDraw(canvas);
        //获取paint中的字体信息  settextSize要在他前面
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        // 计算文字高度
        float fontHeight = fontMetrics.bottom - fontMetrics.top;
        // 计算文字高度baseline
        float textBaseY = getHeight() - (getHeight() - fontHeight) / 2
                - fontMetrics.bottom;

        //获取字体的长度
        if (!TextUtils.isEmpty(text)) {
            float fontWidth = paint.measureText(text);
            //计算文字长度的baseline
            float textBaseX = radius - fontWidth / 2;
            paint.setColor(getResources().getColor(R.color.color_d9d9d9));
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(40);
            paint.setFakeBoldText(true);
            canvas.drawText(text, textBaseX, textBaseY, paint);
        }

    }

    public void setText(String text) {
        this.text = text;
        //TODO 没有调用onDraw方法 一脸懵～～～～
        invalidate();
    }
}
