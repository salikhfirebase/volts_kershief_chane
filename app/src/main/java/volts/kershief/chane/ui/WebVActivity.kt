package volts.kershief.chane.ui

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.app.ActivityCompat
import com.google.android.gms.ads.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.DataSnapshot
import guy4444.smartrate.SmartRate
import im.delight.android.webview.AdvancedWebView
import kotlinx.android.synthetic.main.activity_splash.*
import org.joda.time.DateTime
import org.joda.time.Minutes
import volts.kershief.chane.EXTRA_TASK_URL
import volts.kershief.chane._core.BaseActivity
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import volts.kershief.chane.R


/**
 * Created by Andriy Deputat email(andriy.deputat@gmail.com) on 3/13/19.
 */
class WebVActivity : BaseActivity(), AdvancedWebView.Listener {

    lateinit var webView: WebView
    lateinit var progressBar: ProgressBar
    var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    val FILECHOOSER_RESULTCODE = 1
    var mCameraPhotoPath: String? = null
    val PERMISSION_CODE = 1000
    var size: Long = 0

    var isAlertDialogWorking = ""

    var whenShowAlert = ""

    var whichBanner = ""

    lateinit var dataSnapshot: DataSnapshot

    lateinit var alertBackImageView: ImageView

    lateinit var inflater: LayoutInflater
    lateinit var dialogView: View
    lateinit var yesButton: Button
    lateinit var noButton: Button

    lateinit var firebaseAnalytic: FirebaseAnalytics

    var minutesToday = 0

    lateinit var prefs: SharedPreferences

    private lateinit var mAdView : AdView
    private lateinit var mInterstitialAd: InterstitialAd
    var timeToShowAd: Long = 0

    override fun getContentView(): Int = R.layout.activity_web_v

    override fun initUI() {
        webView = web_view
        progressBar = progress_bar


        inflater = layoutInflater
        dialogView = inflater.inflate(R.layout.alert_dialog_layout, null)
        yesButton = dialogView.findViewById(R.id.alert_yes_button)
        noButton = dialogView.findViewById(R.id.alert_no_button)

        alertBackImageView = dialogView.findViewById(R.id.alert_back_imageView)

        firebaseAnalytic = FirebaseAnalytics.getInstance(this)

        prefs = getSharedPreferences("volts.kershief.chane", Context.MODE_PRIVATE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(
                NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW)
            )
        }

        intent.extras?.let {
            for (key in it.keySet()) {
                val value = intent.extras?.get(key)
                Log.d("MainActivityTokenGCM", "Key: $key Value: $value")
            }
        }

        //Адмоб айди
        MobileAds.initialize(this, "ca-app-pub-7165343268428671~8051940137")

        mAdView = findViewById(R.id.adView_main)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        mAdView.adListener = object: AdListener() {
            override fun onAdClosed() {
                mAdView.loadAd(adRequest)
            }

        }

        mInterstitialAd = InterstitialAd(this)
        //Межстраничное айди
        mInterstitialAd.adUnitId = "ca-app-pub-7165343268428671/6108303072"
        mInterstitialAd.loadAd(AdRequest.Builder().build())

        val handler = Handler()

