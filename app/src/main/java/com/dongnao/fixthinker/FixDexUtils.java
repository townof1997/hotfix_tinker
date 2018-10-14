package com.dongnao.fixthinker;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashSet;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

import static android.content.ContentValues.TAG;

public class FixDexUtils {
	private static HashSet<File> loadedDex = new HashSet<File>();
	
	static{
		loadedDex.clear();
	}
	
	public static void loadFixedDex(Context context){
		if(context == null){
			return ;
		}
		File filesDir = context.getDir("odex", Context.MODE_PRIVATE);
        Log.i(TAG, "loadFixedDex: "+filesDir.getAbsolutePath());
        File[] listFiles = filesDir.listFiles();
		for(File file : listFiles){
			if(file.getName().startsWith("classes")||file.getName().endsWith(".dex")){
				Log.i("INFO", "dexName:"+file.getName());
				loadedDex.add(file);
			}
		}
		doDexInject(context, filesDir, loadedDex);
	}

	private static void doDexInject(final Context appContext, File filesDir,HashSet<File> loadedDex) {
//        if(Build.VERSION.SDK_INT >= 23){
//            Log.w("INFO","Unable to do dex inject on SDK " + Build.VERSION.SDK_INT);
//        }
        String optimizeDir = filesDir.getAbsolutePath() + File.separator + "opt_dex";
        File fopt = new File(optimizeDir);
        if (!fopt.exists())
            fopt.mkdirs();
        try {
            for(File dex : loadedDex){
                DexClassLoader classLoader = new DexClassLoader(
                		dex.getAbsolutePath(), 
                		fopt.getAbsolutePath(),null, 
                		appContext.getClassLoader());
                inject(classLoader, appContext);
            }
        } catch (Exception e) {
            Log.i("INFO", "install dex error:"+Log.getStackTraceString(e));
        }
    }
	
	private static void inject(DexClassLoader loader, Context ctx){
        PathClassLoader pathLoader = (PathClassLoader) ctx.getClassLoader();
        try {
            Object dexElements = combineArray(
                    getDexElements(getPathList(loader)), getDexElements(getPathList(pathLoader)));
            Object pathList = getPathList(pathLoader);
            setField(pathList, pathList.getClass(), "dexElements", dexElements);
        } catch (Exception e) {
            Log.i("INFO", "inject dexClassLoader error:" + Log.getStackTraceString(e));
        }
    }
	
	/**
	 * get BaseDexClassLoader pathList field
	 * @param baseDexClassLoader
	 * @return
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	private static Object getPathList(Object baseDexClassLoader)
            throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        return getField(baseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    private static Object getField(Object obj, Class<?> cl, String field)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        Object obj1=localField.get(obj);
        return localField.get(obj);
    }

    /**
     * get pathList dexElements field
     * @param paramObject
     * @return
     * @throws IllegalArgumentException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private static Object getDexElements(Object paramObject)
            throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        return getField(paramObject, paramObject.getClass(), "dexElements");
    }
    
    /**
     * reset pathList field set the field value(combined array); 
     * @param obj
     * @param cl
     * @param field
     * @param value
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private static void setField(Object obj, Class<?> cl, String field,
                                 Object value) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
//  setField(pathList, pathList.getClass(), "dexElements", dexElements);
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        localField.set(obj, value);
        Log.i(TAG, "setField: obj0"+obj);
    }

    /**
     * combine the fixed DexElements before system dexElements
     * @param arrayLhs
     * @param arrayRhs
     * @return
     */
    private static Object combineArray(Object arrayLhs, Object arrayRhs) {
        Class<?> localClass = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);
        int j = i + Array.getLength(arrayRhs);
        Object result = Array.newInstance(localClass, j);
        for (int k = 0; k < j; ++k) {
            if (k < i) {
                Array.set(result, k, Array.get(arrayLhs, k));
            } else {
                Array.set(result, k, Array.get(arrayRhs, k - i));
            }
        }
        Log.i(TAG, "combineArray: "+result);
        return result;
    }
}
