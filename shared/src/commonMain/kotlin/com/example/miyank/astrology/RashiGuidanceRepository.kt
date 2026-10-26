package com.example.miyank.astrology

object RashiGuidanceRepository {

    fun getGuidanceForRashi(rashi: Rashi): RashiGuidance {
        return when (rashi) {
            Rashi.MESHA -> RashiGuidance(
                rashi = Rashi.MESHA,
                rulingPlanet = "मंगल (Mars)",
                preferredColorsHindi = listOf("लाल (Red)", "केसरिया (Saffron)", "पीला (Yellow)"),
                preferredColorsEnglish = listOf("Red", "Saffron", "Yellow"),
                avoidColorsHindi = listOf("अत्यधिक गहरा काला (Very dark black)"),
                avoidColorsEnglish = listOf("Very dark black"),
                donationSuggestionsHindi = listOf(
                    "जरूरतमंदों को अन्न या गुड़ का दान",
                    "गौशाला में गौ सेवा (हरी घास या चना)",
                    "जल सेवा एवं मंदिर परिसर में स्वच्छता सेवा"
                ),
                donationSuggestionsEnglish = listOf(
                    "Food or jaggery donation to those in need",
                    "Gau Seva at gaushala (green grass/chana)",
                    "Water service and temple cleanliness"
                ),
                goodActionsHindi = listOf(
                    "हनुमान चालीसा का श्रद्धापूर्वक पाठ",
                    "माता-पिता एवं वरिष्ठ जनों का आशीर्वाद लें",
                    "मंदिर में सेवा अथवा दीप प्रज्वलन"
                ),
                goodActionsEnglish = listOf(
                    "Devotional recitation of Hanuman Chalisa",
                    "Seek blessings from parents and elders",
                    "Temple service or lighting a lamp"
                ),
                avoidActionsHindi = listOf(
                    "अनावश्यक क्रोध एवं जल्दबाजी से बचें",
                    "किसी को कटु वचन या अपशब्द न कहें",
                    "अहंकार या कठोर व्यवहार से दूर रहें"
                ),
                avoidActionsEnglish = listOf(
                    "Avoid unnecessary anger and haste",
                    "Avoid harsh or offensive speech",
                    "Stay away from arrogance or harsh behavior"
                ),
                devotionalPracticeHindi = "श्री हनुमान जी का स्मरण एवं 'ॐ नमः शिवाय' अथवा 'ॐ हं हनुमते नमः' मंत्र का जाप।",
                devotionalPracticeEnglish = "Remembrance of Lord Hanuman and chanting 'Om Namah Shivaya' or 'Om Hanumate Namah'.",
                mantraDevanagari = "ॐ हं हनुमते नमः",
                mantraTransliteration = "Om Hanumate Namah",
                sevaSuggestionsHindi = listOf(
                    "धार्मिक स्थल पर सफाई व जल वितरण सेवा",
                    "पक्षियों एवं बेजुबान जीवों के लिए जल-अन्न की व्यवस्था"
                ),
                sevaSuggestionsEnglish = listOf(
                    "Cleanliness and water distribution service at holy places",
                    "Providing food and water for birds and animals"
                )
            )

            Rashi.VRISHABHA -> RashiGuidance(
                rashi = Rashi.VRISHABHA,
                rulingPlanet = "शुक्र (Venus)",
                preferredColorsHindi = listOf("सफेद (White)", "हल्का गुलाबी (Pink)", "सिल्वर (Silver)"),
                preferredColorsEnglish = listOf("White", "Light Pink", "Silver"),
                avoidColorsHindi = listOf("गहरा तीखा लाल (Brilliant bright red)"),
                avoidColorsEnglish = listOf("Brilliant bright red"),
                donationSuggestionsHindi = listOf(
                    "सफेद वस्तु (अन्न, दूध, चावल) या वस्त्र दान",
                    "गौ सेवा (गौ माता को रोटी/अन्न देना)",
                    "कन्याओं अथवा जरूरतमंदों को फल वितरण"
                ),
                donationSuggestionsEnglish = listOf(
                    "White grains, milk, rice or cloth donation",
                    "Gau Seva (offering bread/food to cows)",
                    "Distributing fruits to needy children"
                ),
                goodActionsHindi = listOf(
                    "श्री लक्ष्मी-नारायण एवं माता भगवती का ध्यान",
                    "घर एवं पूजा स्थल की पवित्रता व स्वच्छता बनाए रखें",
                    "मधुर एवं नम्र वाणी का प्रयोग करें"
                ),
                goodActionsEnglish = listOf(
                    "Meditation on Shri Lakshmi-Narayana and Mother Goddess",
                    "Maintain purity and cleanliness at home & prayer place",
                    "Use polite and soft-spoken language"
                ),
                avoidActionsHindi = listOf(
                    "अत्यधिक हठ अथवा ज़िद से बचें",
                    "भोजन तथा अन्न का आदर करें, बर्बादी न होने दें",
                    "दूसरों के प्रति मन में द्वेष न रखें"
                ),
                avoidActionsEnglish = listOf(
                    "Avoid extreme stubbornness",
                    "Respect food and prevent wastage",
                    "Do not hold malice toward others"
                ),
                devotionalPracticeHindi = "माता महालक्ष्मी की आरती अथवा 'ॐ नमो भगवते वासुदेवाय' का पाठ।",
                devotionalPracticeEnglish = "Shri Mahalakshmi Aarti or chanting 'Om Namo Bhagavate Vasudevaya'.",
                mantraDevanagari = "ॐ महालक्ष्म्यै नमः",
                mantraTransliteration = "Om Mahalakshmyai Namah",
                sevaSuggestionsHindi = listOf(
                    "मंदिर में सजावट या पुष्प सेवा",
                    "अन्न क्षेत्र (लंगर/रसोई) में सहयोग"
                ),
                sevaSuggestionsEnglish = listOf(
                    "Temple decoration or flower service",
                    "Assisting in community kitchen (Langar)"
                )
            )

            Rashi.MITHUNA -> RashiGuidance(
                rashi = Rashi.MITHUNA,
                rulingPlanet = "बुध (Mercury)",
                preferredColorsHindi = listOf("हरा (Green)", "हल्का पीला (Light Yellow)", "सफेद (White)"),
                preferredColorsEnglish = listOf("Green", "Light Yellow", "White"),
                avoidColorsHindi = listOf("गहरा लाल या अत्यधिक भड़कीला रंग"),
                avoidColorsEnglish = listOf("Deep dark red or overly flashy colors"),
                donationSuggestionsHindi = listOf(
                    "विद्यार्थियों को शिक्षा सामग्री (पुस्तकें, पेन) दान",
                    "गौ माता को हरी घास अथवा पालक देना",
                    "पर्यावरण संरक्षण हेतु वृक्षारोपण व पौधे लगाना"
                ),
                donationSuggestionsEnglish = listOf(
                    "Donating books/stationery to students in need",
                    "Offering green grass or spinach to cows",
                    "Tree plantation and environmental care"
                ),
                goodActionsHindi = listOf(
                    "श्री गणेश जी एवं विष्णु भगवान का स्मरण",
                    "सत्य, मधुर और विचारशील वाणी बोलें",
                    "ज्ञानवर्धक व धार्मिक साहित्य का अध्ययन करें"
                ),
                goodActionsEnglish = listOf(
                    "Remembrance of Lord Ganesha and Lord Vishnu",
                    "Speak truthful, sweet, and thoughtful words",
                    "Study spiritual literature"
                ),
                avoidActionsHindi = listOf(
                    "झूठी बातें, अफवाहें अथवा निंदा से बचें",
                    "एक साथ कई अधूरे कार्य न छोड़ें",
                    "समय का दुरुपयोग न करें"
                ),
                avoidActionsEnglish = listOf(
                    "Avoid false gossip or backbiting",
                    "Avoid leaving tasks incomplete",
                    "Do not waste time unnecessarily"
                ),
                devotionalPracticeHindi = "श्री गणेश अथर्वशीर्ष पाठ अथवा 'ॐ गं गणपतये नमः' का जाप।",
                devotionalPracticeEnglish = "Recitation of Shri Ganesha Atharvashirsha or chanting 'Om Gam Ganapataye Namah'.",
                mantraDevanagari = "ॐ गं गणपतये नमः",
                mantraTransliteration = "Om Gam Ganapataye Namah",
                sevaSuggestionsHindi = listOf(
                    "धार्मिक पुस्तकें वितरित करना",
                    "पौधों व पक्षियों की नियमित देखभाल सेवा"
                ),
                sevaSuggestionsEnglish = listOf(
                    "Distributing spiritual literature",
                    "Regular plant watering and bird care"
                )
            )

            Rashi.KARKA -> RashiGuidance(
                rashi = Rashi.KARKA,
                rulingPlanet = "चंद्र (Moon)",
                preferredColorsHindi = listOf("सफेद (White)", "सिल्वर/चांदी (Silver)", "हल्का नीला (Light Blue)"),
                preferredColorsEnglish = listOf("White", "Silver", "Light Blue"),
                avoidColorsHindi = listOf("गहरा काला (Dark Black)"),
                avoidColorsEnglish = listOf("Dark Black"),
                donationSuggestionsHindi = listOf(
                    "शीतल जल वितरण (प्याऊ सेवा)",
                    "दूध, चावल या सफेद मिष्ठान दान",
                    "वृद्ध या असहाय जनों को वस्त्र/भोजन दान"
                ),
                donationSuggestionsEnglish = listOf(
                    "Cool drinking water distribution (Pyau Seva)",
                    "Milk, rice, or white sweets donation",
                    "Offering clothing/food to elders and needy"
                ),
                goodActionsHindi = listOf(
                    "भगवान शिव जी का जलाभिषेक व स्मरण",
                    "माता एवं मातृसम महिलाओं का आदर-सम्मान",
                    "मन को शांत व ध्यानमग्न रखें"
                ),
                goodActionsEnglish = listOf(
                    "Offering water (Jalabhisheka) to Lord Shiva",
                    "Respect mothers and elder women",
                    "Keep mind peaceful and meditative"
                ),
                avoidActionsHindi = listOf(
                    "अत्यधिक भावुकता या चिंता से बचें",
                    "किसी के प्रति मन में कटु भावना न लाएं",
                    "जल का अपव्यय न होने दें"
                ),
                avoidActionsEnglish = listOf(
                    "Avoid over-anxiety or extreme moodiness",
                    "Do not hold negative feelings",
                    "Do not waste water"
                ),
                devotionalPracticeHindi = "भगवान शिव जी का पंचाक्षर मंत्र 'ॐ नमः शिवाय' अथवा शिव चालीसा पाठ।",
                devotionalPracticeEnglish = "Chanting Lord Shiva's Panchakshari Mantra 'Om Namah Shivaya' or Shiva Chalisa.",
                mantraDevanagari = "ॐ नमः शिवाय",
                mantraTransliteration = "Om Namah Shivaya",
                sevaSuggestionsHindi = listOf(
                    "तीर्थों पर जल सेवा",
                    "अनाथ व असहाय बच्चों को दूध/भोजन वितरण"
                ),
                sevaSuggestionsEnglish = listOf(
                    "Water service at pilgrimage sites",
                    "Distributing milk/food to orphans"
                )
            )

            Rashi.SIMHA -> RashiGuidance(
                rashi = Rashi.SIMHA,
                rulingPlanet = "सूर्य (Sun)",
                preferredColorsHindi = listOf("केसरिया (Saffron)", "गोल्डन/सुनहरा (Golden)", "लाल (Red)"),
                preferredColorsEnglish = listOf("Saffron", "Golden", "Red"),
                avoidColorsHindi = listOf("गहरा सुर्मा या काला (Dark charcoal or black)"),
                avoidColorsEnglish = listOf("Dark charcoal or black"),
                donationSuggestionsHindi = listOf(
                    "गेहूं, गुड़, तांबे का पात्र या अन्न दान",
                    "धार्मिक स्थल के लिए प्रकाश/बिजली व्यवस्था में सहयोग",
                    "जरूरतमंदों को भोजन"
                ),
                donationSuggestionsEnglish = listOf(
                    "Wheat, jaggery, copper vessel, or grain donation",
                    "Assisting temple lighting and arrangements",
                    "Meal donation to needy"
                ),
                goodActionsHindi = listOf(
                    "प्रातःकाल सूर्य देव को अर्घ्य एवं प्रणाम",
                    "पिता एवं गुरुजनों का आदर करें",
                    "सत्य का पालन व दूसरों का नेतृत्व सद्भाव से करें"
                ),
                goodActionsEnglish = listOf(
                    "Offering water to Lord Surya in early morning",
                    "Respect father, teachers, and mentors",
                    "Follow truth and lead with goodwill"
                ),
                avoidActionsHindi = listOf(
                    "अहंकार, घमंड व अति-आत्मविश्वास से बचें",
                    "किसी छोटे या कमजोर व्यक्ति का अपमान न करें",
                    "आलस्य का त्याग करें"
                ),
                avoidActionsEnglish = listOf(
                    "Avoid ego, pride, and overconfidence",
                    "Do not disrespect or look down on anyone",
                    "Discard laziness"
                ),
                devotionalPracticeHindi = "सूर्य नमस्कार, आदित्य हृदय स्तोत्र पाठ या 'ॐ सूर्याय नमः' का ध्यान।",
                devotionalPracticeEnglish = "Surya Namaskar, Aditya Hridaya Stotra or chanting 'Om Suryaya Namah'.",
                mantraDevanagari = "ॐ सूर्याय नमः",
                mantraTransliteration = "Om Suryaya Namah",
                sevaSuggestionsHindi = listOf(
                    "मंदिर में दीप प्रज्वलन व व्यवस्था सेवा",
                    "गरीब विद्यार्थियों को वस्त्र व छात्रवृत्ति"
                ),
                sevaSuggestionsEnglish = listOf(
                    "Lighting lamps and event management at temple",
                    "Providing clothing and support to needy students"
                )
            )

            Rashi.KANYA -> RashiGuidance(
                rashi = Rashi.KANYA,
                rulingPlanet = "बुध (Mercury)",
                preferredColorsHindi = listOf("हल्का हरा (Light Green)", "सफेद (White)", "क्रीम (Cream)"),
                preferredColorsEnglish = listOf("Light Green", "White", "Cream"),
                avoidColorsHindi = listOf("अत्यधिक लाल (Extreme bright red)"),
                avoidColorsEnglish = listOf("Extreme bright red"),
                donationSuggestionsHindi = listOf(
                    "हरा चारा या पालक गौ माता को देना",
                    "अनाथालयों व विद्यालयों में वस्त्र/पुस्तक दान",
                    "औषधि (दवाइयां) या स्वास्थ्य सेवा में योगदान"
                ),
                donationSuggestionsEnglish = listOf(
                    "Offering green fodder to cows",
                    "Donating books/clothes to orphanages & schools",
                    "Contributing medicines/health service"
                ),
                goodActionsHindi = listOf(
                    "श्री गणेश जी एवं भगवती दुर्गा जी का स्मरण",
                    "सफाई, अनुशासन व स्वच्छता का पालन",
                    "सत्यवादी एवं विनम्र व्यवहार"
                ),
                goodActionsEnglish = listOf(
                    "Remembrance of Shri Ganesha and Goddess Durga",
                    "Maintain cleanliness, order, and discipline",
                    "Practice truthfulness and humility"
                ),
                avoidActionsHindi = listOf(
                    "दूसरों की अत्यधिक कमियां निकालने से बचें",
                    "चिंता या तनाव को मन पर हावी न होने दें",
                    "वाणी में कठोरता न लाएं"
                ),
                avoidActionsEnglish = listOf(
                    "Avoid overly criticizing others",
                    "Do not let stress overpower your mind",
                    "Avoid harsh speech"
                ),
                devotionalPracticeHindi = "श्री गणेश आरती अथवा 'ॐ गं गणपतये नमः' का मानसिक जाप।",
                devotionalPracticeEnglish = "Shri Ganesha Aarti or silent mental chanting of 'Om Gam Ganapataye Namah'.",
                mantraDevanagari = "ॐ गं गणपतये नमः",
                mantraTransliteration = "Om Gam Ganapataye Namah",
                sevaSuggestionsHindi = listOf(
                    "धार्मिक स्थल परिसर में स्वच्छता सेवा",
                    "वृक्षारोपण व पर्यावरण संवर्धन"
                ),
                sevaSuggestionsEnglish = listOf(
                    "Cleanliness service at holy places",
                    "Tree planting and nature care"
                )
            )

            Rashi.TULA -> RashiGuidance(
                rashi = Rashi.TULA,
                rulingPlanet = "शुक्र (Venus)",
                preferredColorsHindi = listOf("सफेद (White)", "सिल्वर (Silver)", "हल्का नीला (Light Blue)"),
                preferredColorsEnglish = listOf("White", "Silver", "Light Blue"),
                avoidColorsHindi = listOf("गहरा काला या बदरंग रंग"),
                avoidColorsEnglish = listOf("Dark black or dull muddy colors"),
                donationSuggestionsHindi = listOf(
                    "अन्न, शक्कर, दूध या घी का दान",
                    "कन्याओं को मिष्ठान व पठन सामग्री देना",
                    "कपड़े तथा वस्त्र दान"
                ),
                donationSuggestionsEnglish = listOf(
                    "Donating grains, sugar, milk, or ghee",
                    "Gifting sweets & learning materials to young girls",
                    "Clothing distribution"
                ),
                goodActionsHindi = listOf(
                    "श्री लक्ष्मी जी व सर्वव्यापी नारायण जी का ध्यान",
                    "न्याय, समता और निष्पक्षता का पालन",
                    "मधुर संगीत व भक्ति भजन का श्रवण"
                ),
                goodActionsEnglish = listOf(
                    "Meditation on Shri Lakshmi and Narayana",
                    "Uphold justice, equality, and fairness",
                    "Listen to devotional music and bhajans"
                ),
                avoidActionsHindi = listOf(
                    "अधर्म अथवा अन्याय का पक्ष न लें",
                    "अत्यधिक आलस्य या निर्णय में विलंब से बचें",
                    "किसी का अधिकार न छीनें"
                ),
                avoidActionsEnglish = listOf(
                    "Do not support injustice or unrighteousness",
                    "Avoid indecisiveness or extreme procrastination",
                    "Do not infringe on anyone's rights"
                ),
                devotionalPracticeHindi = "श्री सूक्त पाठ अथवा 'ॐ महालक्ष्म्यै नमः' का जाप।",
                devotionalPracticeEnglish = "Recitation of Shri Suktam or chanting 'Om Mahalakshmyai Namah'.",
                mantraDevanagari = "ॐ श्रीं महालक्ष्म्यै नमः",
                mantraTransliteration = "Om Shreem Mahalakshmyai Namah",
                sevaSuggestionsHindi = listOf(
                    "मंदिर प्रांगण में प्रकाश व व्यवस्था",
                    "सांस्कृतिक व धार्मिक आयोजनों में सहयोग"
                ),
                sevaSuggestionsEnglish = listOf(
                    "Illumination and decor in temple premises",
                    "Assisting in cultural and devotional events"
                )
            )

            Rashi.VRISHCHIKA -> RashiGuidance(
                rashi = Rashi.VRISHCHIKA,
                rulingPlanet = "मंगल (Mars)",
                preferredColorsHindi = listOf("लाल (Red)", "मैलून/गहरा लाल (Maroon)", "केसरिया (Saffron)"),
                preferredColorsEnglish = listOf("Red", "Maroon", "Saffron"),
                avoidColorsHindi = listOf("अत्यधिक गहरा काला (Very dark black)"),
                avoidColorsEnglish = listOf("Very dark black"),
                donationSuggestionsHindi = listOf(
                    "लाल वस्त्र, मसूर दाल या गुड़ का दान",
                    "हनुमान मंदिर में फल या मिष्ठान चढ़ाना",
                    "रक्तदान (यदि स्वास्थ्य अनुमति दे) या स्वास्थ्य सेवा"
                ),
                donationSuggestionsEnglish = listOf(
                    "Red cloth, lentils (masoor), or jaggery donation",
                    "Offering fruits/sweets at Hanuman temple",
                    "Blood donation (if healthy) or medical support"
                ),
                goodActionsHindi = listOf(
                    "श्री हनुमान जी एवं भगवान शिव जी की भक्ति",
                    "सत्य, निष्ठा और संकल्प शक्ति का पालन",
                    "दूसरों की गुप्त रूप से सहायता"
                ),
                goodActionsEnglish = listOf(
                    "Devotion to Lord Hanuman and Lord Shiva",
                    "Uphold truth, integrity, and strong resolve",
                    "Help others quietly without show"
                ),
                avoidActionsHindi = listOf(
                    "क्रोध, बदले की भावना या ईर्ष्या से बचें",
                    "रहस्यमयी या गलत गतिविधियों में न पड़ें",
                    "किसी को धोखा न दें"
                ),
                avoidActionsEnglish = listOf(
                    "Avoid anger, revengeful attitude, or jealousy",
                    "Avoid misleading or unethical activities",
                    "Do not deceive anyone"
                ),
                devotionalPracticeHindi = "हनुमान चालीसा, बजरंग बाण या 'ॐ नमः शिवाय' जाप।",
                devotionalPracticeEnglish = "Recitation of Hanuman Chalisa, Bajrang Baan, or 'Om Namah Shivaya'.",
                mantraDevanagari = "ॐ हनुमते नमः",
                mantraTransliteration = "Om Hanumate Namah",
                sevaSuggestionsHindi = listOf(
                    "मंदिर में सुरक्षा व व्यवस्था सेवा",
                    "निःसहाय रोगियों की चिकित्सा में मदद"
                ),
                sevaSuggestionsEnglish = listOf(
                    "Temple security and crowd management assistance",
                    "Helping helpless patients with medical care"
                )
            )

            Rashi.DHANU -> RashiGuidance(
                rashi = Rashi.DHANU,
                rulingPlanet = "गुरु (Jupiter)",
                preferredColorsHindi = listOf("पीला (Yellow)", "केसरिया (Saffron)", "गोल्डन (Golden)"),
                preferredColorsEnglish = listOf("Yellow", "Saffron", "Golden"),
                avoidColorsHindi = listOf("गहरा काला (Dark Black)"),
                avoidColorsEnglish = listOf("Dark Black"),
                donationSuggestionsHindi = listOf(
                    "चने की दाल, हल्दी, पीले वस्त्र या धार्मिक पुस्तकें दान",
                    "शिक्षकों, विद्वानों व साधुओं को सम्मान व भोजन",
                    "गौ सेवा व मंदिर लंगर में सहयोग"
                ),
                donationSuggestionsEnglish = listOf(
                    "Chana dal, turmeric, yellow cloth, or spiritual books donation",
                    "Respecting and feeding teachers, scholars, and monks",
                    "Gau Seva and supporting temple langar"
                ),
                goodActionsHindi = listOf(
                    "श्री हरि विष्णु जी व गुरुदेव का ध्यान",
                    "धर्म, न्याय और स्वाध्याय का आचरण",
                    "दूसरों को सद्ज्ञान व मार्गदर्शन देना"
                ),
                goodActionsEnglish = listOf(
                    "Meditation on Shri Hari Vishnu and Gurudev",
                    "Practice righteousness, justice, and self-study",
                    "Provide noble guidance and wisdom to others"
                ),
                avoidActionsHindi = listOf(
                    "गुरुजनों या धार्मिक परंपरा का उपहास न करें",
                    "अधर्म या झूठ का सहारा न लें",
                    "अहंकारपूर्ण उपदेश न दें"
                ),
                avoidActionsEnglish = listOf(
                    "Do not mock teachers or holy traditions",
                    "Do not resort to falsehood or unrighteousness",
                    "Do not give arrogant advice"
                ),
                devotionalPracticeHindi = "विष्णु सहस्रनाम पाठ अथवा 'ॐ नमो भगवते वासुदेवाय' का जप।",
                devotionalPracticeEnglish = "Recitation of Vishnu Sahasranama or chanting 'Om Namo Bhagavate Vasudevaya'.",
                mantraDevanagari = "ॐ नमो भगवते वासुदेवाय",
                mantraTransliteration = "Om Namo Bhagavate Vasudevaya",
                sevaSuggestionsHindi = listOf(
                    "धार्मिक पठन सामग्री व शास्त्रों का वितरण",
                    "गुरुकुल या धार्मिक शिक्षा संस्थानों में सहयोग"
                ),
                sevaSuggestionsEnglish = listOf(
                    "Distribution of scriptures and holy literature",
                    "Assisting gurukuls or religious educational centers"
                )
            )

            Rashi.MAKARA -> RashiGuidance(
                rashi = Rashi.MAKARA,
                rulingPlanet = "शनि (Saturn)",
                preferredColorsHindi = listOf("नीला (Blue)", "गहरा नीला (Navy Blue)", "ग्रे (Grey)"),
                preferredColorsEnglish = listOf("Blue", "Navy Blue", "Grey"),
                avoidColorsHindi = listOf("अत्यधिक चटक लाल (Overly bright red)"),
                avoidColorsEnglish = listOf("Overly bright red"),
                donationSuggestionsHindi = listOf(
                    "गरीब व श्रमजीवियों (मजदूरों) को अन्न व वस्त्र दान",
                    "काले तिल, उड़द या सरसों के तेल का दान",
                    "वृद्धाश्रम में आवश्यक सामग्री भेंट करना"
                ),
                donationSuggestionsEnglish = listOf(
                    "Food & clothing distribution to poor laborers",
                    "Black sesame, urad dal, or mustard oil donation",
                    "Gifting essentials to old age homes"
                ),
                goodActionsHindi = listOf(
                    "भगवान शिव व शनिदेव का स्मरण",
                    "कर्मनिष्ठा, परिश्रम, ईमानदारी व धैर्य",
                    "निराश्रितों व श्रमिकों का सम्मान"
                ),
                goodActionsEnglish = listOf(
                    "Remembrance of Lord Shiva and Lord Shani",
                    "Focus on hard work, honesty, and patience",
                    "Respect laborers and helpless people"
                ),
                avoidActionsHindi = listOf(
                    "किसी गरीब या अधीनस्थ कर्मचारी का शोषण न करें",
                    "आलस्य, काम टालना या छल से बचें",
                    "दूसरों का अधिकार न मारें"
                ),
                avoidActionsEnglish = listOf(
                    "Do not exploit the poor or subordinates",
                    "Avoid laziness, procrastination, or deceit",
                    "Do not deprive others of their rightful due"
                ),
                devotionalPracticeHindi = "शनिवार को पीपल वृक्ष तले दीप प्रज्वलन व 'ॐ शं शनैश्चराय नमः' अथवा 'ॐ नमः शिवाय' जाप।",
                devotionalPracticeEnglish = "Lighting a lamp under Peepal tree and chanting 'Om Sham Shanaishcharaya Namah' or 'Om Namah Shivaya'.",
                mantraDevanagari = "ॐ शं शनैश्चराय नमः",
                mantraTransliteration = "Om Sham Shanaishcharaya Namah",
                sevaSuggestionsHindi = listOf(
                    "वृद्धों व दिव्यांगों की सेवा",
                    "मंदिर निर्माण व मरम्मत कार्य में श्रमदान"
                ),
                sevaSuggestionsEnglish = listOf(
                    "Service to elderly and persons with disabilities",
                    "Volunteering labor for temple maintenance"
                )
            )

            Rashi.KUMBHA -> RashiGuidance(
                rashi = Rashi.KUMBHA,
                rulingPlanet = "शनि (Saturn)",
                preferredColorsHindi = listOf("आसमानी नीला (Sky Blue)", "नीला (Blue)", "बैंगनी/विओलेट (Violet)"),
                preferredColorsEnglish = listOf("Sky Blue", "Blue", "Violet"),
                avoidColorsHindi = listOf("अत्यधिक गहरा लाल (Deep bright red)"),
                avoidColorsEnglish = listOf("Deep bright red"),
                donationSuggestionsHindi = listOf(
                    "जरूरतमंदों को जूते-चप्पल, कंबल या वस्त्र दान",
                    "जल सेवा एवं प्याऊ की व्यवस्था",
                    "अन्न क्षेत्र में गुप्त दान"
                ),
                donationSuggestionsEnglish = listOf(
                    "Footwear, blankets, or clothes donation to the needy",
                    "Water distribution service",
                    "Anonymous food donation"
                ),
                goodActionsHindi = listOf(
                    "भगवान शिव एवं श्री हनुमान जी की सेवा",
                    "लोक कल्याण व परोपकारी कार्यों में भाग लें",
                    "सबके प्रति समभाव व बंधुत्व रखें"
                ),
                goodActionsEnglish = listOf(
                    "Service to Lord Shiva and Lord Hanuman",
                    "Participate in community welfare and philanthropy",
                    "Maintain brotherhood and equality toward all"
                ),
                avoidActionsHindi = listOf(
                    "किसी का अपमान न करें",
                    "घमंड या समाज से कटकर रहने से बचें",
                    "अनैतिक साधनों से धन कमाने से बचें"
                ),
                avoidActionsEnglish = listOf(
                    "Do not insult or belittle anyone",
                    "Avoid arrogance or isolation",
                    "Avoid earning through unethical means"
                ),
                devotionalPracticeHindi = "शिव चालीसा पाठ अथवा 'ॐ नमः शिवाय' का नियमपूर्वक ध्यान।",
                devotionalPracticeEnglish = "Recitation of Shiva Chalisa or regular meditation on 'Om Namah Shivaya'.",
                mantraDevanagari = "ॐ नमः शिवाय",
                mantraTransliteration = "Om Namah Shivaya",
                sevaSuggestionsHindi = listOf(
                    "अस्पतालों व कुष्ठ आश्रमों में सेवा",
                    "पर्यावरण व स्वच्छता अभियानों में योगदान"
                ),
                sevaSuggestionsEnglish = listOf(
                    "Service at health centers or rehabilitation homes",
                    "Contributing to clean & green environment drives"
                )
            )

            Rashi.MEENA -> RashiGuidance(
                rashi = Rashi.MEENA,
                rulingPlanet = "गुरु (Jupiter)",
                preferredColorsHindi = listOf("पीला (Yellow)", "केसरिया (Saffron)", "सफेद (White)"),
                preferredColorsEnglish = listOf("Yellow", "Saffron", "White"),
                avoidColorsHindi = listOf("गहरा काला (Dark Black)"),
                avoidColorsEnglish = listOf("Dark Black"),
                donationSuggestionsHindi = listOf(
                    "धार्मिक पुस्तकों, चने की दाल या फल दान",
                    "जलीय जीवों (मछलियों) को आटा/दाना देना",
                    "अनाथालयों व गोशालाओं में आर्थिक/वस्तु दान"
                ),
                donationSuggestionsEnglish = listOf(
                    "Spiritual books, chana dal, or fruit donation",
                    "Feeding fish and water animals",
                    "Donations to orphanages and gaushalas"
                ),
                goodActionsHindi = listOf(
                    "श्री हरि विष्णु एवं लक्ष्मी जी का स्मरण",
                    "करुणा, क्षमाशीलता एवं परोपकार का भाव",
                    "संतों, साधुओं व माता-पिता का आशीर्वाद"
                ),
                goodActionsEnglish = listOf(
                    "Remembrance of Shri Hari Vishnu and Lakshmi Ji",
                    "Practice compassion, forgiveness, and benevolence",
                    "Seek blessings from saints, monks, and parents"
                ),
                avoidActionsHindi = listOf(
                    "अत्यधिक काल्पनिक या भ्रमित विचारों से बचें",
                    "नशे या हानिकारक पदार्थों से दूर रहें",
                    "किसी के विश्वास को न तोड़ें"
                ),
                avoidActionsEnglish = listOf(
                    "Avoid delusional or impractical thinking",
                    "Stay away from intoxicants or harmful habits",
                    "Do not breach anyone's trust"
                ),
                devotionalPracticeHindi = "विष्णु सहस्रनाम या 'ॐ नमो भगवते वासुदेवाय' अथवा 'ॐ बृं बृहस्पतये नमः' का जप।",
                devotionalPracticeEnglish = "Chanting Vishnu Sahasranama or 'Om Namo Bhagavate Vasudevaya' or 'Om Brim Brihaspataye Namah'.",
                mantraDevanagari = "ॐ बृं बृहस्पतये नमः",
                mantraTransliteration = "Om Brim Brihaspataye Namah",
                sevaSuggestionsHindi = listOf(
                    "तीर्थ यात्रियों की सेवा व सहायता",
                    "मंदिर में पूजा-पाठ व्यवस्था में योगदान"
                ),
                sevaSuggestionsEnglish = listOf(
                    "Assisting pilgrims and yatris",
                    "Supporting temple prayer & ritual arrangements"
                )
            )
        }
    }
}
