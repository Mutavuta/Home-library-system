package com.library.admin.utils;

import android.content.Context;

import com.library.admin.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Single shared instance of Retrofit used by every API interface in the app
// Using a singleton means we reuse on HTTP connection pool instead of
// creating a new one every API call
public class RetrofitClient {

    // The single instance
    private static Retrofit instance;

    // Returns the shared Retrofit instance, creating it on first call
    public static Retrofit getInstance(Context context) {
        if (instance == null) {
            // Logs every HTTO request and response body in Logcat - debug builds only
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    // Adds JWT token to every request automatically
                    .addInterceptor(new AuthInterceptor(context.getApplicationContext()))
                    // Logs requests in Logcat so I can debug API issues easily
                    .addInterceptor(logging)
                    .build();

            instance = new Retrofit.Builder()
                    // API_BASE_URL comes from build.gradle
                    .baseUrl(BuildConfig.API_BASE_URL + "/")
                    .client(client)
                    // Converts JSON responses into Java model objects automatically
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return instance;
    }

}
