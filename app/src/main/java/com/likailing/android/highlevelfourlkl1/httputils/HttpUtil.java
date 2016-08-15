package com.likailing.android.highlevelfourlkl1.httputils;

import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Likailing on 2016/8/14.
 * 网络请求工具类
 */
public class HttpUtil {
    //创建线程池
    private static ExecutorService executorService;
    /*
    * 入口
    * */
    public static HttpThread load(String path){
        //使用线程池管理线程
        if (executorService==null){
            //每次开启3个线程
            executorService= Executors.newFixedThreadPool(3);
        }
        //创建HttpThread对象
        HttpThread httpThread=new HttpThread();
        //调用HttpThread里面的start方法
        httpThread.start(path);
        return httpThread;
    }

    /*
    * 静态内部类
    * */
    public static class HttpThread{
        private ICallBack callBack;
        private int requestCode;
        //定义是否为Post请求
        private boolean isPost;
        //post请求中的参数
        private String params;

        private Handler mHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //接受消息并转化为字符串格式
                String result=msg.obj.toString();
                //使用接口回调
                if (callBack!=null){
                    //调用接口里面的方法
                    callBack.success(result,requestCode);
                }
            }
        };

        /*
        * post请求
        * */
        public HttpThread post(Map<String,Object> param){
            isPost=true;
            //post请求传入的参数
            this.params=formatParams(param);
            return this;
        }

        private String formatParams(Map<String, Object> param) {
            //将Key放入Set集合中
            Set<String> keySet=param.keySet();
            StringBuilder builder=new StringBuilder();
            for(String key:keySet){
                //根据key值取到对应的value值
                Object value=param.get(key);
                //如果有多个后面加上.append("&")
                builder.append(key).append("=").append(value);
            }
            return builder.toString();
        }

        /*
        * 出口
        * */
        public void callback(ICallBack callBack, int requestCode) {
            this.callBack = callBack;
            this.requestCode = requestCode;
        }

        public void start(String path){
            executorService.execute(new HttpRunnable(path));
        }


        /*
        * 开启子线程进行网络请求
        * */
        class HttpRunnable implements Runnable{
            //定义一个URL路径
            private String path;
            private InputStream is;

            //构造器：传入url路径，供外界调用
            public HttpRunnable(String path) {
                this.path = path;
            }

            @Override
            public void run() {
                //网络请求
                try {
                    //获取URL对象
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
                        StringBuilder builder=new StringBuilder();
                        while ((len=is.read(buffer))!=-1){
                            //new String(buffer,0,len)截取buffer中从0到len个长度的字符串
                            builder.append(new String(buffer,0,len));
                        }
                        //转化为字符串格式
                        String result=builder.toString();
                        //封装消息并发送
                        Message message=mHandler.obtainMessage();
                        message.obj=result;
                        message.sendToTarget();

                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        //关闭流
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
