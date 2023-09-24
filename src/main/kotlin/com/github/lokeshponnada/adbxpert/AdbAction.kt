package com.github.lokeshponnada.adbxpert

import com.github.lokeshponnada.adbxpert.ADBActionMap.Companion.CHANGE_TARGET_APP
import com.github.lokeshponnada.adbxpert.ADBActionMap.Companion.SET_TARGET_APP
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.ui.components.JBList
import java.awt.BorderLayout
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.swing.*


class AdbAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project

        // Create a dialog builder
        val dialogBuilder = DialogBuilder(project)


        val targetAppFieldText = if (ADBActionMap.targetApp.isBlank())
                                        SET_TARGET_APP
                                 else CHANGE_TARGET_APP

        val inputTextField = JTextField()
        inputTextField.text = "$targetAppFieldText (Enter package name)"
        val inputPanel = JPanel()
        inputPanel.layout = BorderLayout()
        inputPanel.add(inputTextField, BorderLayout.CENTER)


//        if (ADBActionMap.targetApp.isNotBlank()){ listModel.addElement("Current Target App : ${ADBActionMap.targetApp}") }

        // Create a list of items
        val itemList = ADBActionMap.actionsMap.keys.toList()
        val listModel = DefaultListModel<JButton>()
        itemList.forEach { itemText ->
            val button = JButton(itemText)
            button.addActionListener {
                val actionToExecute = ADBActionMap.actionsMap[itemText]
                val output = actionToExecute!!.runCommand(File("/Users/lokeshponnada/Library/Android/sdk/platform-tools"))
                println("Executed Action : $output")
            }
            listModel.addElement(button)
        }
        val list = JBList(listModel)
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION



        val mainPanel = JPanel(BorderLayout())
        mainPanel.add(inputPanel, BorderLayout.NORTH)
        mainPanel.add(list, BorderLayout.CENTER)

        dialogBuilder.setCenterPanel(mainPanel)

        dialogBuilder.setOkOperation {
            val userInput = inputTextField.text
            val packageNameRegex = "^[a-z][a-z\\d]*(\\.[a-z\\d]+)*$".toRegex()
            val isValidPackageName = packageNameRegex.matches(userInput)
            if(isValidPackageName){
                ADBActionMap.targetApp = userInput
            }
            dialogBuilder.dialogWrapper.close(0)
        }

        dialogBuilder.show()

    }

    private fun String.runCommand(workingDir: File): String? {
        return try {
            val parts = this.split("\\s".toRegex())
            val proc = ProcessBuilder(*parts.toTypedArray())
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            proc.waitFor(60, TimeUnit.MINUTES)
            proc.inputStream.bufferedReader().readText()
        } catch(e: IOException) {
            e.printStackTrace()
            null
        }
    }

}
