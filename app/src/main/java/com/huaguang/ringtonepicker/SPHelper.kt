package com.huaguang.ringtonepicker

import android.content.Context
import android.util.Log

class SPHelper private constructor(context: Context) {

    private val sp = context.getSharedPreferences("sp", Context.MODE_PRIVATE)

    companion object {
        // SPHelper 类维护的一个自身类型的静态实例
        private var instance: SPHelper? = null

        // 获取自身单例的方法
        fun getInstance(context: Context): SPHelper {
            if (instance == null) {
                instance = SPHelper(context)
            }
            return instance as SPHelper
        }
    }

    fun saveRingtoneInfo(song: Song?) {
        val uriStr: String
        val title: String

        if (song == null) {
            Log.i("铃声选择", "saveRingtoneInfo: 为 null，存入")
            uriStr = "NULL"
            title = "无"
        } else {
            uriStr = song.songUri.toString()
            title = song.songTitle
        }

        sp.edit().apply {
            putString("ringtone_uri", uriStr)
            putString("ringtone_title", title)
        }.apply()
    }

    /**
     * 获取 Boolean 类型的标记值，默认为 false
     */
    fun getFlag(key: String): Boolean {
        return sp.getBoolean(key, false)
    }

    fun setFlag(key: String, value: Boolean) {
        sp.edit().putBoolean(key, value).apply()
    }

    fun getUri(): String {
        return sp.getString("ringtone_uri", "") ?: ""
    }

    fun getTitle(): String? { // 如果为空，就是没有存档标题
        return sp.getString("ringtone_title", "")
    }

    /**
     * 在应用的 ”安装-卸载“ 生命周期内只执行一次！
     */
    fun doOnce(action: () -> Unit) {
        if (!getFlag("executed")) {
            action()
            setFlag("executed", true)
        }
    }

}