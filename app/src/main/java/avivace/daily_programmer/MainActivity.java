package avivace.daily_programmer;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.json.JSONArray;

public class MainActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipeContainer;
    public WebView mWebView;
    //private String url = "http://51.254.100.118/dp/material-master/templates/dp.html";
    private String url = "http://192.168.43.219:8080";
    //private String url = "http://51.254.100.118/dp_zero/robe.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);


        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                // fetchTimelineAsync(0);
                mWebView.reload();

            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        mWebView = (WebView) findViewById(R.id.webView);

        Window window = this.getWindow();

        mWebView.addJavascriptInterface(new WebAppInterface(this, window, swipeContainer), "Android");

        // Force links and redirects to open in the WebView instead of in a browser
        mWebView.setWebViewClient(new WebViewClient());

        // Enable Javascript
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        
        webSettings.setAppCacheEnabled(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setDomStorageEnabled(true);
        // _blank 1
        webSettings.setSupportMultipleWindows(true);
        mWebView.loadUrl(url);

        mWebView.setWebChromeClient(new WebChromeClient() {
            // Once the page has (almost) finished loading, trigger the Refreshing state
            public void onProgressChanged (WebView mWebView, int progress){
                if (progress > 80) swipeContainer.setRefreshing(false);
            }
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d("MyApplication", cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId() );
                return true;
            }
            // Catch _blank and open link in a new Android Intent
            @Override public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg)
            {
                WebView newWebView = new WebView(view.getContext());
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(newWebView);
                resultMsg.sendToTarget();
                return true;
            }
        });


    }


    public class WebAppInterface {
        Context mContext;
        Window mWindow;
        SwipeRefreshLayout mSwipeContainer;
        WebAppInterface (Context c, Window w, SwipeRefreshLayout sC){
            mContext = c;
            mWindow = w;
            mSwipeContainer = sC;
        }

        @JavascriptInterface
        public void sbColor(int A, int R, int G, int B){
            final int color = (A & 0xff) << 24 | (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff);

            // "Only the original thread that created a view hierarchy can touch its views."
            // Do things on the UiThread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSwipeContainer.invalidate();
                    mWindow.setStatusBarColor(color);
                }
            });
        }

        @JavascriptInterface
        public void sharer(String content){
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, content);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }
    }


    // Back goes to history[-1] instead of closing the app.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        else
        {
            finish();
            // finish the activity
        }
        return super.onKeyDown(keyCode, event);
    }
}
