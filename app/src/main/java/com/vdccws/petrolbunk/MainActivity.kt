package com.vdccws.petrolbunk

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : Activity() {
    // Your published app link
    private val appUrl = "https://claude.ai/artifact/QHoMPDwCF6nmJu1eB8St67"
    private lateinit var web: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        setContentView(web)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true)
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(v: WebView, r: WebResourceRequest): Boolean {
                val host = r.url.host ?: ""
                // stay inside the app for Claude pages, open everything else in the browser
                return if (host.endsWith("claude.ai") || host.endsWith("anthropic.com")) false
                else { startActivity(Intent(Intent.ACTION_VIEW, r.url)); true }
            }
        }
        if (savedInstanceState == null) web.loadUrl(appUrl) else web.restoreState(savedInstanceState)
    }

    override fun onSaveInstanceState(out: Bundle) { super.onSaveInstanceState(out); web.saveState(out) }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { if (web.canGoBack()) web.goBack() else super.onBackPressed() }
}
