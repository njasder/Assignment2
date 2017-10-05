package com.example.claire.assignment2;
import android.app.Activity;
import android.app.Application;

import java.util.LinkedList;

import android.app.Activity;
import android.app.Application;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by claire on 6/9/17.
 * This is to finish all activity when logged out.
 */

public class SysApplication extends Application {
    private List<Activity> mList = new LinkedList<Activity>();

    private static SysApplication instance;

    private SysApplication(){}

    public synchronized static SysApplication getInstance(){
        if (null == instance) {
            instance = new SysApplication();
        }
        return instance;
    }
    // add Activity
    public void addActivity(Activity activity) {
        mList.add(activity);
    }
    //finish each activity in the list
    public void exit() {
        try {
            for (Activity activity:mList) {
                if (activity != null)
                    activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
    //kill processes
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }
}

