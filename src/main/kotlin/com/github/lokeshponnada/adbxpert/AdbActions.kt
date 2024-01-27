package com.github.lokeshponnada.adbxpert


class AdbActions {


    companion object{

        const val SET_TARGET_APP = "Set Target App"
        const val CHANGE_TARGET_APP = "Change Target App"

// app specific commands in UI.kt
        var actionsMap = mapOf(

            "Clear Logcat" to "ADBPATH logcat -c",

            "Show Layout Bounds" to "ADBPATH shell setprop debug.layout true &&  ADBPATH shell service call activity 1599295570",
            "Hide Layout Bounds" to "ADBPATH shell setprop debug.layout false && ADBPATH shell service call activity 1599295570",

            "Enable Talkback" to "ADBPATH shell settings put secure enabled_accessibility_services com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService\n",
            "Disable Talkback" to "ADBPATH shell settings put secure enabled_accessibility_services com.android.talkback/com.google.android.marvin.talkback.TalkBackService",

            "Launch Home" to "ADBPATH shell am start -W -c android.intent.category.HOME -a android.intent.action.MAIN",
            "Launch Settings App"       to "ADBPATH shell am start -n com.android.settings/.Settings",
            "Launch Developer Settings" to "ADBPATH shell am start -a com.android.settings.APPLICATION_DEVELOPMENT_SETTINGS",

            "Start Server " to "ADBPATH start-server",
            "Kill Server " to "ADBPATH kill-server",

            "Print Device Release Version" to "ADBPATH shell getprop ro.build.version.release",
            "Reboot Connected Device" to "ADBPATH reboot",

            "Bug Report" to "ADBPATH bugreport",
        )


    }

}