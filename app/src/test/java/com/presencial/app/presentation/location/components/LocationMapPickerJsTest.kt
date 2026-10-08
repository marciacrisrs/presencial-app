package com.presencial.app.presentation.location.components

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class LocationMapPickerJsTest {

    @Test
    fun `javascript is enabled before osm_map html is loaded`() {
        val source = locationMapPickerSource()
        val loadUrlIndex = source.indexOf("loadUrl(")
        assertTrue(loadUrlIndex >= 0, "LocationMapPicker must load osm_map.html")

        val beforeLoad = source.substring(0, loadUrlIndex)
        assertTrue(
            beforeLoad.contains("javaScriptEnabled = true"),
            "Leaflet scripts in osm_map.html run only during document parse, so JS must be on before loadUrl"
        )
        assertFalse(
            beforeLoad.contains("javaScriptEnabled = false"),
            "Enabling JS in onPageFinished does not re-execute <script> tags; the map stays blank"
        )
    }

    @Test
    fun `onPageFinished does not toggle javascript after the page has already loaded`() {
        val source = locationMapPickerSource()
        assertFalse(
            source.contains("javaScriptEnabled = isTrustedAsset"),
            "Toggling JS in onPageFinished cannot initialize Leaflet after a no-JS parse"
        )
    }

    @Test
    fun `map javascript evaluation is limited to the local asset page with JS on`() {
        val assetUrl = "${LocationMapJsPolicy.ASSET_URL_PREFIX}/osm_map.html?lat=-23.55&lng=-46.63"
        assertTrue(LocationMapJsPolicy.canEvaluateMapJavascript(assetUrl, true))
        assertFalse(LocationMapJsPolicy.canEvaluateMapJavascript(assetUrl, false))
        assertFalse(LocationMapJsPolicy.canEvaluateMapJavascript("https://evil.example/osm_map.html", true))
        assertFalse(LocationMapJsPolicy.canEvaluateMapJavascript(null, true))
    }

    private fun locationMapPickerSource(): String {
        val file = File("src/main/java/com/presencial/app/presentation/location/components/LocationMapPicker.kt")
        assertTrue(file.isFile, "Missing ${file.path}")
        return file.readText()
    }
}
