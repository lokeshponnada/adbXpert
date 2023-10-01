package com.github.lokeshponnada.adbxpert

import com.intellij.ide.util.PropertiesComponent

class AdbPluginState {
    companion object {
        private const val MY_STRING_KEY = "AdbXpert.TargetApp"
    }

    fun getMyString(): String {
        return PropertiesComponent.getInstance().getValue(MY_STRING_KEY) ?: ""
    }

    fun setMyString(value: String?) {
        PropertiesComponent.getInstance().setValue(MY_STRING_KEY, value)
    }
}