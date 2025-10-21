package com.example.habitforge.utils;

import android.content.Context;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.InputStream;
import java.util.Collections;

public class FcmTokenGenerator {

    public static String getAccessToken(Context context) {
        try {
            InputStream stream = context.getAssets().open("service_account.json");
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
            credentials.refreshIfExpired();
            return credentials.getAccessToken().getTokenValue();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

