package com.example.video_mobile;

import android.content.Context;
import android.util.AttributeSet;
import android.view.WindowManager;
import android.widget.VideoView;

public class CustomVideoView extends VideoView {
    /**
     *     声明屏幕的大小
     */

    /*int width = 1920;
    int height = 1080;*/
    public CustomVideoView(Context context) {
        super(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
      //设置宽高
      int defaultWidth = getDefaultSize(0,widthMeasureSpec);
      int defaultHeight = getDefaultSize(0,heightMeasureSpec);
      setMeasuredDimension(defaultWidth,defaultHeight);


    }
}