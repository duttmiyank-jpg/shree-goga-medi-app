package com.example.miyank

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class AndroidPaymentManager(private val context: Context) : PaymentManager {
    override fun initiateUpiPayment(upiId: String, name: String, amount: String, note: String) {
        val uri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", upiId)
            .appendQueryParameter("pn", name)
            .appendQueryParameter("tn", note)
            .appendQueryParameter("am", amount)
            .appendQueryParameter("cu", "INR")
            .build()

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = uri
        
        val chooser = Intent.createChooser(intent, "Pay with...")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}

@Composable
actual fun rememberPaymentManager(): PaymentManager {
    val context = LocalContext.current
    return remember(context) { AndroidPaymentManager(context) }
}
