package com.unity.testtaskview;

import androidx.fragment.app.FragmentActivity;
import static android.app.ActivityTaskManager.INVALID_TASK_ID;
import static android.view.WindowManager.LayoutParams.PRIVATE_FLAG_TRUSTED_OVERLAY;

import static android.app.WindowConfiguration.WINDOWING_MODE_MULTI_WINDOW;


import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;

import android.os.Binder;

import com.android.wm.shell.TaskView;
import com.android.wm.shell.common.HandlerExecutor;
import android.widget.LinearLayout;

import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Map;
import java.util.HashMap;
import android.widget.LinearLayout;

import android.content.IntentFilter;

import android.content.BroadcastReceiver;

public class MainActivity1 extends FragmentActivity {

    private static final boolean DEBUG = true;

    private static final String TAG = "MainActivity";
    private static final int INVALID_TASK_ID = -1;

    TaskViewManager mTaskViewManager;
    int mTaskViewTaskId = INVALID_TASK_ID;
    TaskView mTaskView;
    boolean mTaskViewReady = false;
    private boolean mIsResumed;

    private Map<String, TaskView.Listener> mTaskViewListeners = new HashMap<>();
    private Map<String, Boolean> mTaskViewFlags = new HashMap<>();

    LinearLayout mGroup;

    BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Setting as trusted overlay to let touches pass through.
        getWindow().addPrivateFlags(PRIVATE_FLAG_TRUSTED_OVERLAY);
        // To pass touches to the underneath task.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        mGroup = (LinearLayout) findViewById(R.id.task_group);
        mGroup.setOrientation(LinearLayout.HORIZONTAL);

