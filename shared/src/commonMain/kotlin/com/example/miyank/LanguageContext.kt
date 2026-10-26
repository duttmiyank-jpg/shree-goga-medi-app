package com.example.miyank

import androidx.compose.runtime.*

enum class Language {
    ENGLISH, HINDI
}

object LanguageManager {
    private val settings = SettingsManager()
    private const val LANG_KEY = "app_language"
    private const val LANG_SELECTED_KEY = "is_language_selected"

    var currentLanguage by mutableStateOf(Language.ENGLISH)
        private set

    var isLanguageSelected by mutableStateOf(false)
        private set

    init {
        val savedLang = settings.getString(LANG_KEY, Language.ENGLISH.name)
        currentLanguage = try { Language.valueOf(savedLang) } catch(e: Exception) { Language.ENGLISH }

        val savedSelected = settings.getString(LANG_SELECTED_KEY, "false")
        isLanguageSelected = savedSelected == "true"
    }

    fun selectLanguage(language: Language) {
        currentLanguage = language
        isLanguageSelected = true
        settings.setString(LANG_KEY, language.name)
        settings.setString(LANG_SELECTED_KEY, "true")
    }

    fun toggleLanguage() {
        currentLanguage = if (currentLanguage == Language.HINDI) Language.ENGLISH else Language.HINDI
        settings.setString(LANG_KEY, currentLanguage.name)
    }
}

val LocalStrings = staticCompositionLocalOf { EnglishStrings }

@Composable
fun ProvideStrings(content: @Composable () -> Unit) {
    val strings = if (LanguageManager.currentLanguage == Language.HINDI) HindiStrings else EnglishStrings
    CompositionLocalProvider(LocalStrings provides strings, content = content)
}

interface AppStrings {
    val appName: String
    val welcomeTitle: String
    val continueToDarbar: String
    val splashSecondary: String
    val splashGuru: String
    val tagline: String
    val settingsTitle: String
    val languageLabel: String
    val currentLanguageName: String
    val quickAccess: String
    val moreServices: String
    val liveDarshan: String
    val virtualAarti: String
    val bhajans: String
    val gallery: String
    val panchang: String
    val myRashi: String
    val yatra: String
    val history1: String
    val history2: String
    val history3: String
    val footer1: String
    val footer2: String
    val footer3: String
    val notificationsSection: String
    val generalSection: String
    val securitySection: String
    val pushNotifications: String
    val eventReminders: String
    val notificationsSubtitle: String
    val remindersSubtitle: String
    val themeLabel: String
    val themeSubtitle: String
    val privacyPolicy: String
    val catBabaJi: String
    val catShringar: String
    val catTemple: String
    val catAarti: String
    val catEvents: String
    val catYatra: String
    val galleryTitle: String
    val darshanTitle: String
    val aartiTimings: String
    val templeOpenSummary: String
    val timingMangala: String
    val timingBhog: String
    val timingSandhya: String
    val timingShayan: String
    val timingMorning: String
    val timingEvening: String
    val timingClosedNote: String
    val darshanWindow: String
    val aboutTitle: String
    val historyTitle: String
    val significanceTitle: String
    val locationTitle: String
    val historyContent: String
    val significanceContent: String
    val locationContent: String
    val eventsTitle: String
    val auspiciousDates: String
    val upcomingEvents: String
    val comingSoon: String
    val comingSoonNote: String
    val bhajanKirtan: String
    val specialAarti: String
    val everySunday: String
    val everyTuesday: String
    val onwards: String
    val mainDarbar: String
    val bhajanDesc: String
    val aartiDesc: String
    val greeting: String
    val guest: String
    val bannerJagran: String
    val bannerDarshan: String
    val bannerUtsav: String
    val bannerTitle1: String
    val bannerTitle2: String
    val bannerTitle3: String
    val goldenQuote: String
    val quoteAuthor: String
    val darshanTimings: String
    val templeHistory: String
    val allEvents: String
    val templeLocation: String
    val mandirCalendar: String
    val bhajanLyrics: String
    val announcements: String
    val contactUs: String
    val liveActiveTitle: String
    val liveActiveDesc: String
    val termsPrivacy: String
    val editProfile: String
    val guestUser: String
    val myActivities: String
    val donationHistory: String
    val myTravels: String
    val notifications: String
    val settings: String
    val administrative: String
    val adminDashboard: String
    val logout: String
    val livePlatformTitle: String
    val livePlatformSubtitle: String
    val youtubeLive: String
    val instagramLive: String
    val facebookLive: String
    val liveNow: String
    val unavailable: String
    val watch: String
    val streamQualityNote: String
    val noLiveAvailable: String
    val virtualPujaTitle: String
    val jaiBabaGoga: String
    val deepakLit: String
    val lightDeepak: String
    val stopAarti: String
    val startAarti: String
    val offerFlowers: String
    val pujaHomeNote: String
    val navHome: String
    val navEvents: String
    val navGallery: String
    val navDonate: String
    val navProfile: String

