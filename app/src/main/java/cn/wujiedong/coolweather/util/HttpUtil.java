package cn.wujiedong.coolweather.util;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Administrator on 2017/10/2.
 */

public class HttpUtil {

    /**
     * 发送http请求,是异步访问网络的
     * @param address  请求地址
     * @param callback 请求之后的回调
     */
    public static void sendOkHttpRequest(String address, Callback callback){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        okHttpClient.newCall(request).enqueue(callback); //enqueue,开启异步线程,访问网络
    }
}
