package com.yaumi.app.azan.data

import com.yaumi.app.azan.domain.model.AzanUiData
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object AzanTimeUtils {
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")

    fun applyOffsets(data: AzanUiData, manualOffsets: Map<String, Int>): AzanUiData {
        val updated = data.timings.map { item ->
            val offset = manualOffsets[item.name] ?: 0
            if (offset == 0) return@map item

            val parsed = runCatching { LocalTime.parse(item.time, formatter) }.getOrNull() ?: return@map item
            val newTime = parsed.plusMinutes(offset.toLong())
            item.copy(time = newTime.format(formatter))
        }
        return data.copy(timings = updated)
    }
}
