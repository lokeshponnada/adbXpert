package com.github.lokeshponnada.adbxpert

import com.intellij.ide.util.PropertiesComponent

class PluginState {
    companion object {
         const val TargetApp = "TargetApp"
         const val AdbPath = "AdbPath"
    }

    fun getMyString(key:String): String {
        return PropertiesComponent.getInstance().getValue(key) ?: ""
    }

    fun setMyString(key:String,value: String) {
        PropertiesComponent.getInstance().setValue(key, value)
    }
}