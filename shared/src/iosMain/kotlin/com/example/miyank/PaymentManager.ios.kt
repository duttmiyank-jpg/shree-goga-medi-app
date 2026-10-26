package com.example.miyank

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class IosPaymentManager : PaymentManager {
    override fun initiateUpiPayment(upiId: String, name: String, amount: String, note: String) {
        // iOS doesn't support generic UPI intents like Android.
        // In a real app, we'd use a payment gateway SDK (like Razorpay)
        // or check for specific app schemes like 'phonepe://'
        println("UPI Payment initiated on iOS: $upiId, $amount")
    }
}

@Composable
actual fun rememberPaymentManager(): PaymentManager {
    return remember { IosPaymentManager() }
}
