package com.yesvoters.android.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.*
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.*

class AudioRecorderService : Service() {

    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var isRecording = false
    private lateinit var audioFile: File

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val durationMinutes = intent?.getIntExtra("record_duration_minutes", 0) ?: 0
        val durationMillis = durationMinutes * 60 * 1000L

        startRecording(durationMillis)

        // Beep when (duration - 5 minutes) is reached
        if (durationMillis >= 5 * 60 * 1000L) {
            scheduleBeepAt(durationMillis - 5 * 60 * 1000L)
        }
        return START_STICKY
    }

    private fun scheduleBeepAt(delayMillis: Long) {
        Thread {
            try {
                Thread.sleep(delayMillis)
                if (isRecording) {
                    playBeep()
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun playBeep() {
        val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 1000) // 1-second beep
    }

    private fun startRecording(durationMillis: Long) {
    val sampleRate = 16000
    val channelConfig = AudioFormat.CHANNEL_IN_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        audioFile = File(cacheDir, "survey_audio_${System.currentTimeMillis()}.wav")

        getSharedPreferences("audio_prefs", MODE_PRIVATE)
            .edit()
            .putString("audio_path", audioFile.absolutePath)
            .apply()

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate, channelConfig, audioFormat, bufferSize
        )

        audioRecord?.startRecording()
        isRecording = true

        recordingThread = Thread {
            writePcmToWav(audioFile, sampleRate, channelConfig, audioFormat, bufferSize)
        }.also { it.start() }

        startForeground(1, createNotification())
         // 🔴 Stop after `durationMillis`
    if (durationMillis > 0) {
        Thread {
            try {
                Thread.sleep(durationMillis)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            stopSelf() // Stop the service after time limit
        }.start()
    }
}


    private fun writePcmToWav(
        file: File,
        sampleRate: Int,
        channelConfig: Int,
        audioFormat: Int,
        bufferSize: Int
    ) {
        val channels = if (channelConfig == AudioFormat.CHANNEL_IN_MONO) 1 else 2
        val bitsPerSample = if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) 16 else 8

        val pcmBuffer = ByteArray(bufferSize)
        val pcmOutput = ByteArrayOutputStream()

        while (isRecording) {
            val read = audioRecord?.read(pcmBuffer, 0, pcmBuffer.size) ?: 0
            if (read > 0) {
                pcmOutput.write(pcmBuffer, 0, read)
            }
        }

        val pcmData = pcmOutput.toByteArray()
        FileOutputStream(file).use { fos ->
            writeWavHeader(fos, pcmData.size, sampleRate, channels, bitsPerSample)
            fos.write(pcmData)
        }
    }

    private fun writeWavHeader(
        out: OutputStream,
        pcmLength: Int,
        sampleRate: Int,
        channels: Int,
        bitsPerSample: Int
    ) {
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val totalDataLen = pcmLength + 36

        out.write("RIFF".toByteArray())
        out.write(intToLittleEndian(totalDataLen))
        out.write("WAVE".toByteArray())
        out.write("fmt ".toByteArray())
        out.write(intToLittleEndian(16)) // Subchunk1Size
        out.write(shortToLittleEndian(1)) // Audio format (1 = PCM)
        out.write(shortToLittleEndian(channels.toShort()))
        out.write(intToLittleEndian(sampleRate))
        out.write(intToLittleEndian(byteRate))
        out.write(shortToLittleEndian((channels * bitsPerSample / 8).toShort())) // Block align
        out.write(shortToLittleEndian(bitsPerSample.toShort()))
        out.write("data".toByteArray())
        out.write(intToLittleEndian(pcmLength))
    }

    private fun intToLittleEndian(value: Int): ByteArray = byteArrayOf(
        (value and 0xff).toByte(),
        (value shr 8 and 0xff).toByte(),
        (value shr 16 and 0xff).toByte(),
        (value shr 24 and 0xff).toByte()
    )

    private fun shortToLittleEndian(value: Short): ByteArray = byteArrayOf(
        (value.toInt() and 0xff).toByte(),
        (value.toInt() shr 8 and 0xff).toByte()
    )

    private fun createNotification(): Notification {
        val channelId = "audio_record_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Audio Recording", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Recording Survey Audio")
            .setContentText("Recording in progress...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()
    }

    override fun onDestroy() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        recordingThread = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun getRecordedFilePath(context: Context): File? {
            val path = context.getSharedPreferences("audio_prefs", Context.MODE_PRIVATE)
                .getString("audio_path", null)
            val file = if (!path.isNullOrEmpty()) File(path) else null
            return if (file?.exists() == true) file else null
        }
    }
}
