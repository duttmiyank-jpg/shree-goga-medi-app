package com.example.miyank

object AppShareConfig {
    fun getOfficialAppLink(packageName: String = "com.example.miyank"): String {
        return "https://play.google.com/store/apps/details?id=$packageName"
    }

    fun getDeepLinkUrl(contentType: String, contentId: String, packageName: String = "com.example.miyank"): String {
        return "https://play.google.com/store/apps/details?id=$packageName&referrer=utm_source%3Dapp_share%26utm_content%3D${contentType}_${contentId}"
    }

    fun buildShareFooter(appLink: String = getOfficialAppLink()): String {
        return "\n\n📱 श्री जामनीवाला दरबार मोबाइल एप डाउनलोड करें:\n$appLink"
    }
}

object ShareManager {

    fun formatSuvicharShareMessage(
        quoteText: String,
        author: String,
        appLink: String = AppShareConfig.getOfficialAppLink()
    ): String {
        val baseMsg = if (quoteText.isNotEmpty()) {
            "जय श्री गोगा जी महाराज 🙏\n\nआज का दैनिक सुविचार:\n“$quoteText”\n— $author"
        } else {
            "जय श्री गोगा जी महाराज 🙏\n\nआज का दैनिक सुविचार\n— श्री जामनीवाला दरबार"
        }
        return baseMsg + AppShareConfig.buildShareFooter(appLink)
    }

    fun formatGalleryShareMessage(
        imageTitle: String,
        categoryTag: String,
        appLink: String = AppShareConfig.getOfficialAppLink()
    ): String {
        val titleText = if (imageTitle.isNotEmpty()) " - $imageTitle" else ""
        return "जय श्री गोगा जी महाराज 🙏\n\nश्री जामनीवाला दरबार की दिव्य झलक 🌸\n[$categoryTag]$titleText" +
                AppShareConfig.buildShareFooter(appLink)
    }

    fun formatAnnouncementShareMessage(
        title: String,
        dateStr: String,
        content: String,
        appLink: String = AppShareConfig.getOfficialAppLink()
    ): String {
        val contentPreview = if (content.length > 180) content.take(180) + "..." else content
        return "🛕 श्री जामनीवाला दरबार\n📢 महत्वपूर्ण घोषणा\n\n📌 $title\n🗓️ $dateStr\n\n$contentPreview" +
                AppShareConfig.buildShareFooter(appLink)
    }

    fun formatEventShareMessage(
        eventTitle: String,
        eventDate: String,
        eventLocation: String,
        appLink: String = AppShareConfig.getOfficialAppLink()
    ): String {
        return "🛕 श्री जामनीवाला दरबार\n🎉 आगामी धार्मिक कार्यक्रम\n\n📌 $eventTitle\n🗓️ तारीख: $eventDate\n📍 स्थान: $eventLocation" +
                AppShareConfig.buildShareFooter(appLink)
    }

    fun formatYatraShareMessage(
        yatraTitle: String,
        yatraDetails: String,
        appLink: String = AppShareConfig.getOfficialAppLink()
    ): String {
        return "🛕 श्री जामनीवाला दरबार\n🚩 श्री गोगामेड़ी बागढ़ यात्रा सूचना\n\n📌 $yatraTitle\n\n$yatraDetails" +
                AppShareConfig.buildShareFooter(appLink)
    }

    fun formatGeneralShareMessage(
        title: String,
        description: String,
        appLink: String = AppShareConfig.getOfficialAppLink()
    ): String {
        return "जय श्री गोगा जी महाराज 🙏\n\n🛕 श्री जामनीवाला दरबार\n📌 $title\n\n$description" +
                AppShareConfig.buildShareFooter(appLink)
    }

    fun formatDarshanTimingsShareMessage(
        morningOpening: String,
        morningClosing: String,
        eveningOpening: String,
        eveningClosing: String,
        aartis: List<AartiTimingItem>,
        isHindi: Boolean = true,
        appLink: String = AppShareConfig.getOfficialAppLink()
    ): String {
        val sb = StringBuilder()
        sb.append("🛕 श्री जामनीवाला दरबार\n")
        sb.append(if (isHindi) "दर्शन एवं आरती समय सारणी\n\n" else "Darshan & Aarti Schedule\n\n")

        if (morningOpening.isNotEmpty() && morningClosing.isNotEmpty()) {
            sb.append(if (isHindi) "🏛️ दरबार द्वार समय:\n" else "🏛️ Temple Windows:\n")
            sb.append(if (isHindi) "• प्रातः दर्शन: $morningOpening - $morningClosing\n" else "• Morning: $morningOpening - $morningClosing\n")
            sb.append(if (isHindi) "• सायं दर्शन: $eveningOpening - $eveningClosing\n" else "• Evening: $eveningOpening - $eveningClosing\n")
            sb.append(if (isHindi) "• विश्राम काल: $morningClosing से $eveningOpening तक\n\n" else "• Rest Period: $morningClosing to $eveningOpening\n\n")
        }

        val activeAartis = aartis.filter { it.isActive }.sortedBy { it.sortOrder }
        if (activeAartis.isNotEmpty()) {
            sb.append(if (isHindi) "🪔 दैनिक महाआरती समय:\n" else "🪔 Daily Aarti Schedule:\n")
            activeAartis.forEach { aarti ->
                val name = if (isHindi || aarti.nameEnglish.isEmpty()) aarti.nameHindi else aarti.nameEnglish
                sb.append("• $name: ${aarti.time}\n")
            }
        }

        return sb.toString() + AppShareConfig.buildShareFooter(appLink)
    }
}
