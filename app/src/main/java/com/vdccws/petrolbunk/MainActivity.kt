package com.vdccws.petrolbunk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

class MainActivity : Activity() {
    // Your published app link
    private val appUrl = "https://karpagampds-alt.github.io/vdccws-wb/"
    private lateinit var web: WebView

    inner class Bridge {
        // Called by the web page to save Excel / PDF files and share/open them
        @JavascriptInterface
        fun saveFile(name: String, b64: String) {
            try {
                val dir = File(cacheDir, "exports"); dir.mkdirs()
                val f = File(dir, name)
                f.writeBytes(Base64.decode(b64, Base64.DEFAULT))
                val uri = FileProvider.getUriForFile(this@MainActivity, "$packageName.fileprovider", f)
                val mime = if (name.endsWith(".pdf")) "application/pdf"
                           else "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                val i = Intent(Intent.ACTION_SEND).apply {
                    type = mime
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                runOnUiThread { startActivity(Intent.createChooser(i, name)) }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this@MainActivity, "Could not save file", Toast.LENGTH_LONG).show() }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        setContentView(web)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.addJavascriptInterface(Bridge(), "AndroidBridge")
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true)
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(v: WebView, r: WebResourceRequest): Boolean {
                val host = r.url.host ?: ""
                // stay inside the app for our site, open everything else in the browser
                return if (host.endsWith("github.io")) false
                else { startActivity(Intent(Intent.ACTION_VIEW, r.url)); true }
            }
        }
        if (savedInstanceState == null) web.loadUrl(appUrl) else web.restoreState(savedInstanceState)
    }

    override fun onSaveInstanceState(out: Bundle) { super.onSaveInstanceState(out); web.saveState(out) }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { if (web.canGoBack()) web.goBack() else super.onBackPressed() }
}