        mInterstitialAd.adListener = object: AdListener() {

            override fun onAdClosed() {
                mInterstitialAd.loadAd(AdRequest.Builder().build())
                handler.postDelayed({
                    mInterstitialAd.show()
                }, timeToShowAd)
            }

            override fun onAdLoaded() {

                // mInterstitialAd.show()

            }
        }

    }

    override fun setUI() {
        logEvent("web-view-screen")
        progressBar.visibility = View.VISIBLE

        configureWebView()

        webView.loadUrl(intent.getStringExtra(EXTRA_TASK_URL))
        //webView.loadUrl("https://en.imgbb.com/")

        val alert = androidx.appcompat.app.AlertDialog.Builder(this)
//                .setPositiveButton(android.R.string.yes
//                ) { p0, p1 -> }
//                .setNegativeButton(android.R.string.no
//                ) { p0, p1 -> }
            .setView(dialogView)
            .create()


        getValuesFromDatabase({
            dataSnapshot = it

            isAlertDialogWorking = dataSnapshot.child("isAlertDialogWorking").value.toString()
            whenShowAlert = dataSnapshot.child("whenShowAlert").value.toString()
            whichBanner = dataSnapshot.child("whichBanner").value.toString()

            when (whichBanner) {
                "1" -> alertBackImageView.setImageResource(R.drawable.rate_five_stars_1)
                "2" -> alertBackImageView.setImageResource(R.drawable.rate_five_stars_2)
                "3" -> alertBackImageView.setImageResource(R.drawable.rate_five_stars_3)
            }

            timeToShowAd = dataSnapshot.child("time_to_show_ad").value.toString().toLong()

            val handler = Handler()
            handler.postDelayed({
                mInterstitialAd.show()
            }, timeToShowAd)

        })




        val uri = Uri.parse("market://details?id=$packageName")

        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        if (Build.VERSION.SDK_INT >= 21) {
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        } else {
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        }



        yesButton.setOnClickListener {
            SmartRate.Rate(this
                , "Rate Us"
                , "Tell others what you think about this app"
                , "Continue"
                , "Please take a moment and rate us on Google Play"
                , "click here"
                , "Cancel"
                , "Thanks for the feedback"
                , Color.parseColor("#2196F3")
                , 4
            ) {
                if (it == 4) {
                    val fourRateBundle = Bundle()
                    fourRateBundle.putString("FourRate", "FourRate")

                    firebaseAnalytic.logEvent("FourRate", fourRateBundle)
                }
                if (it == 5) {
                    val fiveRateBundle = Bundle()
                    fiveRateBundle.putString("FiveRate", "FiveRate")

                    firebaseAnalytic.logEvent("FiveRate", fiveRateBundle)
                }
            }
            alert.dismiss()
        }

        noButton.setOnClickListener {
            alert.dismiss()
        }


        val handler = Handler()
        handler.postDelayed({
            if (!prefs.getBoolean("gtuToday", false)) {
                prefs.edit().putString("gtu", "GTU").apply()
                val gtuOneBundle = Bundle()
                gtuOneBundle.putString("GTU", "GTU")

                firebaseAnalytic.logEvent("GTU", gtuOneBundle)
                prefs.edit().putBoolean("gtuToday", true).apply()

                if (isAlertDialogWorking == "1" && whenShowAlert == "GTU") {
                    alert.show()
                }

            }
        }, 60000)
        handler.postDelayed({
            if (!prefs.getBoolean("mtuToday", false)) {
                prefs.edit().putString("mtu", "MTU").apply()
                val gtuOneBundle = Bundle()
                gtuOneBundle.putString("MTU", "MTU")

                firebaseAnalytic.logEvent("MTU", gtuOneBundle)
                prefs.edit().putBoolean("mtuToday", true).apply()

                if (prefs.getBoolean("firstAlertShow", true)) {
                    if (!this.isFinishing) {
                        if (isAlertDialogWorking == "1" && whenShowAlert == "MTU") {
                            alert.show()
                            prefs.edit().putBoolean("firstAlertShow", false).apply()
                        }
                    }
                }
            }
        }, 180000)
    }

    @SuppressLint("SimpleDateFormat")
    fun createImageFile(): File {
        var timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "JPEG_" + timeStamp + "_"
        var storageDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
    }

    fun verifyStoragePermissions(activity:Activity) {

        var writePermission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        var readPermission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        var cameraPermission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA)

        var permission = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED || cameraPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, permission, PERMISSION_CODE)
        }

    }

    inner class PQChromeClient : WebChromeClient() {

        // For Android 5.0+
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onShowFileChooser(
            view: WebView,
            filePath: ValueCallback<Array<Uri>>,
            fileChooserParams: WebChromeClient.FileChooserParams
        ): Boolean {
            mFilePathCallback?.onReceiveValue(null)
            mFilePathCallback = filePath
            Log.e("FileCooserParams => ", filePath.toString())
            var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent != null) {
                if (takePictureIntent.resolveActivity(packageManager) != null) {
                    // Create the File where the photo should go
                    var photoFile: File? = null
                    try {
                        photoFile = createImageFile()
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath)
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                        Log.e("aga", "Unable to create Image File", ex)
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        mCameraPhotoPath = "file:" + photoFile.absolutePath
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
                    } else {
                        takePictureIntent = null
                    }
                }
            }
            val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
            contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            contentSelectionIntent.type = "image/*"
            val intentArray: Array<Intent?>
            if (takePictureIntent != null) {
                intentArray = arrayOf(takePictureIntent)
            } else {
                intentArray = arrayOfNulls(2)
            }

            val pickIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickIntent.type = "image/*"
            val chooserIntent = Intent.createChooser(contentSelectionIntent, "Select Image")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))

            startActivityForResult(pickIntent, 1)
            return true
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        webView.settings.allowFileAccess = true
        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        webView.settings.setAppCacheEnabled(true)
        webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        webView.settings.domStorageEnabled = true
        webView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
        webView.settings.useWideViewPort = true
        webView.settings.savePassword = true
        webView.settings.saveFormData = true
        webView.settings.setEnableSmoothTransition(true)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        webView.webChromeClient = PQChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                progressBar.visibility = View.GONE
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                val builder = AlertDialog.Builder(this@WebVActivity)
                var message = "SSL Certificate error."

                when (error?.primaryError) {
                    SslError.SSL_UNTRUSTED -> message = "The certificate authority is not trusted."
                    SslError.SSL_EXPIRED -> message = "The certificate has expired."
                    SslError.SSL_IDMISMATCH -> message = "The certificate Hostname mismatch."
                    SslError.SSL_NOTYETVALID -> message = "The certificate is not yet valid."
                }

                message += " Do you want to continue anyway?"

                builder
                    .setTitle("SSL Certificate Error")
                    .setMessage(message)
                    .setPositiveButton("Continue"){ _: DialogInterface, _: Int ->
                        handler?.proceed()
                    }
                    .setNegativeButton("Cancel"){ _: DialogInterface, _: Int ->
                        handler?.cancel()
                    }
                builder.create().show()
            }
        }
        verifyStoragePermissions(this)
    }


    override fun onBackPressed() {
        if(webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        webView.onPause()
        super.onPause()
    }



    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (data != null || mCameraPhotoPath != null)
        {
            var count = 0 //fix fby https://github.com/nnian
            var images:ClipData? = null
            try
            {
                images = data?.clipData
            }
            catch (e:Exception) {
                Log.e("Error!", e.localizedMessage)
            }
            if (images == null && data != null && data.dataString != null)
            {
                count = data.dataString!!.length
            }
            else if (images != null)
            {
                count = images.getItemCount()
            }
            var results = arrayOfNulls<Uri>(count)
            // Check that the response is a good one
            if (resultCode === Activity.RESULT_OK)
            {
                if (size !== 0L)
                {
                    // If there is not data, then we may have taken a photo
                    if (mCameraPhotoPath != null)
                    {
                        results = arrayOf(Uri.parse(mCameraPhotoPath))
                    }
                }
                else if (data != null) {
                    if (data.clipData == null) {
                        results = arrayOf(Uri.parse(data.dataString))
                    } else {
                        if (images != null) {
                            for (i in 0 until images.itemCount) {
                                results[i] = images.getItemAt(i).uri
                            }
                        }
                    }
                }
            }
            mFilePathCallback?.onReceiveValue(results as Array<Uri>)
            mFilePathCallback = null
        }

    }

    fun makeFirstDay(startTime: DateTime) {
        minutesToday += Minutes.minutesBetween(startTime, DateTime.now()).minutes
        if (minutesToday >= 1) {
            if (!prefs.getBoolean("gtuToday", false)) {
                prefs.edit().putString("gtu", "GTU").apply()
                val gtuOneBundle = Bundle()
                gtuOneBundle.putString("GTU", "GTU")

                firebaseAnalytic.logEvent("GTU", gtuOneBundle)
                prefs.edit().putBoolean("gtuToday", true).apply()
            }
        }
    }

    override fun onStop() {
        super.onStop()

//        minutesToday = prefs.getString("minutesToday", "0")!!.toInt()
//
//
//        if (prefs.getString("sessionTime", "") != "") {
//            val startTime = DateTime(prefs.getString("sessionTime", ""))
//            makeFirstDay(startTime)
//        }
//
//
//        prefs.edit().putString("minutesToday", minutesToday.toString()).apply()

        prefs.edit().putString("endurl", webView.url).apply()

    }

    override fun onPageFinished(url: String?) {
    }

    override fun onPageError(errorCode: Int, description: String?, failingUrl: String?) {
    }

    override fun onDownloadRequested(
        url: String?,
        suggestedFilename: String?,
        mimeType: String?,
        contentLength: Long,
        contentDisposition: String?,
        userAgent: String?
    ) {
    }

    override fun onExternalPageRequest(url: String?) {
    }

    override fun onPageStarted(url: String?, favicon: Bitmap?) {
    }
}