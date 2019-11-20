package com.example.webkit

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.KeyEvent
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        val TAG : String = MainActivity::class.java.simpleName
    }

    @SuppressLint("AddJavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        swipeRefreshLayout.setOnRefreshListener {
            webView.reload()
            swipeRefreshLayout.isRefreshing = false
        }

        webView.apply {
            settings.apply {
                // enable Javascript
                javaScriptEnabled = true

                /*
                 * Sets whether the WebView should support zooming using its on-screen zoom controls and gestures.
                 * The particular zoom mechanisms that should be used can be set with setBuiltInZoomControls(boolean).
                 * This setting does not affect zooming performed using the WebView#zoomIn() and WebView#zoomOut() methods.
                 * The default is true.
                 */
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false // no zoom button

                loadWithOverviewMode = true
                useWideViewPort = true

                domStorageEnabled = true
            }
        }

        webView.webViewClient = CustomWebViewClient(this)
        webView.webChromeClient = CustomWebChromeClient(this)

        if (WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROCESS_QUERY)) {
            Log.d(TAG, "isMultiProcessEnabled: " +  WebViewCompat.isMultiProcessEnabled())
        }

        if (WebViewFeature.isFeatureSupported(WebViewFeature.START_SAFE_BROWSING)) {
            WebViewCompat.startSafeBrowsing(this.applicationContext) { value ->
                Log.d(TAG, "WebViewCompat.startSafeBrowsing: $value")
            }
        }

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.getForceDark(webView.settings)
            WebSettingsCompat.setForceDark(webView.settings, WebSettingsCompat.FORCE_DARK_ON)
        }

        webView.loadUrl("https://www.google.co.jp/")
    }

    override fun onDestroy() {
        webView?.destroy()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        webView.onPause()
        super.onPause()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private class CustomWebViewClient internal constructor(private val activity: MainActivity) : WebViewClientCompat() {

        override fun onPageCommitVisible(view: WebView, url: String) {
            super.onPageCommitVisible(view, url)
            Log.d(TAG, "onPageCommitVisible: $url")
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceErrorCompat) {
            super.onReceivedError(view, request, error)
            Log.d(TAG, "onReceivedError: $error")
        }

        override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
            super.onReceivedHttpError(view, request, errorResponse)
            Log.d(TAG, "onReceivedHttpError: $errorResponse")
        }

        override fun onSafeBrowsingHit(view: WebView, request: WebResourceRequest, threatType: Int, callback: SafeBrowsingResponseCompat) {
            super.onSafeBrowsingHit(view, request, threatType, callback)
            Log.d(TAG, "onSafeBrowsingHit: $threatType")
        }

        //
        override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
            Log.d(TAG, "shouldOverrideUrlLoading: $url")
            return false
        }

        // クリックや戻るなどのアクションで画面(URL)が変化した時に通知される
        override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
            super.doUpdateVisitedHistory(view, url, isReload)
            Log.d(TAG, "doUpdateVisitedHistory: $activity.webView.url")
        }
    }

    private class CustomWebChromeClient internal constructor(private val activity: MainActivity) : WebChromeClient() {

        // JavaScriptを動かすために必要
        override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
            return false
        }

        // JavaScriptを動かすために必要
        override fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?, result: JsPromptResult?): Boolean {
            return false
        }

        // <a target="_blank">を反応させるために必要
        override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
            view ?: return false

            val href = view.handler.obtainMessage()
            view.requestFocusNodeHref(href)
            val url = href.data.getString("url")

            view.stopLoading()
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            activity.startActivity(browserIntent)
            return true
        }
    }
}
