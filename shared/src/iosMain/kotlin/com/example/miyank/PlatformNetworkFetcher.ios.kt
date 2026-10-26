package com.example.miyank

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfURL

@OptIn(kotlinx.cinterop.BetaInteropApi::class, kotlinx.cinterop.ExperimentalForeignApi::class)
actual suspend fun fetchUrlContent(urlString: String, timeoutMs: Int): String? = withContext(Dispatchers.Default) {
    try {
        val nsUrl = NSURL.URLWithString(urlString) ?: return@withContext null
        val data = NSData.dataWithContentsOfURL(nsUrl) ?: return@withContext null
        NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
