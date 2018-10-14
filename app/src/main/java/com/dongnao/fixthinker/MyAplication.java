package com.dongnao.fixthinker;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by town on 2018/1/24.
 */

public class MyAplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        MultiDex.install(base);
        FixManager.loadDex(base);
//        FixDexUtils.loadFixedDex(base);
        super.attachBaseContext(base);

    }
}
