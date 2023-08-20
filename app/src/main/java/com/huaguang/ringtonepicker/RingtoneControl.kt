package com.huaguang.ringtonepicker

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * 控制铃声播放、停止的工具类
 */
object RingtoneControl {

    private var mediaPlayer: MediaPlayer? = null
    private var selectedPosition = -1 // 当前选中的位置，默认为 -1
    private val _status: MutableLiveData<Status> = MutableLiveData()
    val status: LiveData<Status> = _status

    /**
     * 专门用于铃声列表项点击的播放控制
     */
    fun ringtonePlayControl(context: Context, uri: Uri, position: Int) {
        // 停止或暂停之前的铃声
        if (position != selectedPosition) {
            // 当前点击的位置和缓存中的不一样
            stopRingtone() // 停止之前的铃声
            selectedPosition = position // 更新位置
            initializePlayer(context, uri) // 创建并配置播放器
            mediaPlayer?.start() // 播放新音乐
        } else {
            // 一样，说明点击的是同一个 Item
            Log.i("铃声选择", "ringtonePlayControl: 点击的是同一个 Item！")
            playOrPause()
        }
    }

    /**
     * 通过 Uri，创建并配置 MediaPlayer，达到 prepare 的状态
     */
    fun initializePlayer(context: Context, uri: Uri) {
        // 为了让外界直接调用，必须给 mediaPlayer 赋值！仅仅创建和配置是没用的。
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, uri)
            setOnCompletionListener {
                Toast.makeText(context, "播放完成", Toast.LENGTH_SHORT).show()
                _status.value = Status.COMPLETE
            }
            prepare()
        }
    }

    /**
     * 专门用于从铃声列表返回时的设定
     */
    fun stopAndPrepare() {
        Log.i("铃声选择", "stopAndPrepare: 退出列表，返回对话框")
        mediaPlayer?.stop()
        mediaPlayer?.prepare()
        _status.value = Status.STOP
    }

    fun stopRingtone() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    /**
     * 调用后，如果播放就暂停，如果暂停就播放（从原暂停处继续）。
     * 顺便设置 status。
     */
    fun playOrPause() {
        _status.value = if (mediaPlayer?.isPlaying == false) {
            Log.i("铃声选择", "playOrPause: 播放")
            mediaPlayer?.start()
            Status.PLAYING
        } else {
            Log.i("铃声选择", "playOrPause: 暂停")
            mediaPlayer?.pause()
            Status.PAUSE
        }
    }

}