    val chooseLanguageTitle: String
    val chooseLanguageSubtitle: String
    val langEnglishName: String
    val langHindiName: String
    val continueText: String

    val todaySuvicharTitle: String
    val suvicharEmptyTitle: String
    val suvicharEmptyNote: String
    val manageSuvicharTitle: String
    val scheduleSuvichar: String
    val downloadSuvichar: String
    val shareSuvichar: String
    val suvicharSavedSuccess: String
    val suvicharShareText: String

    val aboutDarbarTitle: String
    val aboutDarbarSubtitle: String
    val manageAboutDarbarTitle: String
    val readArticle: String

    val parcha: String
    val parchaManagement: String
    val noParchaAvailable: String
    val unableToLoadParcha: String
    val sendParcha: String
    val markAsRead: String
    val unreadStatus: String
    val readStatus: String
    val parchaSubject: String
    val parchaProblem: String
    val parchaGuidance: String
    val parchaUpay: String
    val parchaKaram: String
    val parchaAdditional: String
    val sendAsParcha: String

    val parchaVerificationTitle: String
    val parchaVerificationSubtitle: String
    val enterMobileNumberLabel: String
    val enterOtpLabel: String
    val verifyOtpButton: String
    val resendOtpButton: String
    val invalidOtpError: String
    val yourDarbarGuidance: String
}

