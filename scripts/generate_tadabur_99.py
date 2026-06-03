import json
from pathlib import Path

QURAN_PATH = Path("D:/yaumi/yaumi-android/app/src/main/assets/quran_index.json")
OUT_PATH = Path("D:/yaumi/yaumi-android/app/src/main/assets/data/tadabur/tadabur_99.json")


REFS = [
    {"surah": 1, "ayah": 5, "theme": "doa"},
    {"surah": 2, "ayah": 152, "theme": "syukur"},
    {"surah": 2, "ayah": 153, "theme": "sabar"},
    {"surah": 2, "ayah": 186, "theme": "doa"},
    {"surah": 2, "ayah": 286, "theme": "kasih"},
    {"surah": 2, "ayah": 45, "theme": "sabar"},
    {"surah": 2, "ayah": 201, "theme": "doa"},
    {"surah": 3, "ayah": 139, "theme": "semangat"},
    {"surah": 3, "ayah": 159, "theme": "kasih"},
    {"surah": 3, "ayah": 173, "theme": "tawakal"},
    {"surah": 3, "ayah": 200, "theme": "taqwa"},
    {"surah": 4, "ayah": 28, "theme": "kasih"},
    {"surah": 4, "ayah": 110, "theme": "taubat"},
    {"surah": 4, "ayah": 147, "theme": "syukur"},
    {"surah": 5, "ayah": 8, "theme": "taqwa"},
    {"surah": 5, "ayah": 13, "theme": "kasih"},
    {"surah": 5, "ayah": 48, "theme": "taqwa"},
    {"surah": 6, "ayah": 54, "theme": "kasih"},
    {"surah": 7, "ayah": 23, "theme": "taubat"},
    {"surah": 7, "ayah": 56, "theme": "doa"},
    {"surah": 8, "ayah": 2, "theme": "dzikir"},
    {"surah": 8, "ayah": 46, "theme": "sabar"},
    {"surah": 9, "ayah": 51, "theme": "tawakal"},
    {"surah": 10, "ayah": 57, "theme": "tenang"},
    {"surah": 10, "ayah": 58, "theme": "syukur"},
    {"surah": 11, "ayah": 6, "theme": "tawakal"},
    {"surah": 11, "ayah": 90, "theme": "taubat"},
    {"surah": 12, "ayah": 86, "theme": "doa"},
    {"surah": 12, "ayah": 87, "theme": "tawakal"},
    {"surah": 13, "ayah": 28, "theme": "dzikir"},
    {"surah": 14, "ayah": 7, "theme": "syukur"},
    {"surah": 14, "ayah": 41, "theme": "doa"},
    {"surah": 15, "ayah": 49, "theme": "kasih"},
    {"surah": 15, "ayah": 50, "theme": "taqwa"},
    {"surah": 16, "ayah": 18, "theme": "syukur"},
    {"surah": 16, "ayah": 97, "theme": "semangat"},
    {"surah": 17, "ayah": 23, "theme": "kasih"},
    {"surah": 17, "ayah": 82, "theme": "tenang"},
    {"surah": 17, "ayah": 70, "theme": "kasih"},
    {"surah": 18, "ayah": 10, "theme": "doa"},
    {"surah": 18, "ayah": 46, "theme": "muhasabah"},
    {"surah": 18, "ayah": 110, "theme": "ikhlas"},
    {"surah": 19, "ayah": 96, "theme": "kasih"},
    {"surah": 20, "ayah": 46, "theme": "tenang"},
    {"surah": 20, "ayah": 114, "theme": "ilmu"},
    {"surah": 21, "ayah": 83, "theme": "sabar"},
    {"surah": 21, "ayah": 87, "theme": "doa"},
    {"surah": 21, "ayah": 107, "theme": "kasih"},
    {"surah": 22, "ayah": 77, "theme": "taqwa"},
    {"surah": 22, "ayah": 78, "theme": "semangat"},
    {"surah": 23, "ayah": 60, "theme": "muhasabah"},
    {"surah": 23, "ayah": 109, "theme": "doa"},
    {"surah": 24, "ayah": 22, "theme": "kasih"},
    {"surah": 24, "ayah": 35, "theme": "tenang"},
    {"surah": 25, "ayah": 70, "theme": "taubat"},
    {"surah": 25, "ayah": 74, "theme": "doa"},
    {"surah": 26, "ayah": 80, "theme": "tenang"},
    {"surah": 26, "ayah": 88, "theme": "muhasabah"},
    {"surah": 27, "ayah": 19, "theme": "syukur"},
    {"surah": 28, "ayah": 24, "theme": "doa"},
    {"surah": 28, "ayah": 77, "theme": "ikhlas"},
    {"surah": 29, "ayah": 45, "theme": "dzikir"},
    {"surah": 29, "ayah": 69, "theme": "semangat"},
    {"surah": 30, "ayah": 21, "theme": "tenang"},
    {"surah": 30, "ayah": 60, "theme": "sabar"},
    {"surah": 31, "ayah": 17, "theme": "taqwa"},
    {"surah": 31, "ayah": 19, "theme": "taqwa"},
    {"surah": 33, "ayah": 41, "theme": "dzikir"},
    {"surah": 33, "ayah": 70, "theme": "taqwa"},
    {"surah": 33, "ayah": 71, "theme": "taubat"},
    {"surah": 34, "ayah": 39, "theme": "tawakal"},
    {"surah": 35, "ayah": 28, "theme": "ilmu"},
    {"surah": 35, "ayah": 30, "theme": "semangat"},
    {"surah": 36, "ayah": 58, "theme": "tenang"},
    {"surah": 36, "ayah": 82, "theme": "semangat"},
    {"surah": 39, "ayah": 10, "theme": "sabar"},
    {"surah": 39, "ayah": 53, "theme": "taubat"},
    {"surah": 39, "ayah": 23, "theme": "tenang"},
    {"surah": 40, "ayah": 60, "theme": "doa"},
    {"surah": 41, "ayah": 30, "theme": "tenang"},
    {"surah": 42, "ayah": 23, "theme": "kasih"},
    {"surah": 42, "ayah": 36, "theme": "tawakal"},
    {"surah": 43, "ayah": 67, "theme": "muhasabah"},
    {"surah": 46, "ayah": 15, "theme": "syukur"},
    {"surah": 47, "ayah": 7, "theme": "semangat"},
    {"surah": 48, "ayah": 4, "theme": "tenang"},
    {"surah": 49, "ayah": 13, "theme": "taqwa"},
    {"surah": 50, "ayah": 16, "theme": "tenang"},
    {"surah": 51, "ayah": 56, "theme": "ikhlas"},
    {"surah": 53, "ayah": 39, "theme": "semangat"},
    {"surah": 55, "ayah": 13, "theme": "syukur"},
    {"surah": 55, "ayah": 60, "theme": "kasih"},
    {"surah": 57, "ayah": 4, "theme": "tenang"},
    {"surah": 57, "ayah": 22, "theme": "muhasabah"},
    {"surah": 58, "ayah": 11, "theme": "ilmu"},
    {"surah": 59, "ayah": 18, "theme": "muhasabah"},
    {"surah": 64, "ayah": 11, "theme": "tawakal"},
    {"surah": 65, "ayah": 3, "theme": "tawakal"},
    {"surah": 67, "ayah": 2, "theme": "muhasabah"}
]


