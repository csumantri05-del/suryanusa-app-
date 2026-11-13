package id.suryanusa.kalkulator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private WebView web;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#0b4f6c"));

        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);   // menyimpan input terakhir pengguna
        s.setAllowFileAccess(false);
        s.setTextZoom(100);

        web.addJavascriptInterface(new Bridge(), "AndroidBridge");
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                return openExternal(req.getUrl());
            }
        });
        setContentView(web);
        if (savedInstanceState != null) web.restoreState(savedInstanceState);
        else web.loadUrl("file:///android_asset/index.html");
    }

    /** Tautan WhatsApp & web dibuka di aplikasi lain, halaman kalkulator tetap di sini. */
    private boolean openExternal(Uri uri) {
        String scheme = uri.getScheme();
        if ("file".equals(scheme)) return false;
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (Exception e) {
            Toast.makeText(this, "Tidak ada aplikasi untuk membuka tautan ini", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }

    private class Bridge {
        @JavascriptInterface
        public void copy(String text) {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("SuryaNusa", text));
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Pesan disalin", Toast.LENGTH_SHORT).show());
        }

        /** Kirim data pengajuan ke Google Apps Script (Google Sheets) dari sisi Android. */
        @JavascriptInterface
        public void post(String url, String body, String callbackId) {
            new Thread(() -> {
                boolean ok = false;
                String info = "";
                HttpURLConnection c = null;
                try {
                    if (!url.startsWith("https://script.google.com/")) throw new Exception("alamat tidak diizinkan");
                    c = (HttpURLConnection) new URL(url).openConnection();
                    c.setRequestMethod("POST");
                    c.setDoOutput(true);
                    c.setInstanceFollowRedirects(false);
                    c.setConnectTimeout(15000);
                    c.setReadTimeout(25000);
                    c.setRequestProperty("Content-Type", "text/plain;charset=utf-8");
                    try (OutputStream os = c.getOutputStream()) {
                        os.write(body.getBytes(StandardCharsets.UTF_8));
                    }
                    int code = c.getResponseCode();
                    // Apps Script menjawab 302; data sudah tersimpan saat itu. Ikuti redirect untuk membaca hasilnya.
                    if (code == 302 || code == 301 || code == 303) {
                        String loc = c.getHeaderField("Location");
                        c.disconnect();
                        ok = true;
                        if (loc != null) {
                            c = (HttpURLConnection) new URL(loc).openConnection();
                            c.setConnectTimeout(15000);
                            c.setReadTimeout(25000);
                            int code2 = c.getResponseCode();
                            if (code2 == 200) {
                                info = read(c.getInputStream());
                                if (info.contains("\"ok\":false")) ok = false;
                            }
                        }
                    } else if (code == 200) {
                        info = read(c.getInputStream());
                        ok = !info.contains("\"ok\":false");
                    } else {
                        info = "HTTP " + code;
                    }
                } catch (Exception e) {
                    info = String.valueOf(e.getMessage());
                } finally {
                    if (c != null) c.disconnect();
                }
                final String js = "window.__sheetCb(" + JSONObject.quote(callbackId) + "," + ok + "," + JSONObject.quote(info) + ")";
                runOnUiThread(() -> web.evaluateJavascript(js, null));
            }).start();
        }
    }

    private static String read(InputStream in) throws Exception {
        StringBuilder sb = new StringBuilder();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) > 0) sb.append(new String(buf, 0, n, StandardCharsets.UTF_8));
        in.close();
        return sb.toString();
    }
}
