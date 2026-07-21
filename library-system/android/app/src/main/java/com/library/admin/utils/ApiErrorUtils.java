package com.library.admin.utils;

import com.google.gson.Gson;
import com.library.admin.model.ApiResponse;

import okhttp3.ResponseBody;
import retrofit2.Response;

// Extracts the human-readable error message from a failed API response
// The backend always sends { success:false, message: "...", data:null } on errors,
// but Retrofit only auto-parses the response body for successful (2xx) requests.
// For anything else (400, 403, 500) we have to manually parse errorBody() ourselves
public class ApiErrorUtils {

    public static String getErrorMassage(Response<?> response) {
        try {
            ResponseBody errorBody = response.errorBody();
            if (errorBody != null) {
                String json = errorBody.string();
                ApiResponse<?> parsed = new Gson().fromJson(json, ApiResponse.class);
                if (parsed != null && parsed.getMessage() != null) {
                    return parsed.getMessage();
                }
            }
        } catch (Exception e) {
            // Parsing failed - fall through to the generic message below
        }

        return "Something went wrong. Please try again. ";
    }

}
