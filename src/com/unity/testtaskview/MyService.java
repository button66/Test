package com.unity.testtaskview;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

import android.app.ActivityOptions;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.view.MotionEvent;


import androidx.annotation.NonNull;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;

import com.android.wm.shell.TaskView;
import com.android.wm.shell.common.HandlerExecutor;
import android.widget.LinearLayout;
import java.util.Map;
import java.util.HashMap;



public class MyService extends Service implements IDisplayTouchListener{

    private static final String TAG = "MyService";
    private static final int INVALID_TASK_ID = -1;

    private final static int NOTIFICATION_ID = android.os.Process.myPid();

    TaskViewManager mTaskViewManager;
    int mTaskViewTaskId;
    //TaskView mTaskView;
    boolean mTaskViewReady = false;
    TouchManager mTouchManager;

    private int mTop = 0;
    private int mLeft = 0;
    LinearLayout mGroup;

    private int mWindowX = 0;
    private int mWindowY = 0;

    private float mLastPointX = 0;
    private float mLastPointY = 0;


    private float mLastPointX3 = 0;
    private float mLastPointY3 = 0;
    private long mEventTime = 0;


    private static int WIDTH=1000;
    private static int HEIGHT=600;

    private Map<String,TaskView.Listener> mTaskViewListeners = new HashMap<>();
    private Map<String,Boolean> mTaskViewFlags = new HashMap<>();



    private static final int UPDATE_WINDOW_LOC = 0x100000;

