package com.yaumi.app.azan.data

class IndonesiaLocationMapper {
    private val provinceAliases = mapOf(
        "daerah khusus ibukota jakarta" to "DKI Jakarta",
        "jakarta" to "DKI Jakarta",
        "dki jakarta" to "DKI Jakarta",
        "daerah istimewa yogyakarta" to "DI Yogyakarta",
        "yogyakarta" to "DI Yogyakarta",
        "jogja" to "DI Yogyakarta",
        "kep bangka belitung" to "Kepulauan Bangka Belitung",
        "kepulauan bangka belitung" to "Kepulauan Bangka Belitung",
        "ntb" to "Nusa Tenggara Barat",
        "nusa tenggara barat" to "Nusa Tenggara Barat",
        "ntt" to "Nusa Tenggara Timur",
        "nusa tenggara timur" to "Nusa Tenggara Timur",
        "papua barat daya" to "Papua Barat Daya"
    )

    fun matchProvince(geocoderProvince: String, provinces: List<String>): String? {
        val normalized = normalize(geocoderProvince)
        val alias = provinceAliases[normalized]
        if (alias != null && provinces.any { it == alias }) return alias

        val exact = provinces.firstOrNull { normalize(it) == normalized }
        if (exact != null) return exact

        return provinces.firstOrNull { candidate ->
            val normalizedCandidate = normalize(candidate)
            normalizedCandidate.contains(normalized) || normalized.contains(normalizedCandidate)
        }
    }

    fun matchKabkota(geocoderCity: String, kabkotaList: List<String>): String? {
        val normalized = normalize(geocoderCity)
        val stripped = stripKabkotaPrefix(normalized)
        return kabkotaList.firstOrNull { candidate ->
            val normalizedCandidate = normalize(candidate)
            normalizedCandidate == normalized ||
                stripKabkotaPrefix(normalizedCandidate) == stripped
        }
    }

    private fun stripKabkotaPrefix(value: String): String {
        return value.replace(Regex("^(kabupaten|kab|kota)\\s+"), "").trim()
    }

    private fun normalize(value: String): String {
        return value.lowercase()
            .replace(".", "")
            .replace("-", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
