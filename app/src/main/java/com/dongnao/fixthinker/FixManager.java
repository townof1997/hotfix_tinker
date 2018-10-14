package com.dongnao.fixthinker;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashSet;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * Created by town on 2018/1/24.
 */

public class FixManager {
    public static final String DEX_DIR = "odex";
    private static HashSet<File> loadedDex = new HashSet<File>();

    static{
        loadedDex.clear();
    }
    public static void loadDex(Context context) {
        if (context == null) {
            return;
        }
        File filesDir = context.getDir("odex", Context.MODE_PRIVATE);
        File[]  listFiles=filesDir.listFiles();
        for (File file : listFiles) {
            if(file.getName().startsWith("classes")||file.getName().endsWith(".dex")){
                Log.i("INFO", "dexName:"+file.getName());
                loadedDex.add(file);
            }
        }
        String optimizeDir = filesDir.getAbsolutePath() + File.separator + "opt_dex";
        File fopt = new File(optimizeDir);
        if (!fopt.exists()) {
            fopt.mkdirs();
        }
        for (File dex : loadedDex) {
            DexClassLoader classLoader = new DexClassLoader(dex.getAbsolutePath(), fopt.getAbsolutePath(), null, context.getClassLoader());
            PathClassLoader pathClassLoader = (PathClassLoader) context.getClassLoader();


            try {
//                -----------------------系统的ClassLoader------------------------------------
                Class baseDexClazzLoader=Class.forName("dalvik.system.BaseDexClassLoader");
                Field  pathListFiled=baseDexClazzLoader.getDeclaredField("pathList");
                pathListFiled.setAccessible(true);
                Object pathListObject = pathListFiled.get(pathClassLoader);

                Class  systemPathClazz=pathListObject.getClass();
                Field  systemElementsField = systemPathClazz.getDeclaredField("dexElements");
                systemElementsField.setAccessible(true);
                Object systemElements=systemElementsField.get(pathListObject);


//                ------------------自己的ClassLoader--------------------------
                Class myDexClazzLoader=Class.forName("dalvik.system.BaseDexClassLoader");
                Field  myPathListFiled=myDexClazzLoader.getDeclaredField("pathList");
                myPathListFiled.setAccessible(true);
                Object myPathListObject =myPathListFiled.get(classLoader);

                Class  myPathClazz=myPathListObject.getClass();
                Field  myElementsField = myPathClazz.getDeclaredField("dexElements");
                myElementsField.setAccessible(true);
                Object myElements=myElementsField.get(myPathListObject);

//                ------------------------融合-----------------------------
                Class<?> sigleElementClazz = systemElements.getClass().getComponentType();
                int systemLength = Array.getLength(systemElements);
                int myLength = Array.getLength(myElements);
                int newSystenLength = systemLength + myLength;
//                生成一个新的 数组   类型为Element类型
                Object newElementsArray = Array.newInstance(sigleElementClazz, newSystenLength);
                for (int i = 0; i < newSystenLength; i++) {
                    if (i < myLength) {
                        Array.set(newElementsArray, i, Array.get(myElements, i));
                    }else {
                        Array.set(newElementsArray, i, Array.get(systemElements, i - myLength));
                    }
                }
//      ---------------------------融合完毕   将新数组  放到系统的PathLoad内部---------------------------------
                Field  elementsField=pathListObject.getClass().getDeclaredField("dexElements");;
                elementsField.setAccessible(true);

                elementsField.set(pathListObject,newElementsArray);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
