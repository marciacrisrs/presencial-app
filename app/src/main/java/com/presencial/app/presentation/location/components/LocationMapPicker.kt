package com.presencial.app.presentation.location.components



import android.annotation.SuppressLint

import android.webkit.WebResourceRequest

import android.webkit.WebSettings

import android.webkit.WebView

import android.webkit.WebViewClient

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.height

import androidx.compose.runtime.Composable

import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.remember

import androidx.compose.runtime.setValue

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.dp

import androidx.compose.ui.viewinterop.AndroidView

val LocalSkipLocationMap = staticCompositionLocalOf { false }



@SuppressLint("SetJavaScriptEnabled")

@Composable

fun LocationMapPicker(

    latitude: Double,

    longitude: Double,

    onLocationChanged: (Double, Double) -> Unit,

    modifier: Modifier = Modifier

) {

    if (LocalSkipLocationMap.current) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        return
    }

    val hasValidCoords = latitude != 0.0 || longitude != 0.0

    val initialLat = if (hasValidCoords) latitude else DEFAULT_LAT

    val initialLng = if (hasValidCoords) longitude else DEFAULT_LNG



    var webViewRef by remember { mutableStateOf<WebView?>(null) }



    LaunchedEffect(latitude, longitude) {

        if (hasValidCoords) {

            webViewRef?.evaluateJavascript(

                "setMarker($latitude, $longitude);",

                null

            )

        }

    }



    AndroidView(

        modifier = modifier

            .fillMaxWidth()

            .height(200.dp),

        factory = { context ->

            WebView(context).apply {

                settings.apply {

                    javaScriptEnabled = true

                    domStorageEnabled = true

                    cacheMode = WebSettings.LOAD_DEFAULT

                    mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

                    loadsImagesAutomatically = true

                    useWideViewPort = true

                    loadWithOverviewMode = true

                    allowFileAccess = false

                    allowContentAccess = false

                }

                webViewClient = object : WebViewClient() {

                    override fun onPageFinished(view: WebView, url: String) {

                        view.evaluateJavascript("invalidateMapSize();", null)

                    }



                    override fun shouldOverrideUrlLoading(

                        view: WebView,

                        request: WebResourceRequest

                    ): Boolean = handleUrl(request.url?.toString(), onLocationChanged)



                    @Deprecated("Deprecated in API 24")

                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean =

                        handleUrl(url, onLocationChanged)

                }

                loadUrl(buildMapUrl(initialLat, initialLng))

                webViewRef = this

            }

        },

        update = { webView ->

            webViewRef = webView

        }

    )

}



private fun handleUrl(

    url: String?,

    onLocationChanged: (Double, Double) -> Unit

): Boolean {

    if (url == null) return true

    if (url.startsWith(ASSET_URL_PREFIX)) return false

    val location = LocationUrlParser.parse(url) ?: return true

    onLocationChanged(location.first, location.second)

    return true

}



private fun buildMapUrl(latitude: Double, longitude: Double): String =

    "$ASSET_URL_PREFIX/osm_map.html?lat=$latitude&lng=$longitude"



private const val ASSET_URL_PREFIX = "file:///android_asset"

private const val DEFAULT_LAT = -23.5505

private const val DEFAULT_LNG = -46.6333

