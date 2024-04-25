package com.unity.testtaskview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;


public class MyViewGroup extends FrameLayout {

    public MyViewGroup(Context context) {
        super(context);
    }

    public MyViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        int index = event.getActionIndex();
        final float x = event.getX();
        final float y = event.getY();
        final int action = event.getAction();
        final int actPoint = event.getActionMasked();

        Log.e("feng",String.format("11111index=%d,x=%f,y=%f,action=%d,actPoint=%d",index,x,y,action,actPoint));
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int index = event.getActionIndex();
        final float x = event.getX();
        final float y = event.getY();
        final int action = event.getAction();
        final int actPoint = event.getActionMasked();

        Log.e("feng",String.format("index=%d,x=%f,y=%f,action=%d,actPoint=%d",index,x,y,action,actPoint));
//        switch (action){
//            case
//        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }


}
