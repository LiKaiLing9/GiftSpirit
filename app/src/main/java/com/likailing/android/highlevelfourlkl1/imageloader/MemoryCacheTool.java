package com.likailing.android.highlevelfourlkl1.imageloader;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * Created by Likailing on 2016/8/14.
 * 内存加载
 */
public class MemoryCacheTool {
    public static final int MAX_SIZE=4*1024*1024;
    private static LruCache<String,Bitmap> mLruCache=new LruCache<String, Bitmap>(MAX_SIZE){

        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    /*
    * 存入图片
    * */
    public static void saveMemoryCache(String key,Bitmap value){
        mLruCache.put(key, value);
    }

    /*
    * 读取图片
    * */
    public static Bitmap readMemoryCache(String key){
        return mLruCache.get(key);
    }
}