val HindiStrings = object : AppStrings {
    override val appName = "श्री जामनीवाला दरबार"
    override val welcomeTitle = "श्री जामनीवाला दरबार"
    override val continueToDarbar = "दरबार में प्रवेश करें"
    override val splashSecondary = "जय श्री गोगा जी महाराज"
    override val splashGuru = "सत् गुरु बाबा सोम दत्त जी"
    override val tagline = "श्रद्धा • सेवा • दर्शन"
    override val settingsTitle = "एप्प सेटिंग्स"
    override val languageLabel = "भाषा"
    override val currentLanguageName = "हिंदी"
    override val quickAccess = "त्वरित सेवाएँ"
    override val moreServices = "अन्य सेवाएँ"
    override val liveDarshan = "लाइव दर्शन"
    override val virtualAarti = "वर्चुअल आरती"
    override val bhajans = "भजन"
    override val gallery = "गैलरी"
    override val panchang = "पंचांग"
    override val myRashi = "मेरी राशि"
    override val yatra = "यात्रा"
    override val history1 = "श्री जामनीवाला दरबार का निर्माण सन 2007 में बाबा जी श्री सोम दत्त जी के कर-कमलों से हुआ।"
    override val history2 = "श्री सोम दत्त जी ने अपना संपूर्ण जीवन बाबा जी के चरणों में समर्पित कर दिया।"
    override val history3 = "उनके पावन मार्ग का अनुसरण करते हुए, मैं भी बाबा जी के चरणों में सिमरन करने का एक छोटा-सा प्रयास कर रहा हूँ।"
    override val footer1 = "जय शिव अवतार गुरु गोरक्षनाथ जी"
    override val footer2 = "जय श्री जाहरवीर गोगा जी महाराज"
    override val footer3 = "जय माँ बाला सुंदरी जी"
    override val notificationsSection = "सूचनाएं"
    override val generalSection = "सामान्य"
    override val securitySection = "सुरक्षा एवं अन्य"
    override val pushNotifications = "पुश नोटिफिकेशन"
    override val eventReminders = "आयोजन रिमाइंडर"
    override val notificationsSubtitle = "मंदिर के अपडेट और सूचनाएं प्राप्त करें"
    override val remindersSubtitle = "आगामी आयोजनों के लिए अलर्ट"
    override val themeLabel = "थीम"
    override val themeSubtitle = "डार्क थीम"
    override val privacyPolicy = "गोपनीयता नीति"
    override val catBabaJi = "बाबा जी"
    override val catShringar = "श्रृंगार"
    override val catTemple = "मंदिर"
    override val catAarti = "आरती"
    override val catEvents = "आयोजन"
    override val catYatra = "यात्रा"
    override val galleryTitle = "दिव्य गैलरी"
    override val darshanTitle = "दर्शन का समय"
    override val aartiTimings = "आरती का समय"
    override val templeOpenSummary = "मंदिर सभी भक्तों के लिए प्रतिदिन सुबह 5:00 बजे से रात 9:00 बजे तक खुला रहता है।"
    override val timingMangala = "मंगला आरती"
    override val timingBhog = "भोग आरती"
    override val timingSandhya = "संध्या आरती"
    override val timingShayan = "शयन आरती"
    override val timingMorning = "सुबह"
    override val timingEvening = "शाम"
    override val timingClosedNote = "* मंदिर के द्वार दोपहर 12:00 बजे से शाम 4:00 बजे तक बंद रहते हैं।"
    override val darshanWindow = "दर्शन विंडो"
    override val aboutTitle = "मंदिर का इतिहास"
    override val historyTitle = "इतिहास"
    override val significanceTitle = "महत्व"
    override val locationTitle = "स्थान एवं पहुंच"
    override val historyContent = "श्री जामनीवाला दरबार मंदिर एक प्राचीन और पवित्र धार्मिक स्थल है। यह मंदिर बाबा गोगा जी महाराज (जाहरवीर) को समर्पित है। सदियों से यह स्थान श्रद्धालुओं की अटूट श्रद्धा का केंद्र रहा है। ऐसी मान्यता है कि यहाँ आने वाले हर भक्त की मनोकामना पूर्ण होती है।"
    override val significanceContent = "बाबा गोगा जी महाराज को सांपों के देवता और रक्षक के रूप में पूजा जाता है। मंदिर में आयोजित होने वाली विशेष आरती और जागरण में देश के विभिन्न कोनों से श्रद्धालु पहुँचते हैं। भाद्रपद माह (गोगा नवमी) के दौरान यहाँ भव्य मेले का आयोजन किया जाता है।"
    override val locationContent = "मंदिर सुंदर और शांत वातावरण में स्थित है, जो शहर के शोर-शराबे से दूर आध्यात्मिक शांति प्रदान करता है। श्रद्धालु यहाँ बस, रेल या निजी वाहन द्वारा आसानी से पहुँच सकते हैं। अधिक जानकारी के लिए मंदिर कार्यालय में संपर्क किया जा सकता है।"
    override val eventsTitle = "आयोजन और कैलेंडर"
    override val auspiciousDates = "शुभ मुहूर्त"
    override val upcomingEvents = "आगामी आयोजन"
    override val comingSoon = "जल्द आ रहा है"
    override val comingSoonNote = "आने वाले महीनों के प्रमुख आयोजनों को जल्द ही यहाँ अपडेट किया जाएगा।"
    override val bhajanKirtan = "भजन और कीर्तन"
    override val specialAarti = "विशेष आरती"
    override val everySunday = "प्रत्येक रविवार"
    override val everyTuesday = "प्रत्येक मंगलवार"
    override val onwards = "आगे"
    override val mainDarbar = "मुख्य दरबार"
    override val bhajanDesc = "हर रविवार बाबा जी के भजन की दिव्य अनुभूति प्राप्त करें। सभी भक्त सादर आमंत्रित हैं।"
    override val aartiDesc = "मंगलवार की विशेष चौकी और बाबा जी की महा आरती।"
    override val greeting = "जय गोगा जी,"
    override val guest = "अतिथि"
    override val bannerJagran = "जागरण"
    override val bannerDarshan = "दर्शन"
    override val bannerUtsav = "उत्सव"
    override val bannerTitle1 = "महा जागरण महोत्सव"
    override val bannerTitle2 = "बाबा गोगा जी भव्य दर्शन"
    override val bannerTitle3 = "विशेष धार्मिक आयोजन"
    override val goldenQuote = "जहाँ भक्ति है, वहाँ शक्ति है। श्रद्धा से किया गया हर कार्य फलदायी होता है।"
    override val quoteAuthor = "— श्री गोगा माड़ी वचन"
    override val darshanTimings = "दर्शन का समय"
    override val templeHistory = "मंदिर का इतिहास"
    override val allEvents = "सभी आयोजन"
    override val templeLocation = "मंदिर की लोकेशन"
    override val mandirCalendar = "मंदिर कैलेंडर"
    override val bhajanLyrics = "भजन लिरिक्स"
    override val announcements = "घोषणाएं"
    override val contactUs = "संपर्क करें"
    override val liveActiveTitle = "लाइव दर्शन शुरू है!"
    override val liveActiveDesc = "बाबा जी के लाइव दर्शन के लिए अभी जुड़ें। 🙏"
    override val termsPrivacy = "जारी रखकर, आप हमारी शर्तों और गोपनीयता नीति से सहमत होते हैं।"
    override val editProfile = "प्रोफ़ाइल संपादित करें"
    override val guestUser = "अतिथि उपयोगकर्ता"
    override val myActivities = "मेरी गतिविधियाँ"
    override val donationHistory = "दान का इतिहास"
    override val myTravels = "मेरी यात्राएं"
    override val notifications = "सूचनाएं"
    override val settings = "सेटिंग्स"
    override val administrative = "प्रशासनिक"
    override val adminDashboard = "एडमिन डैशबोर्ड"
    override val logout = "लॉगआउट"
    override val livePlatformTitle = "लाइव दर्शन"
    override val livePlatformSubtitle = "बाबा जी के लाइव दर्शन"
    override val youtubeLive = "यूट्यूब लाइव"
    override val instagramLive = "इंस्टाग्राम लाइव"
    override val facebookLive = "फेसबुक लाइव"
    override val liveNow = "अभी लाइव"
    override val unavailable = "अनुपलब्ध"
    override val watch = "देखें"
    override val streamQualityNote = "स्ट्रीम की गुणवत्ता आपके इंटरनेट कनेक्शन पर निर्भर करती है।"
    override val noLiveAvailable = "वर्तमान में कोई लाइव दर्शन उपलब्ध नहीं है।"

    override val virtualPujaTitle = "वर्चुअल आरती"
    override val jaiBabaGoga = "जय बाबा गोगा जी महाराज"
    override val deepakLit = "दीपक प्रज्वलित"
    override val lightDeepak = "दीपक जलाएं"
    override val stopAarti = "आरती रोकें"
    override val startAarti = "आरती शुरू करें"
    override val offerFlowers = "पुष्पांजलि अर्पित करें"
    override val pujaHomeNote = "घर बैठे बाबा जी की पूजा करें और आशीर्वाद प्राप्त करें।"

    override val navHome = "मुख्य"
    override val navEvents = "आयोजन"
    override val navGallery = "गैलरी"
    override val navDonate = "दान"
    override val navProfile = "प्रोफ़ाइल"

    override val chooseLanguageTitle = "अपनी भाषा चुनें"
    override val chooseLanguageSubtitle = "अपनी पसंदीदा भाषा का चयन करें"
    override val langEnglishName = "English"
    override val langHindiName = "हिन्दी"
    override val continueText = "आगे बढ़ें"

    override val todaySuvicharTitle = "आज का सुविचार"
    override val suvicharEmptyTitle = "आज का सुविचार अभी उपलब्ध नहीं है"
    override val suvicharEmptyNote = "कृपया कुछ समय बाद पुनः देखें 🙏"
    override val manageSuvicharTitle = "आज का सुविचार प्रबंधन"
    override val scheduleSuvichar = "सुविचार शेड्यूल करें"
    override val downloadSuvichar = "गैलरी में सेव करें"
    override val shareSuvichar = "शेयर करें"
    override val suvicharSavedSuccess = "सुविचार आपके गैलरी में सेव हो गया 🙏"
    override val suvicharShareText = "🙏 आज का सुविचार\n\nश्री जामनीवाला दरबार की ओर से\n\nजय श्री जामनीवाला दरबार 🙏"

    override val aboutDarbarTitle = "दरबार के विषय में"
    override val aboutDarbarSubtitle = "दरबार का इतिहास, दरबारी एवं धार्मिक जानकारी"
    override val manageAboutDarbarTitle = "दरबार जानकारी प्रबंधन"
    override val readArticle = "पढ़ें ➔"

    override val parcha = "पर्चा"
    override val parchaManagement = "पर्चा प्रबंधन"
    override val noParchaAvailable = "कोई पर्चा उपलब्ध नहीं है"
    override val unableToLoadParcha = "पर्चा लोड नहीं हो सका। कृपया पुनः प्रयास करें।"
    override val sendParcha = "पर्चा भेजें"
    override val markAsRead = "पढ़ा हुआ चिह्नित करें"
    override val unreadStatus = "अपठित"
    override val readStatus = "पठित"
    override val parchaSubject = "विषय"
    override val parchaProblem = "श्रद्धालु समस्या"
    override val parchaGuidance = "दरबार मार्गदर्शन"
    override val parchaUpay = "उपाय"
    override val parchaKaram = "कर्म / सेवा"
    override val parchaAdditional = "अतिरिक्त मार्गदर्शन"
    override val sendAsParcha = "पर्चा के रूप में भेजें"

    override val parchaVerificationTitle = "पर्चा सत्यापन"
    override val parchaVerificationSubtitle = "आपके पंजीकृत मोबाइल नंबर पर भेजा गया OTP दर्ज करें।"
    override val enterMobileNumberLabel = "पंजीकृत मोबाइल नंबर"
    override val enterOtpLabel = "6-अंकों का OTP दर्ज करें"
    override val verifyOtpButton = "OTP सत्यापित करें"
    override val resendOtpButton = "OTP पुनः भेजें"
    override val invalidOtpError = "अमान्य OTP कोड! कृपया 6-अंकों का OTP दर्ज करें।"
    override val yourDarbarGuidance = "आपके दरबार का मार्गदर्शन"
}

