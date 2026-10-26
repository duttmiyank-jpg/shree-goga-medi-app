package com.example.miyank

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPaymentManager(): PaymentManager

interface PaymentManager {
    fun initiateUpiPayment(
        upiId: String,
        name: String,
        amount: String,
        note: String
    )
}
