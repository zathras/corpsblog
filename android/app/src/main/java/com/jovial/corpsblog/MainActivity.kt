package com.jovial.corpsblog

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.jovial.blog.Site
import com.jovial.blog.model.BlogConfig
import com.jovial.os.OSBrowser
import com.jovial.os.Stdout
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.util.prefs.Preferences
import android.content.Intent
import android.content.ActivityNotFoundException
import android.net.Uri
import android.webkit.WebView


open private class ViewLookup {
    protected fun<T: View> AppCompatActivity.findView(id: Int) : T = findViewById<T>(id)!!
}

class MainActivity : AppCompatActivity() {

    private enum class Prefs(val key: String, val default: String) {
        SRC_DIR("src_dir", "/sdcard"),
        DEST_DIR("dest_dir", "/sdcard");

        fun getString(p: SharedPreferences?) : String = p?.getString(key, default) ?: default

        fun putString(p: SharedPreferences?, value: String) {
            if (p != null) {
                with (p.edit()) {
                    putString(key, value)
                    commit()
                }
            }
        }
    }

    private val ui by lazy {
        object : ViewLookup() {
            val srcDirButton : Button = findView(R.id.srcDirButton)
            val srcDirText : TextView = findView(R.id.srcDirText)
            val destDirButton : Button = findView(R.id.destDirButton)
            val destDirText : TextView = findView(R.id.destDirText)
            val publishButton : Button = findView(R.id.publishButton)
            val mailButton : Button = findView(R.id.mailButton)
            val viewButton : Button = findView(R.id.viewButton)
            val outputText : TextView = findView(R.id.outputText)
            val outputTextSV : ScrollView = findView(R.id.outputTextSV)
        }
    }

    private enum class RequestID (
        val onGranted : (MainActivity) -> Unit
    ) {
        STARTUP( { _ ->
            // Do nothing
        } ),
        SET_SRC_DIR( { activity ->
            activity.launchChooseDirectoryDialog(activity.ui.srcDirText, Prefs.SRC_DIR)
        }),
        SET_DEST_DIR( { activity ->
            activity.launchChooseDirectoryDialog(activity.ui.destDirText, Prefs.DEST_DIR)
        })
    }

    private fun launchChooseDirectoryDialog(toSet: TextView, prefToSet: Prefs) {
        val dialog = DirectoryChooserDialog(this) { dir: String ->
            toSet.text = dir
            prefToSet.putString(prefs, dir)
        }
        dialog.newFolderEnabled = true
        dialog.chooseDirectory()
    }

    private val logListener = { line: String ->
        // Note that initial contents of ui.outputText was set at the moment
        // we added the listener
        runOnUiThread {
            ui.outputText.append(line)
            ui.outputTextSV.post {
                ui.outputTextSV.scrollTo(0, ui.outputText.height)
            }
        }
    }

    private var prefs : SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OSBrowser.addContext(applicationContext)
        val actionBar = supportActionBar!!
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_USE_LOGO or ActionBar.DISPLAY_SHOW_TITLE)
        actionBar.setLogo(R.mipmap.peace_corps_logo)
        setContentView(R.layout.activity_main)
        requestPermissions(RequestID.STARTUP)
        ui.srcDirButton.setOnClickListener {
            requestPermissions(RequestID.SET_SRC_DIR)
        }
        prefs = getPreferences(MODE_PRIVATE)
        ui.srcDirText.text = Prefs.SRC_DIR.getString(prefs)
        ui.destDirText.text = Prefs.DEST_DIR.getString(prefs)
        ui.destDirButton.setOnClickListener {
            requestPermissions(RequestID.SET_DEST_DIR)
        }
        ui.publishButton.setOnClickListener {
            runCorpsblog {
                Stdout.println()
                generateSite(true)
                Stdout.println("Done site generation.")
            }
        }
        ui.mailButton.setOnClickListener {
            runCorpsblog {
                Stdout.println()
                val site = generateSite(true)
                val mgr = site.mailchimpManager
                if (mgr == null) {
                    Stdout.println("***  Error:  Mailchimp not configured.")
                } else {
                    mgr.generateNotifications(site)
                }
                Stdout.println("Done mailing.")
            }
        }
        ui.viewButton.setOnClickListener {
            val index = File(ui.destDirText.text.toString(), "index.html")
            if (!index.exists()) {
                Toast.makeText(
                    this, "${index.absolutePath} does not exist.", Toast.LENGTH_LONG
                ).show()
            } else {
                val intent = Intent(this, ViewBlog::class.java)
                intent.putExtra("url", index.toURL().toString())
                startActivity(intent)
            }
        }
        ui.outputText.text = Stdout.addListener(logListener)
        logListener("")     // Scroll to end
    }

    private fun generateSite(publish: Boolean) : Site {
        val inputDir=File(ui.srcDirText.text.toString())
        val blogConfig = BlogConfig(File(inputDir, "corpsblog.config"))
        val site = Site(
            inputDir=File(ui.srcDirText.text.toString()),
            outputDir=File(ui.destDirText.text.toString()),
            blogConfig = blogConfig,
            publish=publish
        )
        site.generate()
        site.printNotes()
        if (site.hasErrors()) {
            site.printErrors()
        }
        return site
    }

    private fun setEnabled(enabled: Boolean) {
        ui.srcDirButton.isEnabled = enabled
        ui.destDirButton.isEnabled = enabled
        ui.publishButton.isEnabled = enabled
        ui.mailButton.isEnabled = enabled
        ui.viewButton.isEnabled = enabled
    }

    private fun runCorpsblog(task: () -> Unit) {
        setEnabled(false)
        val t = Thread({
            try {
                task()
            } catch (t: Throwable) {
                val baos = ByteArrayOutputStream()
                PrintStream(baos, true, "UTF-8").use { ps -> t.printStackTrace(ps) }
                Stdout.println(String(baos.toByteArray()))   // Android charset guaranteed to be utf8
                t.printStackTrace()     // logcat
            } finally {
                runOnUiThread {
                    setEnabled(true)
                }
            }
        },"corpsblog task");
        t.start()
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

    private fun hasPermission(permission: String) : Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions(id: RequestID) {
        if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            && hasPermission(Manifest.permission.INTERNET))
        {
            id.onGranted(this)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET
            ), id.ordinal)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val granted = permissions.size == 2 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if (granted) {
            RequestID.values()[requestCode].onGranted(this)
        } else if (requestCode == RequestID.STARTUP.ordinal) {
            Toast.makeText(
                this, "I'll run anyway, but I'll ask for permissions later.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        OSBrowser.removeContext(applicationContext)
        Stdout.removeListener(logListener)
    }
}
