package com.example.miyank

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

private class JsBridge(
    private val mainHandler: Handler,
    private val onPlayerReady: () -> Unit,
    private val onPlayerStateChange: (Boolean) -> Unit,
    private val onError: (String) -> Unit
) {
    @JavascriptInterface
    fun onReady() {
        println("LiveDarshanLog: JS Player Ready callback received.")
        mainHandler.post {
            onPlayerReady()
        }
    }

    @JavascriptInterface
    fun onStateChange(stateCode: Int) {
        println("LiveDarshanLog: JS Player State Change: $stateCode")
        mainHandler.post {
            val playing = (stateCode == 1 || stateCode == 3)
            onPlayerStateChange(playing)
        }
    }

    @JavascriptInterface
    fun onError(errorCode: Int) {
        println("LiveDarshanLog: JS Player Error Code: $errorCode")
        mainHandler.post {
            val msg = when (errorCode) {
                101, 150, 152 -> "EMBEDDING_RESTRICTED"
                2 -> "अमान्य पैरामीटर (Invalid Parameter)"
                5 -> "HTML5 प्लेयर त्रुटि (HTML5 Player Error)"
                100 -> "वीडियो नहीं मिला (Video Not Found)"
                else -> "वीडियो लोड करने में त्रुटि (Error $errorCode)"
            }
            onError(msg)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun LiveVideoPlayer(
    videoId: String,
    isPlaying: Boolean,
    isMuted: Boolean,
    onPlayerReady: () -> Unit,
    onPlayerStateChange: (isPlaying: Boolean) -> Unit,
    onError: (errorMsg: String) -> Unit,
    modifier: Modifier
) {
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val jsInterface = remember(videoId) {
        JsBridge(mainHandler, onPlayerReady, onPlayerStateChange, onError)
    }

    LaunchedEffect(videoId) {
        println("LiveDarshanLog: Extracted Video ID: $videoId")
        println("LiveDarshanLog: Player URL: https://www.youtube-nocookie.com/embed/$videoId")
    }

    LaunchedEffect(isPlaying, isMuted) {
        webViewRef?.let { webView ->
            val playCmd = if (isPlaying) "playVideo();" else "pauseVideo();"
            val muteCmd = if (isMuted) "muteVideo();" else "unMuteVideo();"
            webView.evaluateJavascript("$playCmd $muteCmd", null)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(Color.BLACK)
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    allowFileAccess = true
                    allowContentAccess = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
                }
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        println("LiveDarshanLog: WebView load started: $url")
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        println("LiveDarshanLog: WebView page finished: $url")
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        if (request?.isForMainFrame == true) {
                            val errDesc = error?.description?.toString() ?: "Unknown error"
                            val errCode = error?.errorCode ?: -1
                            println("LiveDarshanLog: WebView error ($errCode): $errDesc for URL: ${request.url}")
                            mainHandler.post {
                                onError("WebView error ($errCode): $errDesc")
                            }
                        }
                    }

                    override fun onReceivedHttpError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        errorResponse: WebResourceResponse?
                    ) {
                        super.onReceivedHttpError(view, request, errorResponse)
                        if (request?.isForMainFrame == true) {
                            val statusCode = errorResponse?.statusCode ?: -1
                            println("LiveDarshanLog: HTTP error ($statusCode) for URL: ${request.url}")
                        }
                    }
                }

                addJavascriptInterface(jsInterface, "AndroidBridge")

                val htmlData = getYouTubeEmbedHtml(videoId, isMuted)
                loadDataWithBaseURL("https://www.youtube.com", htmlData, "text/html", "UTF-8", null)

                webViewRef = this
            }
        },
        update = { webView ->
            webViewRef = webView
        },
        onRelease = { webView ->
            try {
                webView.stopLoading()
                webView.loadUrl("about:blank")
                webView.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    )
}

private fun getYouTubeEmbedHtml(videoId: String, isMuted: Boolean): String {
    val muteSetting = if (isMuted) "player.mute();" else "player.unMute();"
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                body, html { margin: 0; padding: 0; width: 100%; height: 100%; background-color: #000; overflow: hidden; }
                iframe { width: 100%; height: 100%; border: none; }
            </style>
        </head>
        <body>
            <iframe id="player"
                    src="https://www.youtube-nocookie.com/embed/$videoId?enablejsapi=1&autoplay=1&playsinline=1&controls=1&modestbranding=1&rel=0"
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                    allowfullscreen>
            </iframe>
            <script>
                var tag = document.createElement('script');
                tag.src = "https://www.youtube.com/iframe_api";
                var firstScriptTag = document.getElementsByTagName('script')[0];
                firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

                var player;
                function onYouTubeIframeAPIReady() {
                    player = new YT.Player('player', {
                        events: {
                            'onReady': onPlayerReady,
                            'onStateChange': onPlayerStateChange,
                            'onError': onPlayerError
                        }
                    });
                }

                function onPlayerReady(event) {
                    $muteSetting
                    try { event.target.playVideo(); } catch(e) {}
                    if (window.AndroidBridge && window.AndroidBridge.onReady) {
                        window.AndroidBridge.onReady();
                    }
                }

                function onPlayerStateChange(event) {
                    if (window.AndroidBridge && window.AndroidBridge.onStateChange) {
                        window.AndroidBridge.onStateChange(event.data);
                    }
                }

                function onPlayerError(event) {
                    if (window.AndroidBridge && window.AndroidBridge.onError) {
                        window.AndroidBridge.onError(event.data);
                    }
                }

                function playVideo() { if (player && player.playVideo) player.playVideo(); }
                function pauseVideo() { if (player && player.pauseVideo) player.pauseVideo(); }
                function muteVideo() { if (player && player.mute) player.mute(); }
                function unMuteVideo() { if (player && player.unMute) player.unMute(); }
            </script>
        </body>
        </html>
    """.trimIndent()
}
