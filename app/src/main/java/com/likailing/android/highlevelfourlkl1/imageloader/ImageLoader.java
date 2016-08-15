package com.likailing.android.highlevelfourlkl1.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Likailing on 2016/8/15.
 * 下载图片
 */
public class ImageLoader {
    private static ImageLoader imageLoader;
    //线程池
    private static ExecutorService executorService;
    //初始化
    public static ImageLoader init(Context context){
        //每次开启3个线程
        executorService= Executors.newFixedThreadPool(3);
        DiskLruCacheTool.init(context);
        if (imageLoader==null){
            imageLoader=new ImageLoader();
        }
        return imageLoader;
    }

    /*
    * 开始加载图片
    * */
    public void load(String url, ImageView imageView){
        //加载内存缓存
        Bitmap bimapMemoryCache=MemoryCacheTool.readMemoryCache(url);
        if (bimapMemoryCache!=null){
            imageView.setImageBitmap(bimapMemoryCache);
        }else {
            //开启线程加载图片
            executorService.execute(new ImageThread(url,imageView));
        }
        //刷新
        DiskLruCacheTool.flush();
    }

}
