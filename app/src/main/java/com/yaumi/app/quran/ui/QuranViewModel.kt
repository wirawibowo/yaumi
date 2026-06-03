package com.yaumi.app.quran.ui

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yaumi.app.quran.data.QuranAudioPreferenceStore
import com.yaumi.app.quran.data.QuranBookmarkStore
import com.yaumi.app.quran.data.QuranReciter
import com.yaumi.app.quran.data.QuranReciters
import com.yaumi.app.quran.data.QuranRepository
import com.yaumi.app.quran.domain.model.AyahLine
import com.yaumi.app.quran.domain.model.SurahSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuranUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val surahList: List<SurahSummary> = emptyList(),
    val selectedSurah: SurahSummary? = null,
    val ayahLines: List<AyahLine> = emptyList(),
    val bookmarkedAyahNumbers: Set<Int> = emptySet(),
    val isSurahAudioLoading: Boolean = false,
    val isSurahAudioPlaying: Boolean = false,
    val audioSurahId: Int? = null,
    val activeAyahNumber: Int? = null,
    val audioErrorMessage: String? = null,
    val reciterId: String = QuranReciters.DEFAULT_ID,
    val availableReciters: List<QuranReciter> = QuranReciters.ALL
)

class QuranViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = QuranRepository(application.applicationContext)
    private val bookmarkStore = QuranBookmarkStore(application.applicationContext)
    private val audioPrefs = QuranAudioPreferenceStore(application.applicationContext)
    private val _uiState = MutableStateFlow(
        QuranUiState(reciterId = audioPrefs.getReciterId())
    )
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()
    private var mediaPlayer: MediaPlayer? = null
    private var currentAyahIndex: Int = -1

    init {
        loadSurahList()
    }

    fun setReciter(id: String) {
        audioPrefs.setReciterId(id)
        _uiState.update { it.copy(reciterId = id) }
        stopAudioInternal(clearAudioSurah = true)
    }

    fun loadSurahList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                repository.getSurahSummaries()
            }.onSuccess { data ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        surahList = data,
                        selectedSurah = data.firstOrNull()
                    )
                }
                data.firstOrNull()?.let { first -> selectSurah(first.id) }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.message ?: "Gagal memuat data Quran."
                    )
                }
            }
        }
    }

    fun selectSurah(surahId: Int) {
        viewModelScope.launch {
            if (_uiState.value.audioSurahId != surahId) {
                stopAudioInternal(clearAudioSurah = true)
            }

            val selected = _uiState.value.surahList.firstOrNull { it.id == surahId }
            _uiState.update { it.copy(isLoading = true, selectedSurah = selected, errorMessage = null) }

            runCatching { repository.getAyahLines(surahId) }
                .onSuccess { ayat ->
                    val bookmarked = bookmarkStore.getBookmarkedAyahNumbers(surahId)
                    val tafsirMap = repository.getTafsirMap(surahId)
                    val merged = ayat.map { it.copy(tafsirText = tafsirMap[it.number]) }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            ayahLines = merged,
                            bookmarkedAyahNumbers = bookmarked,
                            activeAyahNumber = null
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err.message ?: "Gagal memuat ayat."
                        )
                    }
                }
        }
    }

    fun toggleSurahAudio() {
        val state = _uiState.value
        val surah = state.selectedSurah ?: return
        val ayahNumbers = state.ayahLines.map { it.number }
        if (ayahNumbers.isEmpty()) {
            _uiState.update {
                it.copy(audioErrorMessage = "Ayat belum tersedia untuk diputar.")
            }
            return
        }

        val sameSurahSession = state.audioSurahId == surah.id && mediaPlayer != null
        if (sameSurahSession) {
            val player = mediaPlayer ?: return
            if (player.isPlaying) {
                player.pause()
                _uiState.update {
                    it.copy(
                        isSurahAudioPlaying = false,
                        isSurahAudioLoading = false,
                        audioErrorMessage = null
                    )
                }
            } else {
                runCatching { player.start() }
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isSurahAudioPlaying = true,
                                isSurahAudioLoading = false,
                                audioErrorMessage = null
                            )
                        }
                    }
                    .onFailure {
                        playAyahAtIndex(0, surah.id, ayahNumbers)
                    }
            }
            return
        }

        stopAudioInternal(clearAudioSurah = false)
        playAyahAtIndex(0, surah.id, ayahNumbers)
    }

    fun toggleAyahAudio(ayahNumber: Int) {
        val state = _uiState.value
        val surah = state.selectedSurah ?: return
        val ayahNumbers = state.ayahLines.map { it.number }
        val targetIndex = ayahNumbers.indexOf(ayahNumber)
        if (targetIndex < 0) return

        val player = mediaPlayer
        val isSameAyah = state.activeAyahNumber == ayahNumber && state.audioSurahId == surah.id

        if (isSameAyah && player != null) {
            if (player.isPlaying) {
                player.pause()
                _uiState.update {
                    it.copy(
                        isSurahAudioPlaying = false,
                        isSurahAudioLoading = false,
                        audioErrorMessage = null
                    )
                }
            } else {
                runCatching { player.start() }
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isSurahAudioPlaying = true,
                                isSurahAudioLoading = false,
                                audioErrorMessage = null
                            )
                        }
                    }
                    .onFailure {
                        playAyahAtIndex(targetIndex, surah.id, ayahNumbers)
                    }
            }
            return
        }

        stopAudioInternal(clearAudioSurah = false)
        playAyahAtIndex(targetIndex, surah.id, ayahNumbers)
    }

    fun stopAudio() {
        stopAudioInternal(clearAudioSurah = false)
    }

    private fun playAyahAtIndex(index: Int, surahId: Int, ayahNumbers: List<Int>) {
        if (index !in ayahNumbers.indices) {
            _uiState.update {
                it.copy(
                    isSurahAudioLoading = false,
                    isSurahAudioPlaying = false,
                    activeAyahNumber = null
                )
            }
            currentAyahIndex = -1
            mediaPlayer?.runCatching { release() }
            mediaPlayer = null
            return
        }

        val ayahNumber = ayahNumbers[index]
        currentAyahIndex = index

        _uiState.update {
            it.copy(
                isSurahAudioLoading = true,
                isSurahAudioPlaying = false,
                audioSurahId = surahId,
                activeAyahNumber = ayahNumber,
                audioErrorMessage = null
            )
        }

        mediaPlayer?.runCatching {
            stop()
            release()
        }

        val player = MediaPlayer()
        mediaPlayer = player

        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )

        player.setOnPreparedListener {
            it.start()
            _uiState.update { state ->
                state.copy(
                    isSurahAudioLoading = false,
                    isSurahAudioPlaying = true,
                    audioSurahId = surahId,
                    activeAyahNumber = ayahNumber,
                    audioErrorMessage = null
                )
            }
        }
        player.setOnCompletionListener {
            playAyahAtIndex(index + 1, surahId, ayahNumbers)
        }
        player.setOnErrorListener { mp, _, _ ->
            mp.release()
            if (mediaPlayer === mp) {
                mediaPlayer = null
            }
            _uiState.update {
                it.copy(
                    isSurahAudioLoading = false,
                    isSurahAudioPlaying = false,
                    audioErrorMessage = "Gagal memutar ayat $ayahNumber, lanjut ayat berikutnya."
                )
            }
            playAyahAtIndex(index + 1, surahId, ayahNumbers)
            true
        }

        runCatching {
            player.setDataSource(buildAyahAudioUrl(surahId, ayahNumber))
            player.prepareAsync()
        }.onFailure { err ->
            player.release()
            if (mediaPlayer === player) {
                mediaPlayer = null
            }
            _uiState.update {
                it.copy(
                    isSurahAudioLoading = false,
                    isSurahAudioPlaying = false,
                    audioErrorMessage = err.message ?: "Gagal memulai audio ayat $ayahNumber."
                )
            }
            playAyahAtIndex(index + 1, surahId, ayahNumbers)
        }
    }

    fun toggleBookmark(ayahNumber: Int) {
        val surahId = _uiState.value.selectedSurah?.id ?: return
        val bookmarked = bookmarkStore.toggle(surahId, ayahNumber)
        _uiState.update {
            val mutable = it.bookmarkedAyahNumbers.toMutableSet()
            if (bookmarked) mutable.add(ayahNumber) else mutable.remove(ayahNumber)
            it.copy(bookmarkedAyahNumbers = mutable)
        }
    }

    private fun stopAudioInternal(clearAudioSurah: Boolean) {
        mediaPlayer?.runCatching {
            stop()
            release()
        }
        mediaPlayer = null
        currentAyahIndex = -1
        _uiState.update {
            it.copy(
                isSurahAudioLoading = false,
                isSurahAudioPlaying = false,
                audioSurahId = if (clearAudioSurah) null else it.audioSurahId,
                activeAyahNumber = null
            )
        }
    }

    private fun buildAyahAudioUrl(surahId: Int, ayahNumber: Int): String {
        val surahPadded = surahId.toString().padStart(3, '0')
        val ayahPadded = ayahNumber.toString().padStart(3, '0')
        val reciter = QuranReciters.byId(_uiState.value.reciterId)
        return "https://everyayah.com/data/${reciter.pathSegment}/${surahPadded}${ayahPadded}.mp3"
    }

    override fun onCleared() {
        stopAudioInternal(clearAudioSurah = true)
        super.onCleared()
    }
}
