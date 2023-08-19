package com.huaguang.ringtonepicker

import android.content.Context
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Handler
import android.os.Looper

/**
 * 控制铃声播放、停止的工具类
 */
object RingtoneControl {

    private var mediaPlayer: MediaPlayer? = null
    var ringtoneCompletionListener: RingtoneCompletionListener? = null

    fun playRingtone(context: Context, uri: Uri) {
        if (mediaPlayer == null) {
            // 创建并配置 MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                setOnCompletionListener {
                    // 铃声播放完成，通知监听器
                    ringtoneCompletionListener?.onRingtoneCompleted()
                }
                prepare()
            }
        }
        mediaPlayer?.start()
    }

    fun pauseRingtone() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    fun resumeRingtone() {
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    fun stopRingtone() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    interface RingtoneCompletionListener {
        fun onRingtoneCompleted()
    }
}
