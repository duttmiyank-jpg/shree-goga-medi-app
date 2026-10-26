package com.example.miyank.donation

import com.example.miyank.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object DonationCauseManager {

    private val settingsStorage = SettingsManager()
    private const val KEY_CAUSES_JSON = "donation_causes_json_v1"
    private const val KEY_BANK_SETTINGS_JSON = "donation_bank_settings_json_v1"
    private const val KEY_TRANSACTIONS_JSON = "donation_transactions_json_v1"

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _causes = MutableStateFlow<List<DonationCause>>(emptyList())
    val causes: StateFlow<List<DonationCause>> = _causes

    private val _bankSettings = MutableStateFlow(BankUpiSettings())
    val bankSettings: StateFlow<BankUpiSettings> = _bankSettings

    private val _transactions = MutableStateFlow<List<DonationTransaction>>(emptyList())
    val transactions: StateFlow<List<DonationTransaction>> = _transactions

    init {
        _causes.value = loadCauses()
        _bankSettings.value = loadBankSettings()
        _transactions.value = loadTransactions()
    }

    // --- SEVA CAUSES ---
    private fun loadCauses(): List<DonationCause> {
        val saved = settingsStorage.getString(KEY_CAUSES_JSON, "")
        if (saved.isNotBlank()) {
            try {
                return json.decodeFromString(saved)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return getDefaultCauses()
    }

    private fun getDefaultCauses(): List<DonationCause> {
        val now = 1758000000000L
        return listOf(
            DonationCause(
                id = "cause_1",
                titleHindi = "मंदिर निर्माण एवं भव्य विकास",
                titleEnglish = "Temple Construction & Development",
                descriptionHindi = "मंदिर परिसर के निर्माण, सौंदर्यीकरण तथा स्थायी विकास कार्यों हेतु योगदान दें।",
                descriptionEnglish = "Contribute towards construction and development of temple premises.",
                icon = "🛕",
                recommendedAmount = 1100.0,
                sortOrder = 1,
                isActive = true,
                createdAt = now
            ),
            DonationCause(
                id = "cause_2",
                titleHindi = "अखण्ड लंगर एवं भंडारा सेवा",
                titleEnglish = "Langar & Bhandara Seva",
                descriptionHindi = "दरबार में पधारने वाले श्रद्धालुओं हेतु प्रतिदिन आयोजित निःशुल्क लंगर व भंडारा सेवा।",
                descriptionEnglish = "Support daily free langar for visiting devotees.",
                icon = "🥣",
                recommendedAmount = 501.0,
                sortOrder = 2,
                isActive = true,
                createdAt = now
            ),
            DonationCause(
                id = "cause_3",
                titleHindi = "गौ माता सेवा एवं हरा चारा",
                titleEnglish = "Gau Seva",
                descriptionHindi = "गौमाता के संरक्षण, चिकित्सा तथा नियमित उत्तम आहार/चारे हेतु दान दें।",
                descriptionEnglish = "Donate towards cow care, food fodder and shelter.",
                icon = "🐄",
                recommendedAmount = 251.0,
                sortOrder = 3,
                isActive = true,
                createdAt = now
            ),
            DonationCause(
                id = "cause_4",
                titleHindi = "अखण्ड ज्योति व पूजा सेवा",
                titleEnglish = "Akhand Jyoti & Puja Seva",
                descriptionHindi = "दरबार की अखण्ड ज्योति हेतु शुद्ध घी, सामग्री एवं दैनिक आरती सेवा हेतु योगदान।",
                descriptionEnglish = "Support pure ghee and samagri for eternal Akhand Jyoti.",
                icon = "🪔",
                recommendedAmount = 501.0,
                sortOrder = 4,
                isActive = true,
                createdAt = now
            ),
            DonationCause(
                id = "cause_5",
                titleHindi = "श्री गोगामेड़ी मेला व्यवस्था व सेवा",
                titleEnglish = "Mela & Yatra Arrangements",
                descriptionHindi = "वार्षिक धार्मिक मेले एवं पदयात्रा के समय श्रद्धालुओं हेतु जल, चिकित्सा व विश्राम सेवा।",
                descriptionEnglish = "Support water, rest and medical camps during annual melas & yatras.",
                icon = "🚩",
                recommendedAmount = 1100.0,
                sortOrder = 5,
                isActive = true,
                createdAt = now
            ),
            DonationCause(
                id = "cause_6",
                titleHindi = "धार्मिक शिक्षा व निर्धन सहायता",
                titleEnglish = "Charity & Religious Education",
                descriptionHindi = "गरीब व जरूरतमंद श्रद्धालुओं तथा बच्चों की शिक्षा व आकस्मिक चिकित्सा में सहायता।",
                descriptionEnglish = "Support needy families and spiritual education for children.",
                icon = "📚",
                recommendedAmount = 251.0,
                sortOrder = 6,
                isActive = true,
                createdAt = now
            )
        )
    }

    fun saveCause(cause: DonationCause) {
        val current = _causes.value.toMutableList()
        val index = current.indexOfFirst { it.id == cause.id }
        if (index >= 0) {
            current[index] = cause
        } else {
            val newId = if (cause.id.isBlank()) "cause_${1758000000000L}" else cause.id
            current.add(0, cause.copy(id = newId, createdAt = 1758000000000L))
        }
        _causes.value = current
        try {
            settingsStorage.setString(KEY_CAUSES_JSON, json.encodeToString(current))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteCause(causeId: String) {
        val updated = _causes.value.filterNot { it.id == causeId }
        _causes.value = updated
        try {
            settingsStorage.setString(KEY_CAUSES_JSON, json.encodeToString(updated))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getActiveCauses(): List<DonationCause> {
        return _causes.value.filter { it.isActive }.sortedBy { it.sortOrder }
    }

    // --- BANK & UPI SETTINGS ---
    private fun loadBankSettings(): BankUpiSettings {
        val saved = settingsStorage.getString(KEY_BANK_SETTINGS_JSON, "")
        if (saved.isNotBlank()) {
            try {
                return json.decodeFromString(saved)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return BankUpiSettings()
    }

    fun saveBankSettings(settings: BankUpiSettings) {
        val updated = settings.copy(updatedAt = 1758000000000L)
        _bankSettings.value = updated
        try {
            settingsStorage.setString(KEY_BANK_SETTINGS_JSON, json.encodeToString(updated))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- TRANSACTIONS & RECEIPTS ---
    private fun loadTransactions(): List<DonationTransaction> {
        val saved = settingsStorage.getString(KEY_TRANSACTIONS_JSON, "")
        if (saved.isNotBlank()) {
            try {
                return json.decodeFromString(saved)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return emptyList()
    }

    private fun persistTransactions(list: List<DonationTransaction>) {
        _transactions.value = list
        try {
            settingsStorage.setString(KEY_TRANSACTIONS_JSON, json.encodeToString(list))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun createDonationTransaction(
        donorName: String,
        mobileNumber: String,
        gotraCity: String,
        cause: DonationCause,
        amount: Double,
        paymentMethod: PaymentMethod,
        utrNumber: String = ""
    ): DonationTransaction {
        val now = 1758000000000L
        val randomNum = (100000..999999).random()
        val receiptNo = "SJD-DON-2026-$randomNum"
        val txnId = "TXN-$randomNum"

        // Rule: Direct UPI/QR/Bank payments without auto-verify gateway default to PENDING status
        val initialStatus = if (utrNumber.isNotBlank()) DonationStatus.PENDING else DonationStatus.INITIATED

        val transaction = DonationTransaction(
            id = txnId,
            receiptNumber = receiptNo,
            donorName = donorName.ifBlank { "श्रद्धालु" },
            mobileNumber = mobileNumber,
            gotraCity = gotraCity,
            sevaCauseId = cause.id,
            sevaCauseName = cause.titleHindi,
            amount = amount,
            paymentMethod = paymentMethod,
            paymentStatus = initialStatus,
            utrNumber = utrNumber,
            createdAt = now
        )

        val current = _transactions.value.toMutableList()
        current.add(0, transaction)
        persistTransactions(current)
        return transaction
    }

    fun updateUtrNumber(transactionId: String, utrNumber: String): DonationTransaction? {
        val current = _transactions.value.toMutableList()
        val index = current.indexOfFirst { it.id == transactionId }
        if (index >= 0) {
            val existing = current[index]
            val updated = existing.copy(
                utrNumber = utrNumber,
                paymentStatus = DonationStatus.PENDING
            )
            current[index] = updated
            persistTransactions(current)
            return updated
        }
        return null
    }

    fun verifyAndApprovePayment(
        transactionId: String,
        verifiedBy: String = "एडमिन",
        note: String = "भुगतान बैंक/UPI में सत्यापित हुआ।"
    ): DonationTransaction? {
        val current = _transactions.value.toMutableList()
        val index = current.indexOfFirst { it.id == transactionId }
        if (index >= 0) {
            val existing = current[index]
            val updated = existing.copy(
                paymentStatus = DonationStatus.SUCCESS,
                verificationNote = note,
                verifiedBy = verifiedBy,
                verifiedAt = 1758000000000L
            )
            current[index] = updated
            persistTransactions(current)
            return updated
        }
        return null
    }

    fun rejectPayment(transactionId: String, note: String = "सत्यापन विफल"): DonationTransaction? {
        val current = _transactions.value.toMutableList()
        val index = current.indexOfFirst { it.id == transactionId }
        if (index >= 0) {
            val existing = current[index]
            val updated = existing.copy(
                paymentStatus = DonationStatus.REJECTED,
                verificationNote = note,
                verifiedBy = "एडमिन",
                verifiedAt = 1758000000000L
            )
            current[index] = updated
            persistTransactions(current)
            return updated
        }
        return null
    }

    // --- ANALYTICS ---
    fun calculateTotalVerifiedAmount(): Double {
        return _transactions.value
            .filter { it.paymentStatus == DonationStatus.SUCCESS }
            .sumOf { it.amount }
    }

    fun calculateVerifiedCount(): Int {
        return _transactions.value.count { it.paymentStatus == DonationStatus.SUCCESS }
    }

    fun calculatePendingCount(): Int {
        return _transactions.value.count { it.paymentStatus == DonationStatus.PENDING }
    }
}
