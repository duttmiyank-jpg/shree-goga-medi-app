package com.example.miyank.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable
    data object Splash : Screen
    @Serializable
    data object AccessSelection : Screen
    @Serializable
    data object DarbarisAuth : Screen
    @Serializable
    data object AdminAuth : Screen
    @Serializable
    data object LanguageSelection : Screen
    @Serializable
    data object Welcome : Screen
    @Serializable
    data object Home : Screen
    @Serializable
    data object Events : Screen
    @Serializable
    data object Gallery : Screen
    @Serializable
    data object LiveDarshan : Screen
    @Serializable
    data object Yatra : Screen
    @Serializable
    data object Donation : Screen
    @Serializable
    data object Profile : Screen
    @Serializable
    data object Bhajans : Screen
    @Serializable
    data object NotificationHistory : Screen
    @Serializable
    data object DarshanTimings : Screen
    @Serializable
    data object AboutTemple : Screen
    @Serializable
    data object Panchang : Screen
    @Serializable
    data object VirtualPuja : Screen
    @Serializable
    data object TempleLocation : Screen
    @Serializable
    data object MandirCalendar : Screen
    @Serializable
    data object Settings : Screen
    @Serializable
    data object RashiInput : Screen
    @Serializable
    data object RashiResult : Screen
    @Serializable
    data object DarbarStore : Screen
    @Serializable
    data class ProductDetail(val productId: String = "") : Screen
    @Serializable
    data object Cart : Screen
    @Serializable
    data object CheckoutAddress : Screen
    @Serializable
    data object Checkout : Screen
    @Serializable
    data class OrderSuccess(val orderId: String = "") : Screen
    @Serializable
    data object MyOrders : Screen
    @Serializable
    data class OrderDetail(val orderId: String = "") : Screen
    @Serializable
    data object AdminStore : Screen
    @Serializable
    data object MyDonationReceipts : Screen
    @Serializable
    data class DonationReceipt(val transactionId: String = "") : Screen
    @Serializable
    data class DonationPaymentStatus(val transactionId: String = "") : Screen
    @Serializable
    data object AdminDonation : Screen
    @Serializable
    data object BhajanLyrics : Screen
    @Serializable
    data class BhajanLyricsDetail(val title: String, val lyrics: String) : Screen
    @Serializable
    data object Announcements : Screen
    @Serializable
    data object ContactUs : Screen
    @Serializable
    data object AdminPanel : Screen
    @Serializable
    data object AdminAnnouncements : Screen
    @Serializable
    data object AdminMandirCalendar : Screen
    @Serializable
    data object AdminBhajans : Screen
    @Serializable
    data object AdminEvents : Screen
    @Serializable
    data object AdminSendNotification : Screen
    @Serializable
    data object AdminGallery : Screen
    @Serializable
    data object AdminUserQueries : Screen
    @Serializable
    data object AdminAnalytics : Screen
    @Serializable
    data object AdminBanners : Screen
    @Serializable
    data object AdminAppSettings : Screen
    @Serializable
    data object AdminLiveStream : Screen
    @Serializable
    data object AdminMusicSettings : Screen
    @Serializable
    data object AdminSuvichar : Screen
    @Serializable
    data class SuvicharPreview(val suvicharId: String = "") : Screen
    @Serializable
    data object BlogListing : Screen
    @Serializable
    data class BlogDetail(val blogId: String = "") : Screen
    @Serializable
    data object AdminBlog : Screen
    @Serializable
    data object Darbaris : Screen
    @Serializable
    data object DarbarisPanel : Screen
    @Serializable
    data object AdminDarbaris : Screen
    @Serializable
    data object Puja : Screen
    @Serializable
    data object AdminPuja : Screen
    @Serializable
    data class YatraBooking(val yatraId: String = "") : Screen
    @Serializable
    data object AdminYatra : Screen
    @Serializable
    data object Parcha : Screen
    @Serializable
    data class ParchaOtpVerification(val registeredMobile: String = "") : Screen
    @Serializable
    data class ParchaDetail(val parchaId: String = "") : Screen
    @Serializable
    data class AdminParcha(val prefillQueryId: String = "") : Screen
    @Serializable
    data object Sponsored : Screen
    @Serializable
    data object Suggestions : Screen
    @Serializable
    data object EngineersMessage : Screen
    @Serializable
    data object ChalisaStotram : Screen
    @Serializable
    data object AdminSponsored : Screen
    @Serializable
    data object AdminSuggestions : Screen
    @Serializable
    data object AdminDevotionalContent : Screen
}
