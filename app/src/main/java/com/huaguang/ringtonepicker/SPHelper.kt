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

    init {
        saveFlag(false)
    }

    fun saveUri(value: String) {
        sp.edit().putString("ringtone_uri", value).apply()
    }

    fun getUri(): String {
        return sp.getString("ringtone_uri", "") ?: ""
    }

    fun doOnce(callback: () -> Unit) {
        if (!getFlag()) {
            callback()
            saveFlag(true)
        }
    }

    private fun saveFlag(value: Boolean) {
        sp.edit().putBoolean("executed", value).apply()
    }

    private fun getFlag(): Boolean {
        return sp.getBoolean("executed", true)
    }


}