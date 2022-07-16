package com.example.shakefeedback;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitManager {
    private static RetrofitManager retrofitManager;
    
    private Retrofit retrofit;

    private RetrofitManager() {}

    public static RetrofitManager getInstance() {
        if (retrofitManager == null) {
            synchronized (RetrofitManager.class) {
                if (retrofitManager == null) {
                    retrofitManager = new RetrofitManager();
                }
            }
        }
        return retrofitManager;
    }

    public Retrofit getRetrofit(String url) {
        if (retrofit == null) {
            // 拦截等级为body(可以打印出完整的网络请求)
			// 使用OkHttpClient
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1,TimeUnit.MINUTES)
                    .build();

			// 创建出Retrofit
            retrofit = new Retrofit.Builder()
             		// 使用Gson转换工厂
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .baseUrl(url)
                    .client(okHttpClient)
                    .build();
        }
        return retrofit;
    }
}