val EnglishStrings = object : AppStrings {
    override val appName = "Shree Jamniwala Darbar"
    override val welcomeTitle = "Shree Jamniwala Darbar"
    override val continueToDarbar = "Continue to Darbar"
    override val splashSecondary = "Jai Shree Goga Ji Maharaj"
    override val splashGuru = "Sat Guru Baba Som Dutt Ji"
    override val tagline = "Faith • Service • Devotion"
    override val settingsTitle = "App Settings"
    override val languageLabel = "Language"
    override val currentLanguageName = "English"
    override val quickAccess = "Quick Access"
    override val moreServices = "More Services"
    override val liveDarshan = "Live Darshan"
    override val virtualAarti = "Virtual Aarti"
    override val bhajans = "Bhajans"
    override val gallery = "Gallery"
    override val panchang = "Panchang"
    override val myRashi = "My Rashi"
    override val yatra = "Yatra"
    override val history1 = "The construction of Shree Jamniwala Darbar was done in 2007 by the hands of Baba Ji Shree Som Dutt Ji."
    override val history2 = "Shree Som Dutt Ji dedicated his entire life at the feet of Baba Ji."
    override val history3 = "Following his holy path, I am also making a small effort to meditate at the feet of Baba Ji."
    override val footer1 = "Jai Shiv Avtar Guru Gorakhnath Ji"
    override val footer2 = "Jai Shree Jaharveer Goga Ji Maharaj"
    override val footer3 = "Jai Maa Bala Sundari Ji"
    override val notificationsSection = "Notifications"
    override val generalSection = "General"
    override val securitySection = "Security & More"
    override val pushNotifications = "Push Notifications"
    override val eventReminders = "Event Reminders"
    override val notificationsSubtitle = "Receive temple updates and notifications"
    override val remindersSubtitle = "Alerts for upcoming events"
    override val themeLabel = "Theme"
    override val themeSubtitle = "Dark Theme"
    override val privacyPolicy = "Privacy Policy"
    override val catBabaJi = "Baba Ji"
    override val catShringar = "Shringar"
    override val catTemple = "Temple"
    override val catAarti = "Aarti"
    override val catEvents = "Events"
    override val catYatra = "Yatra"
    override val galleryTitle = "Divine Gallery"
    override val darshanTitle = "Darshan Timings"
    override val aartiTimings = "Aarti Timings"
    override val templeOpenSummary = "Temple is open daily from 5:00 AM to 9:00 PM for all devotees."
    override val timingMangala = "Mangala Aarti"
    override val timingBhog = "Bhog Aarti"
    override val timingSandhya = "Sandhya Aarti"
    override val timingShayan = "Shayan Aarti"
    override val timingMorning = "Morning"
    override val timingEvening = "Evening"
    override val timingClosedNote = "* Temple doors remain closed from 12:00 PM to 4:00 PM."
    override val darshanWindow = "Darshan Window"
    override val aboutTitle = "Temple History"
    override val historyTitle = "History"
    override val significanceTitle = "Significance"
    override val locationTitle = "Location"
    override val historyContent = "Shree Jamniwala Darbar Temple is an ancient and sacred religious site. This temple is dedicated to Baba Goga Ji Maharaj (Jaharveer). For centuries, this place has been the center of unwavering faith of the devotees. It is believed that every wish of the devotee who comes here is fulfilled."
    override val significanceContent = "Baba Goga Ji Maharaj is worshipped as the deity and protector of snakes. Devotees from different corners of the country reach the special Aarti and Jagran organized in the temple. A grand fair is organized here during the month of Bhadrapada (Goga Navami)."
    override val locationContent = "The temple is located in a beautiful and peaceful environment, providing spiritual peace away from the noise of the city. Devotees can easily reach here by bus, rail or private vehicle. For more information, the temple office can be contacted."
    override val eventsTitle = "Events & Calendar"
    override val auspiciousDates = "Auspicious Dates"
    override val upcomingEvents = "Upcoming Events"
    override val comingSoon = "Coming Soon"
    override val comingSoonNote = "More major events for the coming months will be updated here soon."
    override val bhajanKirtan = "Bhajan & Kirtan"
    override val specialAarti = "Special Aarti"
    override val everySunday = "Every Sunday"
    override val everyTuesday = "Every Tuesday"
    override val onwards = "onwards"
    override val mainDarbar = "Main Darbar"
    override val bhajanDesc = "Experience the divine vibes of Baba Ji's bhajan every Sunday. All devotees are welcome."
    override val aartiDesc = "Tuesday special chouki and Baba Ji's maha aarti."
    override val greeting = "Jai Goga Ji,"
    override val guest = "Guest"
    override val bannerJagran = "Jagran"
    override val bannerDarshan = "Darshan"
    override val bannerUtsav = "Utsav"
    override val bannerTitle1 = "Maha Jagran Mahotsav"
    override val bannerTitle2 = "Baba Goga Ji Grand Darshan"
    override val bannerTitle3 = "Special Religious Event"
    override val goldenQuote = "Where there is devotion, there is power. Every work done with faith is fruitful."
    override val quoteAuthor = "— Shree Goga Medi Vachan"
    override val darshanTimings = "Darshan Timings"
    override val templeHistory = "Temple History"
    override val allEvents = "All Events"
    override val templeLocation = "Temple Location"
    override val mandirCalendar = "Mandir Calendar"
    override val bhajanLyrics = "Bhajan Lyrics"
    override val announcements = "Announcements"
    override val contactUs = "Contact Us"
    override val liveActiveTitle = "Live Darshan is Active!"
    override val liveActiveDesc = "Join now for Baba Ji's live darshan. 🙏"
    override val termsPrivacy = "By continuing, you agree to our Terms and Privacy Policy."
    override val editProfile = "Edit Profile"
    override val guestUser = "Guest User"
    override val myActivities = "My Activities"
    override val donationHistory = "Donation History"
    override val myTravels = "My Travels"
    override val notifications = "Notifications"
    override val settings = "Settings"
    override val administrative = "Administrative"
    override val adminDashboard = "Admin Dashboard"
    override val logout = "LOGOUT"
    override val livePlatformTitle = "Live Darshan"
    override val livePlatformSubtitle = "Baba Ji's live darshan"
    override val youtubeLive = "YouTube Live"
    override val instagramLive = "Instagram Live"
    override val facebookLive = "Facebook Live"
    override val liveNow = "LIVE NOW"
    override val unavailable = "Unavailable"
    override val watch = "Watch"
    override val streamQualityNote = "Stream quality depends on your connection."
    override val noLiveAvailable = "Currently, no live darshan is available."

    override val virtualPujaTitle = "Virtual Aarti"
    override val jaiBabaGoga = "Jai Baba Goga Ji Maharaj"
    override val deepakLit = "Deepak Lit"
    override val lightDeepak = "Light Deepak"
    override val stopAarti = "Stop Aarti"
    override val startAarti = "Start Aarti"
    override val offerFlowers = "Offer Pushpanjali"
    override val pujaHomeNote = "Perform Baba Ji's puja from home and receive blessings."

    override val navHome = "Home"
    override val navEvents = "Events"
    override val navGallery = "Gallery"
    override val navDonate = "Donate"
    override val navProfile = "Profile"

    override val chooseLanguageTitle = "Choose Your Language"
    override val chooseLanguageSubtitle = "Select your preferred language"
    override val langEnglishName = "English"
    override val langHindiName = "हिन्दी"
    override val continueText = "Continue"

    override val todaySuvicharTitle = "Today's Suvichar"
    override val suvicharEmptyTitle = "Today's Suvichar is not available right now"
    override val suvicharEmptyNote = "Please check back again after some time 🙏"
    override val manageSuvicharTitle = "Manage Daily Suvichar"
    override val scheduleSuvichar = "Schedule Suvichar"
    override val downloadSuvichar = "Save to Gallery"
    override val shareSuvichar = "Share"
    override val suvicharSavedSuccess = "Suvichar saved to your gallery 🙏"
    override val suvicharShareText = "🙏 Today's Suvichar\n\nFrom Shree Jamniwala Darbar\n\nJai Shree Jamniwala Darbar 🙏"

    override val aboutDarbarTitle = "About Darbar"
    override val aboutDarbarSubtitle = "History, Sevadars & Temple Activities"
    override val manageAboutDarbarTitle = "Manage About Darbar"
    override val readArticle = "Read Article ➔"

    override val parcha = "Parcha"
    override val parchaManagement = "Parcha Management"
    override val noParchaAvailable = "No Parcha available"
    override val unableToLoadParcha = "Unable to load Parcha. Please try again."
    override val sendParcha = "Send Parcha"
    override val markAsRead = "Mark as Read"
    override val unreadStatus = "Unread"
    override val readStatus = "Read"
    override val parchaSubject = "Subject"
    override val parchaProblem = "Devotee Concern"
    override val parchaGuidance = "Darbar Guidance"
    override val parchaUpay = "Upay"
    override val parchaKaram = "Karam / Seva"
    override val parchaAdditional = "Additional Guidance"
    override val sendAsParcha = "Send as Parcha"

    override val parchaVerificationTitle = "Parcha Verification"
    override val parchaVerificationSubtitle = "Enter the OTP sent to your registered mobile number."
    override val enterMobileNumberLabel = "Registered Mobile Number"
    override val enterOtpLabel = "Enter 6-digit OTP"
    override val verifyOtpButton = "Verify OTP"
    override val resendOtpButton = "Resend OTP"
    override val invalidOtpError = "Invalid OTP code. Please enter 6 digits."
    override val yourDarbarGuidance = "Your Darbar Guidance"
}
