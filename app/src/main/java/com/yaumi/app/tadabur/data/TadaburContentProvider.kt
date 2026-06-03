package com.yaumi.app.tadabur.data

import android.content.Context
import com.yaumi.app.quran.data.QuranRepository
import com.yaumi.app.tadabur.domain.TadaburDay

class TadaburContentProvider(private val appContext: Context) {
    private val repository = QuranRepository(appContext)
    private val themes = listOf(
        Theme(
            hikmah = "Ayat ini menegaskan bahwa hanya Allah tempat bergantung. Ketika hati kembali kepada-Nya, kegelisahan berkurang dan arah hidup jadi jelas.",
            praktik = listOf("Dzikir singkat setelah Subuh.", "Tulis satu niat ibadah utama hari ini."),
            amal = listOf("Dzikir pagi minimal 3 menit", "Menjaga niat sebelum memulai aktivitas")
        ),
        Theme(
            hikmah = "Ayat ini mengingatkan bahwa setiap nikmat datang dari Allah. Syukur menjaga hati tetap lapang dan menjauhkan keluh kesah.",
            praktik = listOf("Catat 3 nikmat yang terasa hari ini.", "Ucapkan hamdalah di setiap selesai aktivitas."),
            amal = listOf("Mengucap syukur minimal 5 kali", "Tidak mengeluh berlebihan")
        ),
        Theme(
            hikmah = "Ayat ini mengajarkan sabar dalam proses. Ujian tidak selalu berat jika disikapi dengan tenang dan yakin.",
            praktik = listOf("Tahan reaksi saat emosi muncul.", "Ambil jeda 1 menit sebelum merespons."),
            amal = listOf("Menunda amarah", "Memaafkan satu kesalahan orang lain")
        ),
        Theme(
            hikmah = "Ayat ini membuka pintu taubat. Allah menerima hamba yang kembali dengan tulus dan memperbaiki diri.",
            praktik = listOf("Istighfar 100 kali hari ini.", "Perbaiki satu kebiasaan kecil."),
            amal = listOf("Istighfar minimal 100 kali", "Menghindari satu maksiat yang biasa")
        ),
        Theme(
            hikmah = "Ayat ini menegaskan amanah dan akhlak. Kejujuran adalah tanda iman yang hidup.",
            praktik = listOf("Jaga janji kecil yang sudah dibuat.", "Sampaikan kebenaran dengan lembut."),
            amal = listOf("Tepat waktu pada satu janji", "Tidak menunda hak orang")
        ),
        Theme(
            hikmah = "Ayat ini menguatkan tawakal. Ikhtiar dilakukan, hasilnya diserahkan kepada Allah.",
            praktik = listOf("Kerjakan tugas utama tanpa menunda.", "Doa singkat setelah usaha."),
            amal = listOf("Selesaikan satu tugas penting", "Berdoa setelah berikhtiar")
        ),
        Theme(
            hikmah = "Ayat ini mengingatkan bahwa Al-Qur'an adalah petunjuk hidup. Membacanya menambah terang hati.",
            praktik = listOf("Baca ayat hari ini dengan tartil.", "Renungkan satu kata kunci dari ayat."),
            amal = listOf("Tilawah minimal 5 ayat", "Menulis satu pelajaran dari ayat")
        ),
        Theme(
            hikmah = "Ayat ini mengarahkan agar shalat menjaga jiwa. Shalat tepat waktu menata ritme hidup.",
            praktik = listOf("Jaga shalat di awal waktu.", "Perbanyak khusyuk di satu rakaat."),
            amal = listOf("Shalat wajib tepat waktu", "Menjaga fokus saat shalat")
        ),
        Theme(
            hikmah = "Ayat ini mengingatkan menjaga lisan. Ucapan yang baik adalah sedekah.",
            praktik = listOf("Ucapkan kata baik pada 1 orang.", "Hindari ghibah hari ini."),
            amal = listOf("Berkata baik", "Menahan komentar negatif")
        ),
        Theme(
            hikmah = "Ayat ini menekankan berbagi. Sedekah membersihkan hati dan menolong sesama.",
            praktik = listOf("Sisihkan sedekah hari ini.", "Bantu satu orang dengan tenaga."),
            amal = listOf("Sedekah meski sedikit", "Memberi bantuan nyata")
        )
    )

    suspend fun getDay(dayIndex: Int): TadaburDay {
        val surahId = ((dayIndex - 1) % 114) + 1
        val surah = repository.getSurahSummaries().first { it.id == surahId }
        val ayahLines = repository.getAyahLines(surahId)
        val ayahCount = minOf(5, ayahLines.size)
        val selected = ayahLines.take(ayahCount)
        val theme = themes[(dayIndex - 1) % themes.size]

        return TadaburDay(
            dayIndex = dayIndex,
            surahName = surah.latinName,
            ayahRange = "1-$ayahCount",
            arabicText = selected.joinToString(" e4 ") { it.arabicText },
            translationId = selected.joinToString(" ") { it.translationId },
            hikmah = theme.hikmah,
            praktik = theme.praktik,
            amalTracker = theme.amal,
            hadithArabic = "",
            hadithTranslation = "",
            hadithReference = ""
        )
    }

    private data class Theme(
        val hikmah: String,
        val praktik: List<String>,
        val amal: List<String>
    )
}
