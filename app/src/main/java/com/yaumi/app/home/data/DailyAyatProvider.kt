package com.yaumi.app.home.data

import com.yaumi.app.home.components.AyatCardData
import java.time.LocalDate

/**
 * Curated rotation of inspirational ayat. Picked deterministically by
 * day-of-year so the same date always shows the same verse, but it changes
 * every day.
 */
object DailyAyatProvider {
    private val verses = listOf(
        AyatCardData(
            surahLabel = "Q.S. Ar-Ra'd · Ayat 28",
            arabic = "ٱلَّذِينَ ءَامَنُوا۟ وَتَطْمَئِنُّ قُلُوبُهُم بِذِكْرِ ٱللَّهِ ۗ أَلَا بِذِكْرِ ٱللَّهِ تَطْمَئِنُّ ٱلْقُلُوبُ",
            translation = "(yaitu) orang-orang yang beriman dan hati mereka menjadi tenteram dengan mengingat Allah. Ingatlah, hanya dengan mengingat Allah hati menjadi tenteram."
        ),
        AyatCardData(
            surahLabel = "Q.S. Al-Baqarah · Ayat 286",
            arabic = "لَا يُكَلِّفُ ٱللَّهُ نَفْسًا إِلَّا وُسْعَهَا",
            translation = "Allah tidak membebani seseorang melainkan sesuai dengan kesanggupannya."
        ),
        AyatCardData(
            surahLabel = "Q.S. Al-Insyirah · Ayat 5-6",
            arabic = "فَإِنَّ مَعَ ٱلْعُسْرِ يُسْرًا ۝ إِنَّ مَعَ ٱلْعُسْرِ يُسْرًا",
            translation = "Maka sesungguhnya bersama kesulitan ada kemudahan. Sesungguhnya bersama kesulitan ada kemudahan."
        ),
        AyatCardData(
            surahLabel = "Q.S. At-Talaq · Ayat 2-3",
            arabic = "وَمَن يَتَّقِ ٱللَّهَ يَجْعَل لَّهُۥ مَخْرَجًا ۝ وَيَرْزُقْهُ مِنْ حَيْثُ لَا يَحْتَسِبُ",
            translation = "Dan barang siapa bertakwa kepada Allah, niscaya Dia akan membukakan jalan keluar baginya, dan memberinya rezeki dari arah yang tidak disangka-sangka."
        ),
        AyatCardData(
            surahLabel = "Q.S. Ali 'Imran · Ayat 159",
            arabic = "فَإِذَا عَزَمْتَ فَتَوَكَّلْ عَلَى ٱللَّهِ ۚ إِنَّ ٱللَّهَ يُحِبُّ ٱلْمُتَوَكِّلِينَ",
            translation = "Kemudian apabila engkau telah membulatkan tekad, maka bertawakallah kepada Allah. Sungguh, Allah mencintai orang yang bertawakal."
        ),
        AyatCardData(
            surahLabel = "Q.S. Al-Baqarah · Ayat 152",
            arabic = "فَٱذْكُرُونِىٓ أَذْكُرْكُمْ وَٱشْكُرُوا۟ لِى وَلَا تَكْفُرُونِ",
            translation = "Maka ingatlah kepada-Ku, niscaya Aku ingat (pula) kepadamu, dan bersyukurlah kepada-Ku, dan janganlah kamu ingkar kepada-Ku."
        ),
        AyatCardData(
            surahLabel = "Q.S. Al-Baqarah · Ayat 153",
            arabic = "يَٰٓأَيُّهَا ٱلَّذِينَ ءَامَنُوا۟ ٱسْتَعِينُوا۟ بِٱلصَّبْرِ وَٱلصَّلَوٰةِ ۚ إِنَّ ٱللَّهَ مَعَ ٱلصَّٰبِرِينَ",
            translation = "Wahai orang-orang yang beriman! Mohonlah pertolongan (kepada Allah) dengan sabar dan salat. Sungguh, Allah beserta orang-orang yang sabar."
        ),
        AyatCardData(
            surahLabel = "Q.S. Al-Hadid · Ayat 4",
            arabic = "وَهُوَ مَعَكُمْ أَيْنَ مَا كُنتُمْ ۚ وَٱللَّهُ بِمَا تَعْمَلُونَ بَصِيرٌ",
            translation = "Dan Dia bersama kamu di mana saja kamu berada. Dan Allah Maha Melihat apa yang kamu kerjakan."
        ),
        AyatCardData(
            surahLabel = "Q.S. Al-Baqarah · Ayat 255",
            arabic = "ٱللَّهُ لَآ إِلَٰهَ إِلَّا هُوَ ٱلْحَىُّ ٱلْقَيُّومُ",
            translation = "Allah, tidak ada Tuhan selain Dia. Yang Mahahidup, Yang terus-menerus mengurus (makhluk-Nya)."
        ),
        AyatCardData(
            surahLabel = "Q.S. Al-Anfal · Ayat 46",
            arabic = "وَٱصْبِرُوٓا۟ ۚ إِنَّ ٱللَّهَ مَعَ ٱلصَّٰبِرِينَ",
            translation = "Dan bersabarlah. Sungguh, Allah beserta orang-orang yang sabar."
        ),
        AyatCardData(
            surahLabel = "Q.S. Al-Baqarah · Ayat 45",
            arabic = "وَٱسْتَعِينُوا۟ بِٱلصَّبْرِ وَٱلصَّلَوٰةِ",
            translation = "Dan mohonlah pertolongan (kepada Allah) dengan sabar dan salat."
        ),
        AyatCardData(
            surahLabel = "Q.S. Az-Zumar · Ayat 53",
            arabic = "لَا تَقْنَطُوا۟ مِن رَّحْمَةِ ٱللَّهِ ۚ إِنَّ ٱللَّهَ يَغْفِرُ ٱلذُّنُوبَ جَمِيعًا",
            translation = "Janganlah kamu berputus asa dari rahmat Allah. Sesungguhnya Allah mengampuni dosa-dosa semuanya."
        ),
        AyatCardData(
            surahLabel = "Q.S. Al-Mu'min · Ayat 60",
            arabic = "ٱدْعُونِىٓ أَسْتَجِبْ لَكُمْ",
            translation = "Berdoalah kepada-Ku, niscaya akan Aku perkenankan bagimu."
        ),
        AyatCardData(
            surahLabel = "Q.S. Al-Ankabut · Ayat 69",
            arabic = "وَٱلَّذِينَ جَٰهَدُوا۟ فِينَا لَنَهْدِيَنَّهُمْ سُبُلَنَا",
            translation = "Dan orang-orang yang berjihad untuk (mencari keridaan) Kami, benar-benar akan Kami tunjukkan kepada mereka jalan-jalan Kami."
        ),
        AyatCardData(
            surahLabel = "Q.S. Ibrahim · Ayat 7",
            arabic = "لَئِن شَكَرْتُمْ لَأَزِيدَنَّكُمْ",
            translation = "Sesungguhnya jika kamu bersyukur, niscaya Aku akan menambah (nikmat) kepadamu."
        ),
        AyatCardData(
            surahLabel = "Q.S. Yusuf · Ayat 87",
            arabic = "وَلَا تَا۟يْـَٔسُوا۟ مِن رَّوْحِ ٱللَّهِ",
            translation = "Dan janganlah kamu berputus asa dari rahmat Allah."
        ),
        AyatCardData(
            surahLabel = "Q.S. At-Tawbah · Ayat 51",
            arabic = "قُل لَّن يُصِيبَنَآ إِلَّا مَا كَتَبَ ٱللَّهُ لَنَا هُوَ مَوْلَىٰنَا ۚ وَعَلَى ٱللَّهِ فَلْيَتَوَكَّلِ ٱلْمُؤْمِنُونَ",
            translation = "Katakanlah, 'Tidak akan menimpa kami melainkan apa yang telah ditetapkan Allah bagi kami. Dialah Pelindung kami, dan hanya kepada Allah orang-orang yang beriman bertawakal.'"
        ),
        AyatCardData(
            surahLabel = "Q.S. Al-Baqarah · Ayat 216",
            arabic = "وَعَسَىٰٓ أَن تَكْرَهُوا۟ شَيْـًٔا وَهُوَ خَيْرٌ لَّكُمْ",
            translation = "Boleh jadi kamu membenci sesuatu, padahal itu baik bagimu."
        ),
        AyatCardData(
            surahLabel = "Q.S. Al-Hujurat · Ayat 13",
            arabic = "إِنَّ أَكْرَمَكُمْ عِندَ ٱللَّهِ أَتْقَىٰكُمْ",
            translation = "Sungguh, yang paling mulia di antara kamu di sisi Allah ialah orang yang paling bertakwa."
        ),
        AyatCardData(
            surahLabel = "Q.S. Al-Mulk · Ayat 2",
            arabic = "ٱلَّذِى خَلَقَ ٱلْمَوْتَ وَٱلْحَيَوٰةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا",
            translation = "Yang menciptakan mati dan hidup, untuk menguji kamu, siapa di antara kamu yang lebih baik amalnya."
        ),
        AyatCardData(
            surahLabel = "Q.S. Al-Mujadalah · Ayat 11",
            arabic = "يَرْفَعِ ٱللَّهُ ٱلَّذِينَ ءَامَنُوا۟ مِنكُمْ وَٱلَّذِينَ أُوتُوا۟ ٱلْعِلْمَ دَرَجَٰتٍ",
            translation = "Allah akan mengangkat (derajat) orang-orang yang beriman di antaramu dan orang-orang yang diberi ilmu beberapa derajat."
        ),
        AyatCardData(
            surahLabel = "Q.S. Al-Furqan · Ayat 70",
            arabic = "إِلَّا مَن تَابَ وَءَامَنَ وَعَمِلَ عَمَلًا صَٰلِحًا فَأُو۟لَٰٓئِكَ يُبَدِّلُ ٱللَّهُ سَيِّـَٔاتِهِمْ حَسَنَٰتٍ",
            translation = "Kecuali orang-orang yang bertobat dan beriman serta mengerjakan kebajikan; maka kejahatan mereka diganti Allah dengan kebaikan."
        ),
        AyatCardData(
            surahLabel = "Q.S. Al-Qasas · Ayat 77",
            arabic = "وَأَحْسِن كَمَآ أَحْسَنَ ٱللَّهُ إِلَيْكَ",
            translation = "Dan berbuat baiklah (kepada orang lain) sebagaimana Allah telah berbuat baik kepadamu."
        ),
        AyatCardData(
            surahLabel = "Q.S. Asy-Syura · Ayat 30",
            arabic = "وَمَآ أَصَٰبَكُم مِّن مُّصِيبَةٍ فَبِمَا كَسَبَتْ أَيْدِيكُمْ وَيَعْفُوا۟ عَن كَثِيرٍ",
            translation = "Dan musibah apa pun yang menimpa kamu adalah karena perbuatan tanganmu sendiri, dan Allah memaafkan banyak (dari kesalahan-kesalahanmu)."
        ),
        AyatCardData(
            surahLabel = "Q.S. Al-A'raf · Ayat 56",
            arabic = "إِنَّ رَحْمَتَ ٱللَّهِ قَرِيبٌ مِّنَ ٱلْمُحْسِنِينَ",
            translation = "Sesungguhnya rahmat Allah sangat dekat kepada orang-orang yang berbuat kebaikan."
        ),
        AyatCardData(
            surahLabel = "Q.S. Al-Baqarah · Ayat 186",
            arabic = "وَإِذَا سَأَلَكَ عِبَادِى عَنِّى فَإِنِّى قَرِيبٌ",
            translation = "Dan apabila hamba-hamba-Ku bertanya kepadamu (Muhammad) tentang Aku, maka sesungguhnya Aku dekat."
        ),
        AyatCardData(
            surahLabel = "Q.S. Asy-Syarh · Ayat 7-8",
            arabic = "فَإِذَا فَرَغْتَ فَٱنصَبْ ۝ وَإِلَىٰ رَبِّكَ فَٱرْغَب",
            translation = "Maka apabila engkau telah selesai (dari sesuatu urusan), tetaplah bekerja keras (untuk urusan yang lain). Dan hanya kepada Tuhanmulah engkau berharap."
        ),
        AyatCardData(
            surahLabel = "Q.S. Yunus · Ayat 62",
            arabic = "أَلَآ إِنَّ أَوْلِيَآءَ ٱللَّهِ لَا خَوْفٌ عَلَيْهِمْ وَلَا هُمْ يَحْزَنُونَ",
            translation = "Ingatlah wali-wali Allah itu, tidak ada rasa takut pada mereka dan mereka tidak bersedih hati."
        ),
        AyatCardData(
            surahLabel = "Q.S. Al-Imran · Ayat 200",
            arabic = "يَٰٓأَيُّهَا ٱلَّذِينَ ءَامَنُوا۟ ٱصْبِرُوا۟ وَصَابِرُوا۟ وَرَابِطُوا۟ وَٱتَّقُوا۟ ٱللَّهَ لَعَلَّكُمْ تُفْلِحُونَ",
            translation = "Wahai orang-orang yang beriman! Bersabarlah kamu dan kuatkanlah kesabaranmu dan tetaplah bersiap-siaga (di perbatasan negerimu) dan bertakwalah kepada Allah agar kamu beruntung."
        ),
        AyatCardData(
            surahLabel = "Q.S. Al-Fath · Ayat 4",
            arabic = "هُوَ ٱلَّذِىٓ أَنزَلَ ٱلسَّكِينَةَ فِى قُلُوبِ ٱلْمُؤْمِنِينَ",
            translation = "Dialah yang telah menurunkan ketenangan ke dalam hati orang-orang mukmin."
        )
    )

    fun forDate(date: LocalDate = LocalDate.now()): AyatCardData {
        val index = (date.toEpochDay().rem(verses.size).toInt() + verses.size) % verses.size
        return verses[index]
    }
}