THEMES = {
    "doa": {
        "hikmah": [
            "Ayat ini mengajari kita membuka hati apa adanya kepada Allah. Doa bukan sekadar kata, tetapi jembatan harap yang menenangkan ketika dada penuh beban.",
            "Ayat ini memanggil kita untuk datang dengan harap dan kejujuran. Saat doa dipanjatkan, kita diingatkan bahwa pertolongan Allah selalu lebih dekat daripada sangka.",
            "Ayat ini menenangkan jiwa yang berbisik dalam sunyi. Ketika doa kita jujur, Allah menuntun langkah yang paling tepat untuk hari ini.",
        ],
        "praktik": ["Luangkan 2 menit untuk berdoa dengan tenang.", "Tuliskan satu permohonan yang paling kamu butuhkan."],
        "amal": ["Berdoa setelah shalat wajib", "Menjaga harapan baik kepada Allah"],
    },
    "sabar": {
        "hikmah": [
            "Ayat ini menuntun kita bersabar tanpa kehilangan harap. Sabar bukan menyerah, melainkan tetap teguh sambil percaya bahwa Allah melihat setiap perjuangan.",
            "Ayat ini menguatkan hati yang sedang diuji. Saat sabar dipilih, luka perlahan pulih dan jalan keluar sering datang dari arah yang tak disangka.",
            "Ayat ini mengajak kita tahan sejenak saat gelisah. Kesabaran melapangkan dada dan membuat hati tetap lembut di tengah ujian.",
        ],
        "praktik": ["Tarik napas sebelum bereaksi.", "Ucapkan istirja ketika diuji."],
        "amal": ["Menahan emosi saat tersinggung", "Menyelesaikan tugas meski berat"],
    },
    "syukur": {
        "hikmah": [
            "Ayat ini menegaskan bahwa syukur adalah napas hati yang sehat. Ketika nikmat disadari, bahagia menjadi sederhana dan keluh kesah perlahan sirna.",
            "Ayat ini mengajak kita menghitung nikmat, bukan kekurangan. Syukur membuat hati lapang dan menumbuhkan semangat untuk terus berbuat baik.",
            "Ayat ini mengingatkan bahwa nikmat yang kecil pun berarti. Saat kita bersyukur, Allah menambah tenang dan memperluas rezeki hati.",
        ],
        "praktik": ["Catat 3 nikmat yang terasa hari ini.", "Ucapkan hamdalah di akhir aktivitas."],
        "amal": ["Mengucap syukur minimal 5 kali", "Tidak mengeluh berlebihan"],
    },
    "tawakal": {
        "hikmah": [
            "Ayat ini mengajak kita berikhtiar lalu berserah dengan tenang. Hati yang bertawakal tidak panik oleh hasil, karena yakin Allah mengatur yang terbaik.",
            "Ayat ini menenangkan jiwa yang gelisah. Setelah usaha dilakukan, tawakal membuat kita ringan, sebab Allah tidak pernah menyia-nyiakan hamba-Nya.",
            "Ayat ini mengajarkan bahwa hasil ada di tangan Allah. Tugas kita adalah berusaha, lalu menerima keputusan-Nya dengan lapang dada.",
        ],
        "praktik": ["Kerjakan yang mampu, sisanya serahkan kepada Allah.", "Baca hasbunallah saat cemas."],
        "amal": ["Berani mengambil langkah kecil", "Berdoa setelah ikhtiar"],
    },
    "taubat": {
        "hikmah": [
            "Ayat ini membuka pintu kembali yang selalu menunggu. Taubat bukan tentang masa lalu, tetapi keberanian memulai lembar baru dengan hati yang bersih.",
            "Ayat ini memeluk hati yang ingin pulang. Selama kita kembali dengan tulus, Allah mengganti penyesalan dengan harapan yang hangat.",
            "Ayat ini mengingatkan bahwa Allah mencintai hamba yang kembali. Taubat membuat jiwa lebih ringan dan langkah terasa lebih bersih.",
        ],
        "praktik": ["Istighfar perlahan dengan hati hadir.", "Tinggalkan satu kebiasaan yang mengotori hati."],
        "amal": ["Istighfar minimal 100 kali", "Menjauhi satu maksiat"],
    },
    "dzikir": {
        "hikmah": [
            "Ayat ini mengingatkan bahwa hati menemukan tenang saat mengingat Allah. Dzikir adalah obat resah yang sederhana namun dalam, mengembalikan fokus kepada-Nya.",
            "Ayat ini mengajak kita menautkan hidup kepada Allah lewat dzikir. Saat nama-Nya disebut, gelisah mereda dan hati terasa lebih hidup.",
            "Ayat ini menegaskan bahwa kedekatan lahir dari ingatan kepada Allah. Dzikir membuat hati tetap terjaga meski hari sibuk.",
        ],
        "praktik": ["Sebut nama Allah di sela aktivitas.", "Baca tasbih saat pikiran mulai gelisah."],
        "amal": ["Dzikir pagi minimal 3 menit", "Menjaga lisan dari keluh kesah"],
    },
    "tenang": {
        "hikmah": [
            "Ayat ini menurunkan ketenangan ke dalam dada. Saat Allah menenteramkan, kekhawatiran mengecil dan kita mampu memandang hidup dengan lebih jernih.",
            "Ayat ini mengajarkan bahwa damai datang dari dekatnya Allah. Ketika hati bersandar kepada-Nya, badai terasa lebih mudah dilewati.",
            "Ayat ini membuat hati percaya bahwa ada tempat pulang. Bersama Allah, rasa cemas tidak lagi menguasai seluruh pikiran.",
        ],
        "praktik": ["Hening 1 menit, tarik napas, ingat Allah.", "Kurangi distraksi sebelum tidur."],
        "amal": ["Menjaga ketenangan saat sibuk", "Menghindari reaksi tergesa-gesa"],
    },
    "semangat": {
        "hikmah": [
            "Ayat ini menyalakan kembali semangat yang meredup. Allah mendorong kita berdiri, melangkah, dan percaya bahwa setiap usaha yang lurus tidak sia-sia.",
            "Ayat ini memberi energi untuk bangkit dan terus berjalan. Selama niat baik dijaga, langkah kecil hari ini bisa menjadi kemenangan besar esok.",
            "Ayat ini mengajak kita berani memulai. Harapan dan usaha yang jujur akan ditolong dengan cara yang indah.",
        ],
        "praktik": ["Tegakkan niat dan mulai tugas utama hari ini.", "Ucapkan bismillah sebelum memulai."],
        "amal": ["Menyelesaikan satu target", "Tidak menunda kebaikan"],
    },
    "ikhlas": {
        "hikmah": [
            "Ayat ini mengajak kita menjaga niat agar tetap murni. Ikhlas membuat amal terasa ringan, karena tujuannya hanya Allah, bukan pujian manusia.",
            "Ayat ini menuntun hati agar tidak terikat pada penilaian orang. Saat ikhlas hadir, kebaikan menjadi lebih indah dan jiwa lebih bebas.",
            "Ayat ini mengajarkan bahwa keikhlasan adalah ketenangan. Saat niat lurus, kita tidak mudah goyah oleh ucapan orang.",
        ],
        "praktik": ["Periksa niat sebelum beramal.", "Lakukan kebaikan kecil tanpa diketahui orang."],
        "amal": ["Menjaga niat selama berbuat baik", "Tidak mencari pujian"],
    },
    "taqwa": {
        "hikmah": [
            "Ayat ini menuntun kita memilih jalan takwa dalam setiap keputusan. Taqwa menjaga hati tetap bersih dan langkah tetap lurus meski godaan datang.",
            "Ayat ini mengingatkan bahwa takwa adalah pelindung paling lembut. Dengan takwa, kita menjaga batas sekaligus meraih ridha Allah.",
            "Ayat ini menegaskan bahwa takwa membuat hati lebih peka. Saat takwa dijaga, hidup terasa lebih terarah dan damai.",
        ],
        "praktik": ["Ingat Allah sebelum mengambil keputusan.", "Pilih yang halal dan bersih meski sulit."],
        "amal": ["Menjaga amanah kecil", "Menolak yang meragukan"],
    },
    "kasih": {
        "hikmah": [
            "Ayat ini menghangatkan hati dengan rahmat dan kasih sayang Allah. Saat kita memberi maaf dan berlembut, hidup terasa lebih ringan dan penuh cahaya.",
            "Ayat ini menegaskan bahwa rahmat Allah mendahului murka. Mengasihi, memaafkan, dan merendah membuat hati kita ikut lapang.",
            "Ayat ini mengajak kita merangkul, bukan menghakimi. Kasih sayang membuka pintu kebaikan yang sering kita cari.",
        ],
        "praktik": ["Maafkan satu orang di hati.", "Ucapkan kata lembut pada keluarga."],
        "amal": ["Memaafkan kesalahan kecil", "Mengganti marah dengan doa"],
    },
    "muhasabah": {
        "hikmah": [
            "Ayat ini mengajak kita menoleh ke dalam, bukan hanya ke luar. Muhasabah menata hati agar tidak larut dalam dunia dan mengingatkan arah pulang yang sejati.",
            "Ayat ini menegaskan bahwa hidup adalah ujian singkat. Saat kita merenung, kita sadar mana yang perlu diperbaiki sebelum terlambat.",
            "Ayat ini mengingatkan bahwa hari ini adalah kesempatan. Muhasabah membuat langkah besok lebih bersih dan lebih berarti.",
        ],
        "praktik": ["Renungkan apa yang perlu diperbaiki hari ini.", "Tuliskan satu kebiasaan yang ingin ditata."],
        "amal": ["Mengevaluasi diri sebelum tidur", "Memperbaiki satu sikap"],
    },
    "ilmu": {
        "hikmah": [
            "Ayat ini mengajak kita merendah di hadapan ilmu Allah. Semakin belajar, semakin kita tahu betapa butuhnya bimbingan-Nya dalam setiap langkah.",
            "Ayat ini menumbuhkan rasa haus pada ilmu yang menuntun. Ilmu yang benar melembutkan hati dan membuat kita lebih dekat kepada Allah.",
            "Ayat ini mengingatkan bahwa ilmu adalah cahaya. Saat kita belajar dengan niat baik, hati menjadi lebih tenang dan arah lebih jelas.",
        ],
        "praktik": ["Baca satu halaman ilmu yang bermanfaat.", "Tanyakan satu hal yang belum kamu pahami."],
        "amal": ["Menjaga semangat belajar", "Mengamalkan satu ilmu kecil"],
    },
}


