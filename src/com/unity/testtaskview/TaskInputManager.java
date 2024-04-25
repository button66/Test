
package com.unity.testtaskview;

import android.view.BatchedInputEventReceiver;
import android.view.Choreographer;
import android.view.IWindowManager;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.WindowManagerGlobal;
import android.os.Looper;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;


import android.hardware.input.InputManager;
import static android.view.Display.DEFAULT_DISPLAY;
import android.view.InputMonitor;
import com.android.systemui.shared.system.InputChannelCompat;
import android.os.HandlerThread;





public class TaskInputManager {

    private static final String TAG = InputManager.class.getSimpleName();

    private IDisplayTouchListener mListener;




    private InputMonitor mInputMonitor;
    private InputChannelCompat.InputEventReceiver mInputEventReceiver;


    public void unRegisterListener() {
        mListener = null;
    }


    


    public void registerInputManager(IDisplayTouchListener listener) {
        
        mListener = listener;
        // Register input event receiver
        HandlerThread mHandlerThread = new HandlerThread("mHandlerThread");
        mHandlerThread.start();
        mInputMonitor = InputManager.getInstance().monitorGestureInput(
            "edge", DEFAULT_DISPLAY);
        // mInputEventReceiver = new InputChannelCompat.InputEventReceiver(
        //     mInputMonitor.getInputChannel(), Looper.getMainLooper(),
        //     Choreographer.getInstance(), this::onInputEvent);
        mInputEventReceiver = new InputChannelCompat.InputEventReceiver(
            mInputMonitor.getInputChannel(), mHandlerThread.getLooper(),
            Choreographer.getInstance(), this::onInputEvent);

    }




    private void onInputEvent(InputEvent ev) {
        if (!(ev instanceof MotionEvent)) return;
        MotionEvent event = (MotionEvent) ev;
        Log.e(TAG, "onInputEvent,event = "+event +"----threadid"+Thread.currentThread().getId());
        if(mListener != null) {
            mListener.onTouch(event);
        }

        //onMotionEvent(event);
    }
   




}