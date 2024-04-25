/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 package com.unity.testtaskview;

import android.app.WindowConfiguration;

import com.android.wm.shell.ShellTaskOrganizer;

import android.annotation.UiContext;
import android.app.ActivityTaskManager;
import android.app.TaskInfo;
import android.content.Context;
import android.window.TaskAppearedInfo;

import com.android.wm.shell.FullscreenTaskListener;
import com.android.wm.shell.ShellTaskOrganizer;
import com.android.wm.shell.TaskView;
import com.android.wm.shell.TaskViewFactory;
import com.android.wm.shell.TaskViewFactoryController;
import com.android.wm.shell.common.HandlerExecutor;
import com.android.wm.shell.common.SyncTransactionQueue;
import com.android.wm.shell.common.TransactionPool;
import com.android.wm.shell.startingsurface.StartingWindowController;
import com.android.wm.shell.startingsurface.phone.PhoneStartingWindowTypeAlgorithm;

import java.util.List;
import java.util.function.Consumer;
import android.util.Log;
import java.util.Map;
import java.util.HashMap;


public final class TaskViewManager {
    private static final boolean DBG = false;

    private final Context mContext;
    private final HandlerExecutor mExecutor;
    private static TaskViewFactory mTaskViewFactory;
    private static ShellTaskOrganizer taskOrganizer;

    private  Map<String,TaskView> mTaskViews = new HashMap<>();

    public TaskViewManager(@UiContext Context context, HandlerExecutor handlerExecutor) {
        mContext = context;
        mExecutor = handlerExecutor;
        mTaskViewFactory = initWmShell();
    }

    private TaskViewFactory initWmShell() {
        taskOrganizer = new ShellTaskOrganizer(mExecutor, mContext);
        TransactionPool transactionPool = new TransactionPool();
        FullscreenTaskListener fullscreenTaskListener =
                new FullscreenTaskListener(new SyncTransactionQueue(transactionPool, mExecutor));
        taskOrganizer.addListenerForType(fullscreenTaskListener, ShellTaskOrganizer.TASK_LISTENER_TYPE_FULLSCREEN);
        // StartingWindowController startingController =
        //         new StartingWindowController(mContext, mExecutor,
        //                 new PhoneStartingWindowTypeAlgorithm(), transactionPool);
        //taskOrganizer.initStartingWindow(startingController);
        List<TaskAppearedInfo> taskAppearedInfos = taskOrganizer.registerOrganizer();
        cleanUpExistingTaskViewTasks(taskAppearedInfos);

        return new TaskViewFactoryController(taskOrganizer, mExecutor).asTaskViewFactory();
    }

    void createTaskView(Consumer<TaskView> onCreate) {
        mTaskViewFactory.create(mContext, mExecutor, onCreate);
    }

    private static void cleanUpExistingTaskViewTasks(List<TaskAppearedInfo> taskAppearedInfos) {
        ActivityTaskManager atm = ActivityTaskManager.getInstance();
        for (TaskAppearedInfo taskAppearedInfo : taskAppearedInfos) {
            TaskInfo taskInfo = taskAppearedInfo.getTaskInfo();
            Log.e("TaskViewManager","taskInfo = "+taskInfo);
            // Only TaskView tasks have WINDOWING_MODE_MULTI_WINDOW.
            if (taskInfo.getWindowingMode() == WindowConfiguration.WINDOWING_MODE_MULTI_WINDOW) {
                atm.removeTask(taskInfo.taskId);
            }
        }
    }


    public void removeTask(int id){
        ActivityTaskManager atm = ActivityTaskManager.getInstance();
        atm.removeTask(id);
    }





    // public void printTasks(){
    //     List<TaskAppearedInfo> taskAppearedInfos = taskOrganizer.registerOrganizer();
    //     for (TaskAppearedInfo taskAppearedInfo : taskAppearedInfos){
    //         TaskInfo taskInfo = taskAppearedInfo.getTaskInfo();
    //         Log.e("TaskViewManager","taskInfo = "+taskInfo);
    //     }
    // }


    public void addTaskView(String packageName, TaskView taskView) {
        mTaskViews.put(packageName, taskView);
    }


    public TaskView getTaskView(String packageName) {
        if(mTaskViews.containsKey(packageName)) {
            return mTaskViews.get(packageName);
        }
        return null;
    }







}