def load_quran():
    with QURAN_PATH.open(encoding="utf-8") as f:
        return json.load(f)


def build_entry(day: int, ref: dict, surah: dict, ayah: dict):
    theme = THEMES[ref["theme"]]
    hikmah = theme["hikmah"][day % len(theme["hikmah"])]
    return {
        "day": day,
        "surah": surah["name"],
        "ayah_range": str(ref["ayah"]),
        "arab": ayah["ar"],
        "terjemah": ayah["id"],
        "hikmah": hikmah,
        "praktik": theme["praktik"],
        "amal_tracker": theme["amal"],
        "hadis_ar": "",
        "hadis_id": "",
        "hadis_ref": "",
    }


def main():
    quran = load_quran()
    out = []
    for idx, ref in enumerate(REFS, start=1):
        surah = next((s for s in quran if s["id"] == ref["surah"]), None)
        if not surah:
            raise SystemExit(f"Surah {ref['surah']} not found")
        ayah = next((a for a in surah["ayat"] if a["ayah"] == ref["ayah"]), None)
        if not ayah:
            raise SystemExit(f"Ayah {ref['ayah']} not found in surah {ref['surah']}")
        out.append(build_entry(idx, ref, surah, ayah))

    OUT_PATH.write_text(json.dumps(out, ensure_ascii=False, indent=2), encoding="utf-8")


if __name__ == "__main__":
    main()
