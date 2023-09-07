package com.android.speedoride;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize WebView
        webView = findViewById(R.id.web);

        // Load the URL in WebView
        webView.loadUrl("https://speedoride.com/");

        // Enable JavaScript in WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Set a WebViewClient to handle page navigation
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("https://api.whatsapp.com/")) {
                    // Open WhatsApp using a URL scheme
                    // Example: https://api.whatsapp.com/send?phone=1234567890
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    return true; // Prevent WebView from loading this URL
                } else if (url.startsWith("tel:")) {
                    // Initiate a phone call if the URL is a phone number link
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    return true; // Prevent WebView from loading this URL
                } else {
                    // Open the URL in a new WebViewActivity
                    openWebActivity(url);
                    return true; // Prevent WebView from loading this URL
                }
            }
        });
    }

    private void openWebActivity(String url) {
        Intent intent = new Intent(MainActivity.this, com.android.speedoride.WebView.class);
        intent.putExtra("links", url);
        startActivity(intent);
    }
}
