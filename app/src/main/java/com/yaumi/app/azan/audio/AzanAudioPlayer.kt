package com.yaumi.app.azan.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import java.io.File

object AzanAudioPlayer {
    @Volatile
    private var currentPlayer: MediaPlayer? = null

    @Volatile
    private var volumeLevel: Float = 1f

    fun playAssetAudio(
        context: Context,
        fileName: String,
        onCompletion: (() -> Unit)? = null
    ) {
        runCatching {
            val target = ensureCachedFile(context, fileName)

            stopCurrent()

            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setDataSource(target.absolutePath)
                prepare()
                setVolume(volumeLevel, volumeLevel)
                setOnCompletionListener {
                    it.release()
                    if (currentPlayer === it) {
                        currentPlayer = null
                    }
                    onCompletion?.invoke()
                }
                setOnErrorListener { mp, _, _ ->
                    mp.release()
                    if (currentPlayer === mp) {
                        currentPlayer = null
                    }
                    onCompletion?.invoke()
                    true
                }
            }
            currentPlayer = player
            player.start()
        }
    }

    fun assetDurationSeconds(context: Context, fileName: String): Int {
        val fallback = 320
        return runCatching {
            val target = ensureCachedFile(context, fileName)
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(target.absolutePath)
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            retriever.release()
            val seconds = (durationMs / 1000L).toInt()
            if (seconds < 30) fallback else seconds
        }.getOrDefault(fallback)
    }

    fun setVolumePercent(percent: Int) {
        val normalized = (percent.coerceIn(0, 100) / 100f)
        volumeLevel = normalized
        currentPlayer?.setVolume(normalized, normalized)
    }

    fun pauseCurrent() {
        val player = currentPlayer ?: return
        if (player.isPlaying) {
            player.pause()
        }
    }

    fun resumeCurrent() {
        val player = currentPlayer ?: return
        if (!player.isPlaying) {
            runCatching { player.start() }
        }
    }

    fun stopCurrent() {
        val player = currentPlayer ?: return
        runCatching {
            if (player.isPlaying) {
                player.stop()
            }
        }
        runCatching { player.release() }
        if (currentPlayer === player) {
            currentPlayer = null
        }
    }

    fun isPlaying(): Boolean = currentPlayer?.isPlaying == true

    private fun ensureCachedFile(context: Context, fileName: String): File {
        val target = File(context.cacheDir, fileName)
        if (!target.exists() || target.length() == 0L) {
            context.assets.open("audio/$fileName").use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
        }
        return target
    }
}
