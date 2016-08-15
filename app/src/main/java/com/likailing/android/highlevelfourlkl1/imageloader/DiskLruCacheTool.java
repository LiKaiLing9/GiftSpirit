package com.likailing.android.highlevelfourlkl1.imageloader;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Likailing on 2016/8/14.
 * 磁盘缓存
 */
public class DiskLruCacheTool {
    public static final int MAX_SIZE=4*1024*1024;
    private static DiskLruCache diskLruCache;

    /*
    * 初始化
    * @param context :上下文对象
    *
    * */
    public static void init(Context context){
        //开启缓存对象
        /*
        * 参数1：缓存的路径
        * 参数2：versioncode 版本号
        * 参数3：默认值1，即一个key对应一个value
        * 参数4：最大缓存空间
        * */
        File cacheDir=getCacheDir(context);
        int versionCode=getVersionCode(context);
        try {
            diskLruCache=DiskLruCache.open(cacheDir,versionCode,1,MAX_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    * 获取存储目录
    * */
    private static File getCacheDir(Context context){
        //首先判断外置存储是否有，如果有的话，首选外置存储
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()){
            return context.getExternalCacheDir();
        }
        //如果没有外置存储，则选择内置存储
        return context.getCacheDir();
    }

    /*
    * 获取系统的版本号versioncode
    * */
    private static int getVersionCode(Context context){
        String pakgeName=context.getPackageName();
        try {
            PackageInfo packageInfo=context.getPackageManager().getPackageInfo(pakgeName,0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        return 1;
    }

    /*
    * 将Bitmap对象写入磁盘
    * @param key 用来唯一标识当前的Bitmap
    *        key的要求：只能是a-z或者0-9这几个字符，长度不能32个字母，
    *        所以需要格式化key
    * @param bitmap
    * */
    public static void saveBitmapDiskCache(String key, Bitmap bitmap){
        //Editor对象用来存储数据
        String formatKey=formatKey(key);
        OutputStream os=null;
        try {
            DiskLruCache.Editor editor=diskLruCache.edit(formatKey);
            if (editor!=null){
                os=editor.newOutputStream(0);
                //将Bitmap对象写入输出流
                /*
                * 参数1：设置图片的格式，有PNG和Jpg
                * 参数2：图片质量，100表示无压缩存储
                * 参数3：输出流对象
                * 返回值：true表示写入成功，false表示写入失败
                * */
                boolean compress=bitmap.compress(Bitmap.CompressFormat.PNG,100,os);
                //如果写入成功，提交本次操作
                if (compress){
                    editor.commit();
                }else {
                    //如果失败，则撤销全部操作
                    editor.abort();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            //关闭流
            if (os!=null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
    * 从磁盘中读取Bitmap对象
    * @param url
    * @return
    * */
    public static Bitmap readBitmapDiskCache(String url){
        String formatKey=formatKey(url);
        try {
            //获取DiskLruCache.Snapshot对象，调用它的getInputStream()方法可以得到缓存文件的输入流
            DiskLruCache.Snapshot snapshot=diskLruCache.get(formatKey);
            if (snapshot!=null){
                InputStream is=snapshot.getInputStream(0);
                return BitmapFactory.decodeStream(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    * MD5加密
    * 格式化key,使key满足a-z或0-9,并且在32个字母以内
    * @param url
    * @return
    * */
    private static String formatKey(String url){
        //MessageDigest用于为应用程序提供信息摘要算法的功能，如MD5
        try {
            //MessageDigest通过getInstance静态函数来进行实例化和初始化
            MessageDigest messageDigest=MessageDigest.getInstance("MD5");
            //MessageDigest通过update方法处理数据
            messageDigest.update(url.getBytes());
            //一旦所有数据完成更新，调用digest()方法完成哈希计算并返回结果
            //digest方法只能被调用一次。digest方法被调用后， MessageDigest对象被重新设置为初始化状态
            byte[] bytes=messageDigest.digest();
            StringBuilder builder=new StringBuilder();
            for (int i = 0; i <bytes.length; i++) {
                //Integer.toHexString()转化为16进制，Math.abs()转化为绝对值
                builder.append(Integer.toHexString(Math.abs(bytes[i])));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return String.valueOf(url.hashCode());
    }

    /*
    * 刷新
    * */
    public static void flush(){
        if (diskLruCache!=null){
            try {
                diskLruCache.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
