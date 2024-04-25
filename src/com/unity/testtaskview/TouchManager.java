package com.unity.testtaskview;

import android.util.Log;
import android.view.Display;
import android.view.IPointerEventListener;
import android.view.MotionEvent;
import android.view.WindowManager;



public class TouchManager extends IPointerEventListener.Stub {
    private static final String TAG = "TouchManager";
    WindowManager mWindowManager = null;
    IDisplayTouchListener mListener;


    TouchManager(WindowManager manager) {
        mWindowManager = manager;
    }

    @Override
    public void onPointerEvent(MotionEvent motionEvent){
        if (mListener != null) {
            mListener.onTouch(motionEvent);
        }
    }

    public void registerListener(IDisplayTouchListener listener) {
        try {
            mListener = listener;
            if (mWindowManager != null) {
                Log.e(TAG, "registerPointerEventListener");
                mWindowManager.registerPointerEventListener(Display.DEFAULT_DISPLAY, this);
            }
        } catch (Exception e) {
            Log.e(TAG, "not connected!", e);
        }
    }

    public void unRegisterListener() {
        try {
            if (mWindowManager != null) {
                Log.e(TAG, "unRegisterListener");
                mWindowManager.unregisterPointerEventListener(this);
            }
        } catch (Exception e) {
            Log.e(TAG, "not connected!", e);
        }
    }

}
