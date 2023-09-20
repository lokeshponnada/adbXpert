package com.github.lokeshponnada.adbxpert

import com.github.lokeshponnada.adbxpert.ADBActionMap.Companion.CHANGE_TARGET_APP
import com.github.lokeshponnada.adbxpert.ADBActionMap.Companion.SET_TARGET_APP
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.ui.components.JBList
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.swing.DefaultListModel
import javax.swing.ListSelectionModel


class AdbAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project

        // Create a list of items
        val itemList = ADBActionMap.actionsMap.keys.toList()
        val targetAppFieldText = if (ADBActionMap.targetApp.isBlank()) SET_TARGET_APP else CHANGE_TARGET_APP

        // Create a dialog builder
        val dialogBuilder = DialogBuilder(project)

        // Create a list component
        val listModel = DefaultListModel<String>()
        listModel.addElement(targetAppFieldText)
        itemList.forEach { listModel.addElement(it) }
        val list = JBList(listModel)
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION

        // Add a listener to handle item selection
        list.addListSelectionListener {
            if (!it.valueIsAdjusting) {
                val selectedIndex = list.selectedIndex
                val selectedValue = list.selectedValue

                if(selectedIndex == 0){
//                    showPackageSetDialog()
                    return@addListSelectionListener
                }


                if (selectedValue != null) {
                    val actionToExecute = ADBActionMap.actionsMap[selectedValue]
                    val output = actionToExecute!!.runCommand(File("/Users/lokeshponnada/Library/Android/sdk/platform-tools"))
                    println("Executed Action : $output")
                }
            }
        }

        // Set the list as the content of the dialog
        dialogBuilder.centerPanel(list)

        // Set the dialog title and show it
        dialogBuilder.title("Clickable List")
        dialogBuilder.show()
    }

    private fun String.runCommand(workingDir: File): String? {
        try {
            val parts = this.split("\\s".toRegex())
            val proc = ProcessBuilder(*parts.toTypedArray())
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            proc.waitFor(60, TimeUnit.MINUTES)
            return proc.inputStream.bufferedReader().readText()
        } catch(e: IOException) {
            e.printStackTrace()
            return null
        }
    }

}
