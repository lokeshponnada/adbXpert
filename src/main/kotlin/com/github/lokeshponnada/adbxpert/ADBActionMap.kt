package com.github.lokeshponnada.adbxpert

import com.github.lokeshponnada.adbxpert.AdbAction.Companion.targetApp

class ADBActionMap {


    companion object{

        const val SET_TARGET_APP = "Set Target App"
        const val CHANGE_TARGET_APP = "Change Target App"


        var actionsMap = mapOf(

            "Show Layout Bounds" to "adb shell setprop debug.layout true && adb shell service call activity 1599295570",
            "Hide Layout Bounds" to "adb shell setprop debug.layout false && adb shell service call activity 1599295570",

            "Enable Talkback" to "adb shell settings put secure enabled_accessibility_services com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService\n",
            "Disable Talkback" to "adb shell settings put secure enabled_accessibility_services com.android.talkback/com.google.android.marvin.talkback.TalkBackService",

            "Kill Server - ADB" to "adb kill-server",
            "Start Server - ADB" to "adb start-server",

            "Device Release Version" to "adb shell getprop ro.build.version.release",
            "Launch Home" to "adb shell am start -W -c android.intent.category.HOME -a android.intent.action.MAIN",

            "Reboot Connected Device" to "adb reboot"

        )


    }

}