        setUpTaskView();

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v1) {
                // startIntentInTaskView();
                String[] packStrings = { "com.tencent.wecarflow", "com.qiyi.video" };
                // String[] packStrings={"com.tencent.wecarflow"};
                String paString = packStrings[(count++) % packStrings.length];
                TaskView v = mTaskViewManager.getTaskView(paString);
                if(v==null){
                    setUpTaskView(paString);
                    return;
                }
                if (v.getParent() == null) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    params.width = 720;
                    params.height = 600;
                    Log.e(TAG, "activity add");
                    mGroup.addView(v, params);
                } else {
                    mGroup.removeView(v);
                    Log.e(TAG, "activity remove");
                }

                // if(v.getVisibility()==View.VISIBLE){
                // v.setVisibility(View.GONE);
                // }else{
                // v.setVisibility(View.VISIBLE);
                // }
            }
        });

        mGroup.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "post");
                try {
                    ActivityOptions options = ActivityOptions.makeCustomAnimation(MainActivity1.this,
                            /* enterResId= */ 0, /* exitResId= */ 0);
                    // To show the Activity in TaskView, the Activity should be above the host task
                    // in
                    // ActivityStack. This option only effects the host Activity is in resumed.
                    options.setTaskAlwaysOnTop(true);
                    // Rect launchBounds = new Rect();
                    // launchBounds.left = 0;
                    // launchBounds.top = 0;
                    // launchBounds.bottom = 500;
                    // launchBounds.right = 300;
        
                    final Binder launchCookie = new Binder();
                    options.setLaunchCookie(launchCookie);
        
                    PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity1.this, /* requestCode= */ 0, getTaskIntent(),
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                    //options.setLaunchWindowingMode(WINDOWING_MODE_MULTI_WINDOW);
        
        
                    try {
                        pendingIntent.send(MainActivity1.this, 0 /* code */, null,
                        null /* onFinished */, null /* handler */, null /* requiredPermission */,
                        options.toBundle());
            
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                   
                    // taskView.startActivity(
                    //         PendingIntent.getActivity(this, /* requestCode= */ 0, getTaskIntent(),
                    //                 PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT),
                    //         /* fillInIntent= */ null, options, null /* launchBounds */);
                } catch (ActivityNotFoundException e) {
                    Log.w(TAG, "activity not found", e);
                }
            }
        },2000L);


        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e(TAG, "onReceive");
                String[] packStrings = { "com.tencent.wecarflow", "com.qiyi.video" };
                String paString = packStrings[(count++) % packStrings.length];
                TaskView v = mTaskViewManager.getTaskView(paString);

                if(v==null){
                    setUpTaskView(paString);
                    return;
                }

                if (v.getParent() == null) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    params.width = 720;
                    params.height = 600;
                    Log.e(TAG, "activity add");
                    mGroup.addView(v, params);
                } else {
                    mGroup.removeView(v);
                    Log.e(TAG, "activity remove");
                }

            }
        };
        IntentFilter i = new IntentFilter();
        i.addAction("switch_task");
        registerReceiver(mBroadcastReceiver, i);

        // getWindow().addPrivateFlags(PRIVATE_FLAG_TRUSTED_OVERLAY);

    }

    private void setUpTaskViewListener(final String packageName) {
        TaskView.Listener mTaskViewListener = new TaskView.Listener() {
            public void onTaskVisibilityChanged(int taskId, boolean visible) {
            }

            public void onBackPressedOnTaskRoot(int taskId) {
            }

            public void onInitialized() {
                // mTaskViewReady = true;
                Log.e(TAG, "onInitialized");
                mTaskViewFlags.put(packageName, true);

                startIntentInTaskView(packageName);

            }

            public void onReleased() {
                mTaskViewFlags.put(packageName, false);
                // mTaskViewReady = false;
            }

            public void onTaskCreated(int taskId, ComponentName name) {
                Log.e(TAG, "onTaskCreated taskId=" + taskId);
                mTaskViewTaskId = taskId;
            }

            public void onTaskRemovalStarted(int taskId) {
                Log.e(TAG, "onTaskRemovalStarted taskId=" + taskId);
                mTaskViewTaskId = INVALID_TASK_ID;
            }
        };
        mTaskViewListeners.put(packageName, mTaskViewListener);

    }

    public void setUpTaskView(String packString) {

        setUpTaskViewListener(packString);
        mTaskViewManager.createTaskView(taskView -> {
            TaskView.Listener l = mTaskViewListeners.get(packString);

            taskView.setListener(getMainExecutor(), l);
            // mTaskView = taskView;
            // 把TaskView添加到了mapsCard

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.width = 720;
            params.height = 600;
            mTaskViewManager.addTaskView(packString, taskView);
            mGroup.addView(taskView, params);

        });

    }

    private void setUpTaskView() {
        // 把TaskViewManager进行构造
        mTaskViewManager = new TaskViewManager(this,
                new HandlerExecutor(new Handler(getMainLooper())));
        String[] packStrings = { "com.tencent.wecarflow", "com.qiyi.video" };
        for (String packString : packStrings) {
            setUpTaskView(packString);
        }
    }

    private void startIntentInTaskView(String packageName) {

        TaskView taskView = mTaskViewManager.getTaskView(packageName);
        boolean isTaskViewReady = mTaskViewFlags.get(packageName);
        if (taskView == null || !isTaskViewReady) {
            return;
        }
        try {
            ActivityOptions options = ActivityOptions.makeCustomAnimation(this,
                    /* enterResId= */ 0, /* exitResId= */ 0);
            // To show the Activity in TaskView, the Activity should be above the host task
            // in
            // ActivityStack. This option only effects the host Activity is in resumed.
            options.setTaskAlwaysOnTop(true);
            // Rect launchBounds = new Rect();
            // launchBounds.left = 0;
            // launchBounds.top = 0;
            // launchBounds.bottom = 500;
            // launchBounds.right = 300;

           

            // PendingIntent pendingIntent = PendingIntent.getActivity(this, /* requestCode= */ 0, getTaskIntent(),
            // PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            //options.setLaunchWindowingMode(WINDOWING_MODE_MULTI_WINDOW);


            // try {
            //     pendingIntent.send(this, 0 /* code */, null,
            //     null /* onFinished */, null /* handler */, null /* requiredPermission */,
            //     options.toBundle());
    
            // } catch (Exception e) {
            //     throw new RuntimeException(e);
            // }
           
            taskView.startActivity(
                    PendingIntent.getActivity(this, /* requestCode= */ 0, getTaskIntent(),
                            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT),
                    /* fillInIntent= */ null, options, null /* launchBounds */);
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "activity not found", e);
        }
    }

    public int count = 0;

    public Intent getTaskIntent() {
        if ((count++) % 2 == 0) {
            return getIntent(this, "com.tencent.wecarflow");
        } else {
            return getIntent(this, "com.qiyi.video");
        }

    }

    public static Intent getIntent(Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        Log.e(TAG, "intent=" + intent);
        return intent;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsResumed = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTaskView != null && mTaskViewReady) {
            // mTaskView.release();
            mTaskView = null;
        }
        unregisterReceiver(mBroadcastReceiver);
    }

}