package com.drkryz.musicplayer.screens;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.StatusBarManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewRenderProcess;
import android.webkit.WebViewRenderProcessClient;

import com.drkryz.musicplayer.R;

public class YTMusicActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ytmusic);

        changeColors();

        WebView webView = (WebView) findViewById(R.id.webview);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSafeBrowsingEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setUserAgentString(
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36"
        );

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Log.e("REQUEST", "" + request.getUrl());
                return true;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            webView.setWebViewRenderProcessClient(new WebViewRenderProcessClient() {
                @Override
                public void onRenderProcessUnresponsive(@NonNull WebView webView, @Nullable WebViewRenderProcess webViewRenderProcess) {

                }

                @Override
                public void onRenderProcessResponsive(@NonNull WebView webView, @Nullable WebViewRenderProcess webViewRenderProcess) {

                }
            });
        }

        webView.loadUrl("https://music.youtube.com");
    }

    private void changeColors() {
        Window window = getWindow();

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.BLACK);
        window.setNavigationBarColor(Color.BLACK);
    }

    public class MediaWebView extends WebView {
        public MediaWebView(Context context) {
            super(context);
        }

        public MediaWebView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public MediaWebView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected void onWindowVisibilityChanged(int visibility) {
            if (visibility != View.GONE) super.onWindowVisibilityChanged(View.VISIBLE);
        }
    }
}