    private static final int REMOVE_WINDOW = 0x100001;
    WindowManager mWindowManager;
    WindowManager.LayoutParams mWindowParam;

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case UPDATE_WINDOW_LOC:
                    Log.e(TAG, "handleMessage,mWindowParam = "+mWindowParam.x+","+mWindowParam.y);
                    mWindowManager.updateViewLayout(mGroup,mWindowParam);
                    break;
                case REMOVE_WINDOW:
                    Log.e(TAG, "REMOVE_WINDOW");
                    mWindowManager.removeView(mGroup);
                    stopSelf();
                    break;
            }
        }
    };


    
    // private final TaskView.Listener mTaskViewListener = new TaskView.Listener() {

    //     public void onTaskVisibilityChanged(int taskId, boolean visible) {
    //     }
    //     public void onBackPressedOnTaskRoot(int taskId) {
    //     }

    //     public void onInitialized() {
    //         mTaskViewReady = true;
    //         startIntentInTaskView();

    //     }
    //     public void onReleased() {
    //         mTaskViewReady = false;
    //     }
    //     public void onTaskCreated(int taskId, ComponentName name) {
    //         mTaskViewTaskId = taskId;
    //     }
    //     public void onTaskRemovalStarted(int taskId) {
    //         mTaskViewTaskId = INVALID_TASK_ID;
    //     }
        
    // };

    private void setUpTaskViewListener(final String packageName) {
        TaskView.Listener mTaskViewListener = new TaskView.Listener() {
            public void onTaskVisibilityChanged(int taskId, boolean visible) {
            }
            public void onBackPressedOnTaskRoot(int taskId) {
            }
    
            public void onInitialized() {
                //mTaskViewReady = true;
                Log.e(TAG, "onInitialized = "+packageName);
                mTaskViewFlags.put(packageName, true);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        startIntentInTaskView(packageName);
                    }
                });
    
            }
            public void onReleased() {
                mTaskViewFlags.put(packageName, false);
                //mTaskViewReady = false;
            }
            public void onTaskCreated(int taskId, ComponentName name) {
                mTaskViewTaskId = taskId;
                Log.e(TAG, "onTaskCreated = "+mTaskViewTaskId);

            }
            public void onTaskRemovalStarted(int taskId) {
                mTaskViewTaskId = INVALID_TASK_ID;
            }
        };
        mTaskViewListeners.put(packageName,mTaskViewListener);



    }



    public MyService() {
    }

    private void setUpTaskView() {
        //把TaskViewManager进行构造
        if(mTaskViewManager==null){
            mTaskViewManager = new TaskViewManager(this,
                new HandlerExecutor(new Handler(getMainLooper())));
        }
        if(mGroup==null){
            mGroup =new LinearLayout(MyService.this);
            mGroup.setBackgroundColor(Color.BLACK);
            mGroup.setOrientation(LinearLayout.HORIZONTAL);
            mWindowManager.addView(mGroup,mWindowParam);
        }
        //创建对应的TaskView
        // String[] packStrings={"com.tencent.wecarflow","com.qiyi.video"};
        String[] packStrings={"com.unity.map"};
        for(String packString:packStrings){
            setUpTaskViewListener(packString);
            mTaskViewManager.createTaskView(taskView -> {
                TaskView.Listener l = mTaskViewListeners.get(packString);

                taskView.setListener(getMainExecutor(), l);
                //mTaskView = taskView;
                //把TaskView添加到了mapsCard
                
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.width = WIDTH;
                params.height = HEIGHT;
                mTaskViewManager.addTaskView(packString, taskView);
                mGroup.addView(taskView,params);
                
            });
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void onCreate(){
        Log.e(TAG, "onCreate");
        super.onCreate();
        initWindow();
        setUpTaskView();
        //mTouchManager = new TouchManager(mWindowManager);
        //mTouchManager.registerListener(this);
        startForeground(NOTIFICATION_ID, getNotification());

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                TaskInputManager inputManager = new TaskInputManager();
                inputManager.registerInputManager(MyService.this);
            }
        },2000);
       
    }





    public void onDestroy(){
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        //if (mTaskView!= null) {
            Log.e(TAG, "removeView");
            mWindowManager.removeView(mGroup);
            //mGroup.removeView(mTaskView);
            //mTaskView.release();
        //}
        //mTouchManager.unRegisterListener();

    }


    private void initWindow(){
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowParam = new WindowManager.LayoutParams(
            WIDTH,
            HEIGHT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN 
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        mWindowParam.setTrustedOverlay();
        mWindowParam.width = WIDTH;
        mWindowParam.height = HEIGHT;
        mWindowParam.x = mWindowX;
        mWindowParam.y = mWindowY;
    }


    private String createNotificationChannel(String channelId, String channelName){
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }

    private Notification getNotification()
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, createNotificationChannel("test","testtaskview"));
        Notification notification = builder.setOngoing(true)
                .setPriority(PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        return notification;
    }




    public int onStartCommand(Intent intent, int flags, int startId) {
        //startIntentInTaskView();
        return super.onStartCommand(intent,flags,startId);
    }


    // private void startActivityBehind(PendingIntent pendingIntent){
    //     ActivityOptions options = ActivityOptions.makeTaskLaunchBehind();
    //     pendingIntent.send(mContext, 0 /* code */, fillInIntent,
    //                 null /* onFinished */, null /* handler */, null /* requiredPermission */,
    //                 options.toBundle());
    // }

    private void startIntentInTaskView(String packageName){

        TaskView taskView = mTaskViewManager.getTaskView(packageName);
        boolean isTaskViewReady = mTaskViewFlags.get(packageName);
        if (taskView == null || !isTaskViewReady) {
            return;
        }    
        try {
            ActivityOptions options = ActivityOptions.makeCustomAnimation(this,
                    /* enterResId= */ 0, /* exitResId= */ 0);
            // To show the Activity in TaskView, the Activity should be above the host task in
            // ActivityStack. This option only effects the host Activity is in resumed.
            options.setTaskAlwaysOnTop(true);
            // Rect launchBounds = new Rect();
            // launchBounds.left = 0;
            // launchBounds.top = 0;
            // launchBounds.bottom = 500;
            // launchBounds.right = 300;

            taskView.startActivity(
                    PendingIntent.getActivity(this, /* requestCode= */ 0, getTaskIntent(),
                            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT),
                    /* fillInIntent= */ null, options, null /* launchBounds */);
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "activity not found", e);
        }
    }


    public int count=0;

    public Intent getTaskIntent(){

        if((count++)%2==0){
            return getIntent(this,"com.unity.map");
        }else{
            return getIntent(this,"com.unity.map");
        }
        //return getIntent(this,"com.qiyi.video");
        //return getIntent(this,"com.tencent.wecarflow");
        
    }

    public static Intent getIntent(Context context, String packageName){
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        Log.e(TAG, "intent="+intent);
        return intent;
    }


    private boolean mFlag = false;


    @Override
    public void onTouch(MotionEvent event) {
        if(mFlag){
            return;
        }
        final int pointerCount = event.getPointerCount();
        if(pointerCount == 2){
            int action = event.getAction();
             if(action == MotionEvent.ACTION_MOVE){
                float x = event.getX(0);
                float y = event.getY(0);

                mWindowParam.x = mWindowParam.x+(int)(x-mLastPointX);
                mWindowParam.y = mWindowParam.y+(int)(y-mLastPointY);
                mLastPointX = x;
                mLastPointY = y;
                Message msg = Message.obtain();
                msg.what = UPDATE_WINDOW_LOC;
                mHandler.sendMessage(msg);
            }else if((action&MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN){
                int[] location = new int[2] ;
                mLastPointX = event.getX(0);
                mLastPointY = event.getY(0);
                Log.e(TAG, "param x = "+mWindowParam.x+",mWindowY = "+mWindowParam.y);

            }
        }else if(pointerCount == 3){
            int action = event.getAction();
            if((action&MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN){
                mEventTime = event.getEventTime();
                mLastPointX3 = event.getX(0);
                mLastPointY3 = event.getY(0);
                Log.e(TAG, "mWindowX = "+mWindowX+",mWindowY = "+mWindowY);
                Log.e(TAG, "param x = "+mWindowParam.x+",mWindowY = "+mWindowParam.y);

            }if(action == MotionEvent.ACTION_MOVE){
                float y = event.getY(0);
                long t = event.getEventTime();
                if((t-mEventTime)<100 && (y-mLastPointY3)>200){
                    
                    mFlag = true;
                    Message msg = Message.obtain();
                    msg.what = REMOVE_WINDOW;
                    mHandler.sendMessage(msg);
                }
            }
        }
    }

}