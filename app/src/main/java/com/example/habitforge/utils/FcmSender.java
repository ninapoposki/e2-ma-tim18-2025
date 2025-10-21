package com.example.habitforge.utils;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

public class FcmSender {

    private static final String PROJECT_ID = "habitforge-3559b"; // zameni sa tvojim Project ID
    private static final String FCM_URL = "https://fcm.googleapis.com/v1/projects/" + PROJECT_ID + "/messages:send";

    public static void sendFcmNotification(Context context, String fcmToken, String title, String body) {
        String accessToken = FcmTokenGenerator.getAccessToken(context);
        if (accessToken == null) return;

        try {
            JSONObject message = new JSONObject();
            JSONObject notification = new JSONObject();
            notification.put("title", title);
            notification.put("body", body);

            JSONObject messageWrapper = new JSONObject();
            messageWrapper.put("token", fcmToken);
            messageWrapper.put("notification", notification);

            message.put("message", messageWrapper);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, FCM_URL, message,
                    response -> Log.d("FCM", "Notification sent: " + response),
                    error -> Log.e("FCM", "Error sending FCM", error)
            ) {
                @Override
                public java.util.Map<String, String> getHeaders() {
                    java.util.Map<String, String> headers = new java.util.HashMap<>();
                    headers.put("Authorization", "Bearer " + accessToken);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            Volley.newRequestQueue(context).add(request);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

