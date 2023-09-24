package com.github.lokeshponnada.adbxpert

class ADBActionMap {


    companion object{

        var targetApp = "com.onepeloton.callisto.dev"

        val SET_TARGET_APP = "Set Target App"
        val CHANGE_TARGET_APP = "Change Target App"

        val actionsMap = mapOf(

            "Launch App" to "adb shell monkey -p $targetApp -c android.intent.category.LAUNCHER 1",
            "Kill App" to "adb shell am force-stop $targetApp",
            "Clear App Data" to "adb shell pm clear $targetApp",
            "Uninstall App" to "adb shell pm uninstall $targetApp",

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