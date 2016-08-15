package com.likailing.android.highlevelfourlkl1.imageloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Likailing on 2016/8/14.
 * 开启线程加载网络图片
 */
public class ImageThread implements Runnable{
    //定义url路径
    private String path;
    private ImageView mImageView;

    /*
    * 构造器：传入Url
    * 可供外界调用
    * */
    public ImageThread(String path, ImageView mImageView) {
        this.path = path;
        this.mImageView = mImageView;
        this.mImageView.setTag(path);
    }

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //接受消息
            Object obj=msg.obj;
            if (obj instanceof  Bitmap){
                Bitmap bitmap= (Bitmap) obj;
                if (path.equals(mImageView.getTag())){
                    mImageView.setImageBitmap(bitmap);
                }
            }
        }
    };


    @Override
    public void run() {
        //先看磁盘中是否有，如果有直接从磁盘获取，如果没有从网上请求
        //读取磁盘缓存
        Bitmap diskBitmap=DiskLruCacheTool.readBitmapDiskCache(path);
        if (diskBitmap!=null){
            //写入缓存
            MemoryCacheTool.saveMemoryCache(path,diskBitmap);
            //封装消息并发送
            Message message=mHandler.obtainMessage();
            message.obj=diskBitmap;
            mHandler.sendMessage(message);
            return;
        }
        //网络请求加载图片
        InputStream is=null;
        ByteArrayOutputStream baos=null;
        //获取URL
        try {
            URL url=new URL(path);
            HttpURLConnection connection= (HttpURLConnection) url.openConnection();
            connection.connect();
            //获取请求码
            if (connection.getResponseCode()==HttpURLConnection.HTTP_OK){
                //获取输入流
                is=connection.getInputStream();
                int len=0;
                //缓冲区
                byte[] buffer=new byte[1024];
                baos=new ByteArrayOutputStream();
                while ((len=is.read(buffer))!=-1){
                    baos.write(buffer,0,len);
                }
                baos.flush();

                //将获取到的流转换为Bitmap对象
                byte[] bytes=baos.toByteArray();
                //------------二次采样----------
                BitmapFactory.Options options=new BitmapFactory.Options();
                //如果为false，只加载图片边框属性，
                options.inJustDecodeBounds=true;
                BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
                int outHeight=options.outHeight;
                int ratio=outHeight/100;
                if (ratio>=1){
                    options.inSampleSize=ratio;
                }

                options.inJustDecodeBounds=false;
                Bitmap bitmap=BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
                //存入磁盘
                DiskLruCacheTool.saveBitmapDiskCache(path,bitmap);

                //Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                //将消息封装并发送
                Message message=mHandler.obtainMessage();
                message.obj=bitmap;
                mHandler.sendMessage(message);
            }
            //关闭流
            close(is);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            //关闭流
            close(is);
            close(baos);
        }
    }

    /*
    * 关闭流
    * */
    private void close(Closeable stream){
        if (stream!=null){
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
