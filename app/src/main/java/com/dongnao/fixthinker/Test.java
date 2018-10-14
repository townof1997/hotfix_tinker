package com.dongnao.fixthinker;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by town on 2018/1/24.
 */

public class Test {
            public  void testFix(Context context){
                int i = 10;
                int a =0;
                Toast.makeText(context, "shit:"+i/a, Toast.LENGTH_SHORT).show();
//        Toast.makeText(context, "shit:"+i/a, Toast.LENGTH_SHORT).show();
//        try {
//            sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        Toast.makeText(context, "修复成功", Toast.LENGTH_SHORT).show();
    }
}
