package com.example.miyank

expect suspend fun fetchUrlContent(urlString: String, timeoutMs: Int = 6000): String?
