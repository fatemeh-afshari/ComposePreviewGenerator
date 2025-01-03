package taxi.tap30.compose.preview.composepreviewgenerator.setting

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.util.maximumWidth
import com.intellij.ui.util.minimumWidth
import com.intellij.ui.util.preferredHeight
import com.intellij.ui.util.preferredWidth
import java.awt.BorderLayout
import java.awt.Image
import javax.swing.*

class PropertiesDialog : DialogWrapper(true) {
    init {
        title = "Welcome to Compose Preview Generator Plugin"
        init()
    }
    override fun createActions(): Array<Action> {
        val okAction = super.getOKAction()
        val cancelAction = super.getCancelAction()
        okAction.putValue(Action.NAME, "Next")
        cancelAction.putValue(Action.NAME, "Close")
        return arrayOf(okAction, cancelAction)
    }
    override fun createCenterPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.preferredWidth = 700
        panel.preferredHeight = 500
        val propertiesCode = JTextArea(
            """
            interface Property {
                val title: String
            }

            class SwitchButtonProperty(override val title: String,
             val value: Boolean, 
             val action: (Boolean) -> Unit
             ) : Property

            interface MultiChoiceProperty<T> : Property {
                var value: T
                val options: List<T>
                val action: (Int) -> Unit
            }

            class DropDownProperty<T>(
                override val title: String,
                override var value: T,
                override val options: List<T>,
                override val action: (Int) -> Unit
            ) : MultiChoiceProperty<T>
        """.trimIndent()
        ).apply {
            isEditable = false  // Make text areas read-only
            lineWrap = true     // Enable line wrapping
            wrapStyleWord = true
            minimumWidth = 1000
            border = BorderFactory.createTitledBorder("Properties codes")
        }
        panel.add(propertiesCode)
        // Add additional UI components as needed
        return JScrollPane(panel)
    }

    override fun doOKAction() {
        super.doOKAction()
        SettingPanelDialog().show()
    }
}