package com.github.lokeshponnada.adbxpert

import com.github.lokeshponnada.adbxpert.ADBActionMap.Companion.CHANGE_TARGET_APP
import com.github.lokeshponnada.adbxpert.ADBActionMap.Companion.SET_TARGET_APP
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.swing.*


class AdbAction : AnAction() {

    companion object{
        var targetApp = ""
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project

        targetApp = AdbPluginState().getMyString()

        // Create a dialog builder
        val dialogBuilder = DialogBuilder(project)

        val isTargetAppSet = targetApp.isNotBlank()

        val targetAppButtonText = if (isTargetAppSet)
                                        CHANGE_TARGET_APP
                                    else SET_TARGET_APP
        val targetAppButton = JButton()
        targetAppButton.text = "$targetAppButtonText"
        targetAppButton.addActionListener {
            dialogBuilder.dialogWrapper.close(DialogWrapper.CLOSE_EXIT_CODE)
            showTargetAppDialog(project)
        }


        val inputPanel = JPanel()
        inputPanel.layout = BorderLayout()
        inputPanel.add(targetAppButton, BorderLayout.CENTER)

        val listModel = DefaultListModel<String>()



        if(isTargetAppSet){
            // Provide a button with change target app text
            // Show a text with current target app
            listModel.addElement("Current Target App - ${targetApp}")
            val appSpecificMap = mapOf(
                "Launch App" to "adb shell monkey -p $targetApp -c android.intent.category.LAUNCHER 1",
                "Kill App" to "adb shell am force-stop $targetApp",
                "Clear App Data" to "adb shell pm clear $targetApp",
                "Uninstall App" to "adb shell pm uninstall $targetApp",
            )
            val modifiedMap = appSpecificMap + ADBActionMap.actionsMap
            ADBActionMap.actionsMap = modifiedMap
        }else{
            // provide a button with set target app text
            // do not show launch, kill, clear, uninstall app
        }

        val itemList = ADBActionMap.actionsMap.keys
        itemList.forEach { itemText ->
           listModel.addElement(itemText)
        }
        val list = JBList(listModel)
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION

        list.addListSelectionListener {
            if(!it.valueIsAdjusting){
                val selIndex = list.selectedIndex
                val selItem = listModel.getElementAt(selIndex)
                val actionToExecute = ADBActionMap.actionsMap[selItem]
                val actionRes = actionToExecute?.runCommand(File("/Users/lokeshponnada/Library/Android/sdk/platform-tools"))
                println("Executed Action $actionToExecute - Result : $actionRes")
            }
        }

        val mainPanel = JPanel(BorderLayout())
        mainPanel.add(inputPanel, BorderLayout.NORTH)
        mainPanel.add(list, BorderLayout.CENTER)

        dialogBuilder.setCenterPanel(mainPanel)


        dialogBuilder.setOkActionEnabled(false)
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


    private fun showTargetAppDialog(project: Project?) {
        val dialogBuilder = DialogBuilder(project)
        dialogBuilder.title("Set Target App")

        val label = JLabel("Enter Package Name")
        val textField = JBTextField() // Input text field


        val contentPanel = JPanel()
        contentPanel.layout = BorderLayout()
        contentPanel.add(label, BorderLayout.NORTH)
        contentPanel.add(textField, BorderLayout.CENTER)

        dialogBuilder.centerPanel(contentPanel)

        dialogBuilder.setOkOperation {
            val inputText = textField.text
            val packageNameRegex = "^[a-z][a-z0-9]*(\\.[a-z][a-z0-9]*)+$".toRegex()
            val isValidPackageName = packageNameRegex.matches(inputText)

            if (isValidPackageName) {
                // Handle the valid package name
                saveTargetApp(inputText)
                JOptionPane.showMessageDialog(null, "Target App Set to - $inputText")
                dialogBuilder.dialogWrapper.close(DialogWrapper.OK_EXIT_CODE)
            } else {
                // Handle an invalid package name
                JOptionPane.showMessageDialog(null, "Not a valid package name - $inputText")
            }
        }

        // Set an action listener for the Cancel button
        dialogBuilder.setCancelOperation {
            dialogBuilder.dialogWrapper.close(DialogWrapper.CANCEL_EXIT_CODE)
        }

        dialogBuilder.showModal(true)
    }

    private fun saveTargetApp(packageName:String){
        val pluginState = AdbPluginState()
        pluginState.setMyString(packageName)
    }


}
