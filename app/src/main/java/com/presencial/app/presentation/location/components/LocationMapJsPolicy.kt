package com.presencial.app.presentation.location.components

internal object LocationMapJsPolicy {
    const val ASSET_URL_PREFIX = "file:///android_asset"

    fun isTrustedMapAssetUrl(url: String?): Boolean =
        url != null && url.startsWith(ASSET_URL_PREFIX)

    fun canEvaluateMapJavascript(pageUrl: String?, javaScriptEnabled: Boolean): Boolean =
        javaScriptEnabled && isTrustedMapAssetUrl(pageUrl)
}
