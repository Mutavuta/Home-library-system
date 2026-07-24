package com.library.admin.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

// Automatically attaches the JWT token to every outgoing API request
// Without this we would have to manually add the Authorization header in every single API call
public class AuthInterceptor implements Interceptor {

    private final SharedPreferences prefs;

    public AuthInterceptor(Context context) {
        // Read from the same SharedPreferences file where we store the token after login
        prefs = context.getSharedPreferences("library_prefs", Context.MODE_PRIVATE);
    }

    @NonNull
    public Response intercept(Interceptor.Chain chain) throws IOException {
        // Read the stored JWT token - null if not loggr=ed in yet
        String token = prefs.getString("token", null);
        Request original = chain.request();

        if (token != null) {
            // Attach the token as a Bearer Authorization header on every request
            Request request = original.newBuilder()
                    .header("Authorization",  "Bearer " + token)
                    .build();
            return chain.proceed(request);
        }

        // No token yet (login screen) - send request as-is
        return chain.proceed(original);
    }

}
