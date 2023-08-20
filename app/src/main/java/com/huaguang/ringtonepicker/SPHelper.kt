package com.huaguang.ringtonepicker

import android.content.Context

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

    fun saveRingtoneInfo(song: Song) {
        sp.edit().apply {
            putString("ringtone_uri", song.songUri.toString())
            putString("ringtone_title", song.songTitle)
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

    fun getTitle(): String {
        return sp.getString("ringtone_title", "") ?: ""
    }

    /**
     * 在应用的 ”安装-卸载“ 生命周期内只执行一次！
     */
    fun doOnce(callback: () -> Unit) {
        if (!getFlag("executed")) {
            callback()
            setFlag("executed", true)
        }
    }

}