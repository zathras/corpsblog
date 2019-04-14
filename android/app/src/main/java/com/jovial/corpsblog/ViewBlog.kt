package com.jovial.corpsblog

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient

class ViewBlog : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_blog)
        val actionBar = supportActionBar!!
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_USE_LOGO or ActionBar.DISPLAY_SHOW_TITLE)
        actionBar.setLogo(R.mipmap.peace_corps_logo)
        val webView : WebView = findViewById(R.id.view_blog_webview)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()     // Enables page navigation
        val url = intent.extras.getString("url")
        webView.loadUrl(url)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) : Boolean {
        if (item.itemId == R.id.main_menu_quit) {
            System.exit(0)
            return true     // not reached
        }
        return super.onOptionsItemSelected(item)
    }
}
