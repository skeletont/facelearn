package com.example.facelearn;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class CustomSurfaceView extends SurfaceView {

    public CustomSurfaceView(Context context) {
        super(context);
    }
    public CustomSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }
}
