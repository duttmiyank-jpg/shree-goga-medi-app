package com.example.miyank

data class HinduFestival(
    val day: Int,
    val month: Int, // 1-12
    val year: Int,
    val nameHindi: String,
    val nameEnglish: String,
    val description: String = ""
)

object HinduFestivalProvider {

    fun getFestivalsForMonth(year: Int, month: Int): List<HinduFestival> {
        val list = mutableListOf<HinduFestival>()

        when (month) {
            1 -> {
                list.add(HinduFestival(14, 1, year, "मकर संक्रांति / पोन्गल", "Makar Sankranti", "सूर्य देव का उत्तरायण प्रवेश एवं तिल-गुड़ दान"))
                list.add(HinduFestival(26, 1, year, "बसंत पंचमी (सरस्वती पूजा)", "Vasant Panchami", "माँ सरस्वती पूजन एवं वसंत ऋतु आगमन"))
            }
            2 -> {
                list.add(HinduFestival(15, 2, year, "महाशिवरात्रि", "Maha Shivratri", "भगवान भोलेनाथ की महापूजा एवं रात्रि जागरण"))
            }
            3 -> {
                list.add(HinduFestival(14, 3, year, "होली / होलिका दहन", "Holi / Holika Dahan", "पावन रंगोत्सव एवं प्रह्लाद भक्ति उत्सव"))
                list.add(HinduFestival(30, 3, year, "राम नवमी / चैत्र नवरात्रि", "Ram Navami", "मर्यादा पुरुषोत्तम भगवान श्री राम जन्मोत्सव"))
            }
            4 -> {
                list.add(HinduFestival(14, 4, year, "हनुमान जयंती", "Hanuman Jayanti", "श्री हनुमान जी का जन्मोत्सव एवं सुन्दरकाण्ड"))
                list.add(HinduFestival(22, 4, year, "अक्षय तृतीया", "Akshaya Tritiya", "अक्षय पुण्य प्राप्ति एवं सोने का दान-खरीदारी"))
            }
            5 -> {
                list.add(HinduFestival(12, 5, year, "गंगा दशहरा", "Ganga Dussehra", "पावन गंगा स्नान एवं दानपुण्य"))
                list.add(HinduFestival(23, 5, year, "बुद्ध पूर्णिमा", "Buddha Purnima", "वैशाख पूर्णिमा स्नानादि"))
            }
            6 -> {
                list.add(HinduFestival(21, 6, year, "निर्जला एकादशी", "Nirjala Ekadashi", "निर्जल उपवास एवं जल दान महत्ता"))
            }
            7 -> {
                list.add(HinduFestival(21, 7, year, "गुरु पूर्णिमा", "Guru Purnima", "सत् गुरु देव पूजन एवं व्यास पूजा"))
            }
            8 -> {
                list.add(HinduFestival(19, 8, year, "नाग पंचमी", "Nag Panchami", "नाग देवता पूजन एवं दूध-अभिषेक"))
                list.add(HinduFestival(28, 8, year, "रक्षाबंधन / पूर्णिमा", "Raksha Bandhan", "भाई-बहन के पवित्र प्रेम का पर्व"))
            }
            9 -> {
                list.add(HinduFestival(5, 9, year, "श्री कृष्ण जन्माष्टमी", "Janmashtami", "भगवान श्री कृष्ण जन्मोत्सव"))
                list.add(HinduFestival(18, 9, year, "गोगा नवमी (बागढ़ उत्सव)", "Goga Navami", "बाबा गोगा जी महाराज पावन जन्मोत्सव एवं मेला"))
                list.add(HinduFestival(25, 9, year, "गणेश चतुर्थी", "Ganesh Chaturthi", "भगवान श्री गणेश स्थापना एवं मोदक महाप्रसाद"))
            }
            10 -> {
                list.add(HinduFestival(11, 10, year, "शरद नवरात्रि आरंभ", "Navratri Begins", "माँ दुर्गा नवदुर्गा पूजन एवं गरबा उत्सव"))
                list.add(HinduFestival(20, 10, year, "विजयादशमी / दशहरा", "Dussehra", "अधर्म पर धर्म की विजय का पावन पर्व"))
                list.add(HinduFestival(28, 10, year, "करवा चौथ व्रत", "Karwa Chauth", "अखण्ड सौभाग्य एवं पति की दीर्घायु व्रत"))
            }
            11 -> {
                list.add(HinduFestival(8, 11, year, "दीपावली / महालक्ष्मी पूजा", "Diwali", "प्रकाश का महापर्व एवं माँ लक्ष्मी पूजन"))
                list.add(HinduFestival(10, 11, year, "गोवर्धन पूजा / अन्नकूट", "Govardhan Puja", "अन्नकूट महाप्रसाद एवं गोवर्धन पर्वत पूजन"))
                list.add(HinduFestival(12, 11, year, "भैया दूज", "Bhai Dooj", "यमुना-यमराज पूजन एवं भाई-बहन स्नेह"))
            }
            12 -> {
                list.add(HinduFestival(21, 12, year, "गीता जयंती / मोक्षदा एकादशी", "Gita Jayanti", "श्रीमद्भगवद्गीता पूजन एवं पाठ"))
            }
        }

        return list
    }

    fun getAllFestivalsForYear(year: Int): List<HinduFestival> {
        val all = mutableListOf<HinduFestival>()
        for (m in 1..12) {
            all.addAll(getFestivalsForMonth(year, m))
        }
        return all
    }

    fun getUpcomingFestivalsForYear(year: Int, fromMonth: Int, fromDay: Int = 1): List<HinduFestival> {
        return getAllFestivalsForYear(year).filter {
            (it.month > fromMonth) || (it.month == fromMonth && it.day >= fromDay)
        }
    }

    fun getFestivalForDate(year: Int, month: Int, day: Int): HinduFestival? {
        return getFestivalsForMonth(year, month).firstOrNull { it.day == day }
    }
}
