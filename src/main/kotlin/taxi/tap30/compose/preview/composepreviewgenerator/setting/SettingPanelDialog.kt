package taxi.tap30.compose.preview.composepreviewgenerator.setting

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.util.*
import java.awt.BorderLayout
import java.awt.Image
import javax.swing.*

class SettingPanelDialog : DialogWrapper(true) {
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
        panel.preferredHeight = 1000
        val propertiesCode = JTextArea(
            """
                @Composable
                fun SettingPanel(propertyList: List<Property>, modifier: Modifier) {

                    val colorProperties = propertyList.filterIsInstance<ColorPickerProperty>()
                    val otherProperties = propertyList.filterNot { colorProperties.contains(it) }

                    Column(
                        modifier = modifier
                            .background(Color(0xffececec)),
                        horizontalAlignment = Alignment.End
                    ) {
                        if (colorProperties.isNotEmpty())
                            Text(
                                text = "Colors",
                                modifier = Modifier.padding(8.dp).padding(top = 12.dp),
                            )

                        Column {
                            colorProperties.forEach {
                                ColorPicker(title = it.title, colors = it.options, colorPicked = it.action)
                            }
                        }

                        if (otherProperties.isNotEmpty()) {
                            Text(
                                text = "Properties",
                                modifier = Modifier.padding(8.dp).padding(top = 12.dp),
                            )
                        }

                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            otherProperties.forEach {
                                when (it) {
                                    is SwitchButtonProperty ->{
                                        SwitchProperty( // implement your own implementation
                                            title = it.title,
                                            onSwitchClicked = it.action,
                                            value = it.value
                                        )
                                    }
                                    is DropDownProperty<*> -> {
                                        val property = it as MultiChoiceProperty<*>
                                        DropDown( // implement your own implementation
                                            title = property.title,
                                            options = property.options,
                                            action = property.action
                                        )
                                    }
                                }
                            }
                        }
                    }

                }
        """.trimIndent()
        ).apply {
            isEditable = false  // Make text areas read-only
            lineWrap = true     // Enable line wrapping
            wrapStyleWord = true
            minimumWidth = 1000
            border = BorderFactory.createTitledBorder("Properties codes")
        }
        panel.add(propertiesCode)
        return JScrollPane(panel)
    }

    override fun doOKAction() {
        super.doOKAction()
       FixturesDialog().show()
    }
}