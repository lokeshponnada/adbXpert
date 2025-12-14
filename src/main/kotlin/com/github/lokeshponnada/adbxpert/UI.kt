package com.github.lokeshponnada.adbxpert

import com.github.lokeshponnada.adbxpert.AdbActions.Companion.CHANGE_TARGET_APP
import com.github.lokeshponnada.adbxpert.AdbActions.Companion.SET_TARGET_APP
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import java.io.IOException
import javax.swing.*
import com.intellij.notification.Notifications


class UI : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project

        targetApp = PluginState.getMyString(PluginState.KEYS.TARGET_APP)

        // Create a dialog builder
        val dialogBuilder = DialogBuilder(project)

        val isTargetAppSet = targetApp.isNotBlank()

        val targetAppButtonText = if (isTargetAppSet)
                                        CHANGE_TARGET_APP
                                    else SET_TARGET_APP
        val targetAppButton = JButton()
        targetAppButton.text = targetAppButtonText
        targetAppButton.addActionListener {
            dialogBuilder.dialogWrapper.close(DialogWrapper.CLOSE_EXIT_CODE)
            showTargetAppDialog(project)
        }

        val inputPanel = JPanel()
        inputPanel.layout = BorderLayout()
        inputPanel.add(targetAppButton, BorderLayout.NORTH)

        val choosePathButton = JButton("Choose ADB Path")
        choosePathButton.addActionListener {
            val fileChooser = FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()
            val virtualFile = FileChooser.chooseFile(fileChooser, null, null)
            if (virtualFile != null) {
                val selectedPath = virtualFile.path
                saveAdbPath(selectedPath)
            }
        }
        inputPanel.add(choosePathButton,BorderLayout.CENTER)

        val listModel = DefaultListModel<String>()
        if(isTargetAppSet){
            // Provide a button with change target app text
            // Show a text with current target app
            listModel.addElement("Current Target App - ${targetApp}")
            val appSpecificMap = mapOf(
                "Launch App" to "ADBPATH shell monkey -p $targetApp -c android.intent.category.LAUNCHER 1",
                "Kill App" to "ADBPATH shell am force-stop $targetApp",
                "Clear App Data" to "ADBPATH shell pm clear $targetApp",
                "Uninstall App" to "ADBPATH shell pm uninstall $targetApp",
                "Trigger Critical Memory Event" to "ADBPATH shell am send-trim-memory $targetApp 15",
            )
            val modifiedMap = appSpecificMap + AdbActions.actionsMap
            AdbActions.actionsMap = modifiedMap
        }else{
            // provide a button with set target app text
            // do not show launch, kill, clear, uninstall app
        }

        val itemList = AdbActions.actionsMap.keys
        itemList.forEach { itemText ->
           listModel.addElement(itemText)
        }
        val list = JBList(listModel)
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION

        list.addListSelectionListener {
            if (!it.valueIsAdjusting) {
                val adbPath = getAdbPath()
                if (adbPath.isNotBlank()) {
                    val selIndex = list.selectedIndex
                    val selItem = listModel.getElementAt(selIndex)
                    val actionToExecute = AdbActions.actionsMap[selItem]
                    actionToExecute?.let {
                        val modifiedCommand = it.replace("ADBPATH","$adbPath")
                        val actionRes = modifiedCommand.runCommand()
                        if(actionRes.exitCode != 0){
                            showError(actionRes)
                            MixPanelLogger.logEvent(actionToExecute,false)
                        }else {
                            println(actionRes.output)
                            MixPanelLogger.logEvent(actionToExecute,true)
                        }
                    }
                }else{
                    val errorMessage = "ADB path not set in the plugin"
                    val dialogTitle = "Error"
                    Messages.showErrorDialog(errorMessage, dialogTitle)
                }
            }

        }
        val mainPanel = JPanel(BorderLayout())
        mainPanel.add(inputPanel, BorderLayout.NORTH)
        mainPanel.add(list, BorderLayout.CENTER)
        dialogBuilder.setCenterPanel(mainPanel)
        dialogBuilder.setOkActionEnabled(false)
        dialogBuilder.show()

    }

    /* Post any information after executing a command */
    private fun postAnyAdditionalInfo(selectedItem:String){
        if(selectedItem.contains("Simulate Slow Internet")){
            showNotification("Heads Up!","Do not forget to disable throttling")
        }
    }

    private fun showNotification(title:String, content:String){
        val notification = Notification("com.github.lokeshponnada.adbxpert",
            title,content,
            NotificationType.WARNING
        )
        Notifications.Bus.notify(notification)
    }

    private fun showError(commandResult: CommandResult){
        val dialogTitle = "Error ${commandResult.exitCode}"
        val errorMessage = "Please make sure that ADB path is set correctly, device is connected, developer options are enabled and usb debugging is authorized"
        Messages.showErrorDialog(errorMessage, dialogTitle)
    }

    private data class CommandResult(val exitCode: Int, val output: String)

    private fun String.runCommand(): CommandResult {
        return try {
            val proc = ProcessBuilder("/bin/bash", "-c", this)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            val exitCode = proc.waitFor()
            val output = proc.inputStream.bufferedReader().readText()
            CommandResult(exitCode, output)
        } catch (e: IOException) {
            e.printStackTrace()
            CommandResult(-1, e.message ?: "")
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
        PluginState.setMyString(PluginState.KEYS.TARGET_APP, packageName)
    }

    private fun saveAdbPath(path:String){
        PluginState.setMyString(PluginState.KEYS.ADB_PATH,path)
    }

    private fun getAdbPath():String{
        return PluginState.getMyString(PluginState.KEYS.ADB_PATH)
    }
}

var targetApp = ""