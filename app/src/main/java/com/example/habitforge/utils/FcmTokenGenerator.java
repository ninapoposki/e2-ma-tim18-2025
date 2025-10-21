package com.example.habitforge.utils;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

public class FcmTokenGenerator {

    private static final String TAG = "FcmTokenGenerator";
    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";
    private static final String SCOPE = "https://www.googleapis.com/auth/firebase.messaging";

    /**
     * Gets an OAuth2 access token by creating a signed JWT and exchanging it.
     * Returns null on failure.
     */
    public static String getAccessToken(Context context) {
        try {
            // 1) load service_account.json from assets
            InputStream is = context.getAssets().open("service_account.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONObject sa = new JSONObject(json);

            String clientEmail = sa.getString("client_email");
            String privateKeyPem = sa.getString("private_key");

            // 2) build JWT
            long now = System.currentTimeMillis() / 1000L;
            long exp = now + 3600L; // 1 hour

            JSONObject header = new JSONObject();
            header.put("alg", "RS256");
            header.put("typ", "JWT");

            JSONObject claim = new JSONObject();
            claim.put("iss", clientEmail);
            claim.put("scope", SCOPE);
            claim.put("aud", TOKEN_URI);
            claim.put("iat", now);
            claim.put("exp", exp);

            String headerB64 = base64UrlEncode(header.toString().getBytes(StandardCharsets.UTF_8));
            String claimB64 = base64UrlEncode(claim.toString().getBytes(StandardCharsets.UTF_8));
            String unsignedJwt = headerB64 + "." + claimB64;

            // 3) sign with private key (PKCS#8)
            PrivateKey privateKey = getPrivateKeyFromPem(privateKeyPem);
            byte[] signature = sign(unsignedJwt.getBytes(StandardCharsets.UTF_8), privateKey);
            String signatureB64 = base64UrlEncode(signature);

            String signedJwt = unsignedJwt + "." + signatureB64;

            // 4) exchange JWT for access_token
            String postData = "grant_type=" + URLEncoder.encode("urn:ietf:params:oauth:grant-type:jwt-bearer", "UTF-8")
                    + "&assertion=" + URLEncoder.encode(signedJwt, "UTF-8");

            URL url = new URL(TOKEN_URI);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Accept", "application/json");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            InputStream respStream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(respStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            if (code < 200 || code >= 300) {
                Log.e(TAG, "Token exchange failed code=" + code + " resp=" + sb.toString());
                return null;
            }

            JSONObject resp = new JSONObject(sb.toString());
            String accessToken = resp.getString("access_token");
            // optionally you can read expires_in
            return accessToken;

        } catch (Exception e) {
            Log.e(TAG, "getAccessToken error", e);
            return null;
        }
    }

    private static String base64UrlEncode(byte[] input) {
        return Base64.encodeToString(input, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
    }

    private static byte[] sign(byte[] data, PrivateKey privateKey) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);
        sig.update(data);
        return sig.sign();
    }

    private static PrivateKey getPrivateKeyFromPem(String pem) throws Exception {
        // pem contains -----BEGIN PRIVATE KEY-----\n...base64...\n-----END PRIVATE KEY-----
        String privateKeyPem = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] pkcs8 = Base64.decode(privateKeyPem, Base64.DEFAULT);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }
}
