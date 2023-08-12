package com.generativeaichatbot.gaichatbot;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiFetch {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();
    private final String API_Key;

    public ApiFetch(Context context){
        API_Key = getApiKey(context);
    }

    private String getApiKey(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("api_key", "");
    }

    public void fetchResponse(String msg, ApiCallback callback) {

        if(msg.equals("Hello")){
            callback.onResponse("It's called nature");
        }else{
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("model", "text-davinci-003");
                jsonBody.put("prompt", msg);
                jsonBody.put("max_tokens", 4000);
                jsonBody.put("temperature", 0);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            RequestBody requestBody = RequestBody.create(jsonBody.toString(), JSON);
            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/completions")
                    .header("Authorization", "Bearer " + API_Key)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            callback.onResponse("Failed to load message due to this reason " + e.getMessage()));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (API_Key.isEmpty()) {
                        new Handler(Looper.getMainLooper()).post(() ->
                                callback.onResponse("First add the API key"));
                    } else if (response.isSuccessful()) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            JSONArray jsonArray = jsonObject.getJSONArray("choices");
                            String result = jsonArray.getJSONObject(0).getString("text");
                            new Handler(Looper.getMainLooper()).post(() ->
                                    callback.onResponse(result.trim()));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        new Handler(Looper.getMainLooper()).post(() ->
                                callback.onResponse("Failed due to incorrect Api key, " + response.body().toString()));
                    }
                }
            });
        }
    }

    public interface ApiCallback {
        void onResponse(String response);
    }
}
