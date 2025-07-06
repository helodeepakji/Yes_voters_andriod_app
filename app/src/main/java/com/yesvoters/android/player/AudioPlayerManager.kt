package com.yesvoters.android.player

import android.content.Context
import android.os.Handler
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class AudioPlayerManager(private val context: Context) {

    private var player: ExoPlayer? = null
    private var timeUpdateCallback: ((String, String) -> Unit)? = null
    private var handler: Handler? = null

    fun playAudio(uri: String, onTimeUpdate: (String, String) -> Unit) {
        release()
        timeUpdateCallback = onTimeUpdate

        player = ExoPlayer.Builder(context).build().apply {
            val audioUrl = "http://213.210.37.77/storage/${uri}"

            setMediaItem(MediaItem.fromUri(audioUrl))
            prepare()
            play()

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        val duration = formatTime(duration)
                        timeUpdateCallback?.invoke(formatTime(0), duration)
                        startTimeUpdates()
                    }
                }
            })
        }
    }

    private fun startTimeUpdates() {
        handler = Handler()
        handler?.post(object : Runnable {
            override fun run() {
                player?.let {
                    if (it.isPlaying) {
                        val current = formatTime(it.currentPosition)
                        val total = formatTime(it.duration)
                        timeUpdateCallback?.invoke(current, total)
                        handler?.postDelayed(this, 1000)
                    }
                }
            }
        })
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun stop() {
        player?.stop()
    }

    fun release() {
        handler?.removeCallbacksAndMessages(null)
        player?.release()
        player = null
    }
    fun pause() {
        player?.pause()
    }

    fun resume(onTimeUpdate: (String, String) -> Unit) {
        timeUpdateCallback = onTimeUpdate
        player?.play()
        startTimeUpdates()
    }

    fun isPlaying(): Boolean = player?.isPlaying == true
}
