package com.android.speedoride;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class WebView extends AppCompatActivity {
    private android.webkit.WebView webView;
    private ProgressBar progressBar;
    private ValueCallback<Uri[]> filePathCallback;
    private static final int REQUEST_CODE_FILE_UPLOAD = 1;
    private static final int REQUEST_CODE_PERMISSIONS = 2;

    private static final String[] REQUIRED_PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private boolean permissionGranted = false;

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);



        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webSettings.setAllowFileAccessFromFileURLs(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webSettings.setAllowUniversalAccessFromFileURLs(true);
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(android.webkit.WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE); // Show the progress bar
            }

            @Override
            public void onPageFinished(android.webkit.WebView view, String url) {
                progressBar.setVisibility(View.GONE); // Hide the progress bar
            }

            @Override
            public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {

                if (url.startsWith("https://api.whatsapp.com/")) {
                    // Open WhatsApp using a URL scheme
                    // Example: https://api.whatsapp.com/send?phone=1234567890
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    return true; // Prevent WebView from loading this URL
                }

                if (url.endsWith(".jpg") || url.endsWith(".png") || url.endsWith(".gif")) {
                    // Handle image URLs
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(url), "image/*");
                    startActivity(intent);
                    return true;
                } else {
                    // Load other URLs in the WebView
                    view.loadUrl(url);
                    return false;
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @SuppressLint("QueryPermissionsNeeded")
            public boolean onShowFileChooser(android.webkit.WebView webView, ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                if (!permissionGranted) {
                    requestPermissions();
                    return false;
                }

                WebView.this.filePathCallback = filePathCallback;

                Intent intent = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    intent = fileChooserParams.createIntent();
                }
                try {
                    startActivityForResult(intent, REQUEST_CODE_FILE_UPLOAD);
                } catch (Exception e) {
                    return false;
                }

                return true;
            }
        });

        String url = getIntent().getStringExtra("links");
        webView.loadUrl(url);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE_FILE_UPLOAD) {
            if (filePathCallback != null) {
                Uri[] result = null;

                if (resultCode == Activity.RESULT_OK && intent != null) {
                    ClipData clipData = intent.getClipData();
                    Uri dataUri = intent.getData();

                    if (clipData != null) {
                        int itemCount = clipData.getItemCount();
                        result = new Uri[itemCount];
                        for (int i = 0; i < itemCount; i++) {
                            result[i] = clipData.getItemAt(i).getUri();
                        }
                    } else if (dataUri != null) {
                        result = new Uri[]{dataUri};
                    }
                }

                filePathCallback.onReceiveValue(result);
                filePathCallback = null;
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            permissionGranted = true;
        }
    }
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack(); // Go back to the previous page
        } else {
            Intent i = new Intent(WebView.this, MainActivity.class);
            startActivity(i);
            finish(); // If there's no previous page, proceed with the default back button behavior
        }
    }


    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }
}