package com.example.watch_anime;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity {

    private WebView mWebView;
    private SwipeRefreshLayout refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hide the ActionBar (App Bar)
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Initialize WebView and SwipeRefreshLayout
        mWebView = findViewById(R.id.webView);
        refresh = findViewById(R.id.refresh);

        // Set up WebView settings
        mWebView.setWebViewClient(new MyWebViewClient()); // Handle URL redirects in the app
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Enable JavaScript on web pages
        webSettings.setGeolocationEnabled(true); // Enable GPS location on web pages

        // Set up WebChromeClient for full-screen video playback
        mWebView.setWebChromeClient(new MyChrome());

        // Load a web page
        mWebView.loadUrl("https://aniwave.to/home");

        // Set up swipe-to-refresh functionality
        refreshApp();
    }

    public class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // Handle URL loading within the WebView
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            final String urls = url;
            if (urls.contains("mailto") || urls.contains("whatsapp") || urls.contains("tel") || urls.contains("sms") || urls.contains("facebook") || urls.contains("truecaller") || urls.contains("twiter")) {
                mWebView.stopLoading();
                Intent i = new Intent();
                i.setAction(Intent.ACTION_VIEW);
                i.setData(Uri.parse(urls));
                startActivity(i);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private class MyChrome extends WebChromeClient {
        private View mCustomView;
        private WebChromeClient.CustomViewCallback mCustomViewCallback;
        protected FrameLayout mFullscreenContainer;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;

        MyChrome() {}

        public Bitmap getDefaultVideoPoster() {
            if (mCustomView == null) {
                return null;
            }
            return BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573);
        }

        public void onHideCustomView() {
            if (this.mCustomView == null) {
                return; // No custom view to hide
            }

            // Hide the ActionBar (App Bar)
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }

            // Remove the custom view from the layout
            ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
            decorView.removeView(this.mCustomView);

            // Clean up
            this.mCustomView = null;
            this.mCustomViewCallback = null;

            // Restore the original layout parameters
            mWebView.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));

            // Restore the original orientation
            setRequestedOrientation(this.mOriginalOrientation);

            // Restore the original system UI visibility
            decorView.setSystemUiVisibility(this.mOriginalSystemUiVisibility);

            // Request layout to apply the restored parameters
            mWebView.requestLayout();
        }

        public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback) {
            if (this.mCustomView != null) {
                onHideCustomView();
                return;
            }
            this.mCustomView = paramView;
            this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
            this.mOriginalOrientation = getRequestedOrientation();
            this.mCustomViewCallback = paramCustomViewCallback;

            // Set the activity's orientation to landscape
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            // Adjust WebView layout parameters for full-screen mode
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            mWebView.setLayoutParams(layoutParams);

            ((FrameLayout) getWindow().getDecorView()).addView(this.mCustomView);
            getWindow().getDecorView().setSystemUiVisibility(3846 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mWebView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mWebView.restoreState(savedInstanceState);
    }

    private void refreshApp() {
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Reload the current page when refreshing
                mWebView.reload();

                // Hide the refresh indicator when the page has finished reloading
                mWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        refresh.setRefreshing(false);
                    }
                });
            }
        });
    }
}