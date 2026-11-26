package dk.allanpersson.hyggespil

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.webkit.JavascriptInterfaceimport android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.webkit.WebViewAssetLoader
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var web: WebView
    private var tts: TextToSpeech? = null
    private var taleKlar = false

    /* Kun disse https-sider må åbnes uden for appen. Alt andet blokeres,
       så en fejl i indholdet ikke kan sende brugeren videre til ukendte sider. */
    private val tilladteVaerter = setOf("github.com", "buymeacoffee.com", "allanpersson.github.io")

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        /* Kant-til-kant: på Android 15+ tegnes siden under status- og
           navigationslinjen. Vi lægger selv sikkerhedsmargenen på WebView'en,
           så hverken topkort eller fod forsvinder under systemlinjerne. */
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        web = WebView(this)
        setContentView(web)

        /* Tilbageknappen (både hardware og gestus): spillet får først lov at lukke
           en dialog eller gå hjem fra et spil. Først hvis det ikke kan, lukkes appen. */
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                web.evaluateJavascript("(window.hsTilbage && window.hsTilbage()) ? '1' : '0'") { res ->
                    if (res != "1" && res != "\"1\"") finish()
                }
            }
        })
        web.setBackgroundColor(0xFF0F0A24.toInt())
        ViewCompat.setOnApplyWindowInsetsListener(web) { v, insets ->
            val b = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.setPadding(b.left, b.top, b.right, b.bottom)
            insets
        }
        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess = false
            allowContentAccess = false
            allowFileAccessFromFileURLs = false
            allowUniversalAccessFromFileURLs = false
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) safeBrowsingEnabled = true
            builtInZoomControls = false
            displayZoomControls = false
            textZoom = 100
        }
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)

        val loader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest) =
                loader.shouldInterceptRequest(request.url)

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                indsætTaleBro()
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val u = request.url
                if (u.host == "appassets.androidplatform.net") return false
                /* Eksterne links åbnes kun, når de er https og på listen. */
                if (u.scheme == "https" && tilladteVaerter.contains(u.host)) {
                    startActivity(Intent(Intent.ACTION_VIEW, u))
                }
                return true
            }
        }

        web.addJavascriptInterface(this, "AndroidVaerktoej")
        web.loadUrl("https://appassets.androidplatform.net/assets/www/hs.html")
    }

    /* Android kan tale dansk, også hvor WebView'en ikke kan. Broen giver spillet
       en speechSynthesis, så oplæsningen virker uden Home Assistant. */
    private fun indsætTaleBro() {
        web.evaluateJavascript(
            """
            (function(){
              if (window.__androidTale) return; window.__androidTale = true;
              if (window.speechSynthesis && window.speechSynthesis.speak) return;
              window.SpeechSynthesisUtterance = window.SpeechSynthesisUtterance || function(t){ this.text=t; this.lang='da-DK'; this.rate=1; this.pitch=1; };
              window.speechSynthesis = {
                speak: function(u){ try { AndroidVaerktoej.tal(String((u&&u.text)||'')); } catch(e){} },
                cancel: function(){ try { AndroidVaerktoej.stopTale(); } catch(e){} },
                pause: function(){}, resume: function(){},
                getVoices: function(){ return []; },
                speaking: false, pending: false, paused: false
              };
            })();
            """.trimIndent(), null
        )
    }

    @JavascriptInterface
    fun tal(tekst: String) {
        if (!taleKlar || tekst.isBlank()) return
        tts?.speak(tekst, TextToSpeech.QUEUE_FLUSH, null, "hyggespil")
    }

    @JavascriptInterface
    fun stopTale() {
        tts?.stop()
    }

    override fun onInit(status: Int) {
        taleKlar = status == TextToSpeech.SUCCESS
        if (taleKlar) {
            tts?.language = Locale("da", "DK")
            tts?.setSpeechRate(1.0f)
        }
    }

    override fun onPause() {
        tts?.stop()
        super.onPause()
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        web.destroy()
        super.onDestroy()
    }
}
