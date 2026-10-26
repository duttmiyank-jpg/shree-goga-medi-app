package com.example.miyank.donation

import kotlinx.serialization.Serializable

/**
 * Donation Cause / Seva Activity
 */
@Serializable
data class DonationCause(
    val id: String = "",
    val titleHindi: String = "",
    val titleEnglish: String = "",
    val descriptionHindi: String = "",
    val descriptionEnglish: String = "",
    val icon: String = "🛕",
    val imageUrl: String = "",
    val recommendedAmount: Double = 501.0,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long = 0L
)

/**
 * Payment / Transaction Verification Status
 */
@Serializable
enum class DonationStatus(
    val hindiTitle: String,
    val englishTitle: String
) {
    INITIATED("प्रारंभ हुआ", "Initiated"),
    PROCESSING("प्रक्रिया जारी", "Processing"),
    PENDING("सत्यापन लंबित", "Verification Pending"),
    SUCCESS("सत्यापित एवं स्वीकृत", "Verified & Successful"),
    FAILED("असफल", "Failed"),
    CANCELLED("रद्द", "Cancelled"),
    REFUNDED("वापस", "Refunded"),
    REJECTED("अस्वीकृत", "Rejected")
}

/**
 * Payment Method Type
 */
@Serializable
enum class PaymentMethod(
    val hindiTitle: String,
    val englishTitle: String
) {
    UPI_INTENT("UPI एप्प", "UPI App"),
    UPI_QR("UPI QR कोड", "UPI QR Code"),
    BANK_TRANSFER("बैंक ट्रांसफर", "Bank Transfer")
}

/**
 * Bank & UPI Configuration
 */
@Serializable
data class BankUpiSettings(
    val trustName: String = "श्री जामनीवाला दरबार मंदिर ट्रस्ट",
    val bankName: String = "State Bank of India",
    val accountHolderName: String = "Shree Jamniwala Darbar Temple Trust",
    val accountNumber: String = "39876543210",
    val ifscCode: String = "SBIN0001234",
    val accountType: String = "चालू खाता (Current Account)",
    val upiId: String = "mandir@upi",
    val qrCodeUrl: String = "file:///android_asset/mandir_qr.jpg",
    val paymentInstructions: String = "UPI या बैंक ट्रांसफर के बाद कृपया संदर्भ संख्या (UTR) दर्ज करें ताकि तुरंत रसीद सत्यापित की जा सके।",
    val minDonationAmount: Double = 1.0,
    val updatedAt: Long = 0L
)

/**
 * Donation Transaction Record & Receipt Snapshot
 */
@Serializable
data class DonationTransaction(
    val id: String = "",
    val receiptNumber: String = "",
    val donorName: String = "",
    val mobileNumber: String = "",
    val gotraCity: String = "",
    val sevaCauseId: String = "",
    val sevaCauseName: String = "",
    val amount: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.UPI_INTENT,
    val paymentStatus: DonationStatus = DonationStatus.PENDING,
    val utrNumber: String = "",
    val paymentReference: String = "",
    val verificationNote: String = "",
    val verifiedBy: String = "",
    val createdAt: Long = 0L,
    val verifiedAt: Long = 0L
) {
    fun isVerified(): Boolean {
        return paymentStatus == DonationStatus.SUCCESS
    }

    fun getMaskedMobile(): String {
        return if (mobileNumber.length == 10) {
            "${mobileNumber.take(2)}******${mobileNumber.takeLast(2)}"
        } else mobileNumber
    }
}
