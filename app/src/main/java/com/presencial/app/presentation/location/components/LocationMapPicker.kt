package com.presencial.app.presentation.location.components

import android.annotation.SuppressLint
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LocationMapPicker(
    latitude: Double,
    longitude: Double,
    onLocationChanged: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val hasValidCoords = latitude != 0.0 || longitude != 0.0
    val initialLat = if (hasValidCoords) latitude else DEFAULT_LAT
    val initialLng = if (hasValidCoords) longitude else DEFAULT_LNG

    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(latitude, longitude) {
        if (hasValidCoords) {
            webViewRef?.evaluateJavascript(
                "setMarker(${latitude}, ${longitude});",
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
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = object : WebViewClient() {
                    @Deprecated("Deprecated in API 24")
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        val location = LocationUrlParser.parse(url) ?: return false
                        onLocationChanged(location.first, location.second)
                        return true
                    }
                }
                loadDataWithBaseURL(
                    MAP_ORIGIN,
                    buildOpenStreetMapHtml(initialLat, initialLng),
                    "text/html",
                    "UTF-8",
                    null
                )
                webViewRef = this
            }
        }
    )
}

private fun buildOpenStreetMapHtml(latitude: Double, longitude: Double): String = """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8"/>
      <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
      <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
      <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
      <style>
        html, body, #map { height: 100%; margin: 0; padding: 0; }
      </style>
    </head>
    <body>
      <div id="map"></div>
      <script>
        function postLocation(lat, lng) {
          window.location.href = 'presencial://location/' + lat + '/' + lng;
        }
        var map = L.map('map').setView([$latitude, $longitude], 16);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          maxZoom: 19,
          attribution: '&copy; OpenStreetMap'
        }).addTo(map);
        var marker = L.marker([$latitude, $longitude], { draggable: true }).addTo(map);
        marker.on('dragend', function(e) {
          var pos = e.target.getLatLng();
          postLocation(pos.lat, pos.lng);
        });
        map.on('click', function(e) {
          marker.setLatLng(e.latlng);
          postLocation(e.latlng.lat, e.latlng.lng);
        });
        function setMarker(lat, lng) {
          marker.setLatLng([lat, lng]);
          map.setView([lat, lng], 16);
        }
      </script>
    </body>
    </html>
""".trimIndent()

private const val MAP_ORIGIN = "https://localhost/"
private const val DEFAULT_LAT = -23.5505
private const val DEFAULT_LNG = -46.6333
