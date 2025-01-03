package taxi.tap30.compose.preview.composepreviewgenerator

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import taxi.tap30.compose.preview.composepreviewgenerator.framework.XParameter
import java.awt.Component
import java.awt.FlowLayout
import javax.swing.*


class ParameterDialog(
    project: Project,
    val parameters: List<XParameter<*>>,
    val onOkPressed: (result: Map<String, String>) -> Unit
) : DialogWrapper(project) {

    val panel = JPanel()
    private val parameterCheckboxes: Map<XParameter<*>, List<Component>> =
        parameters.associateWith { param ->
            param.getMockList().second.map { JCheckBox(it.key.toString()) }
        }


    init {
        init()
        title = "Parameter Options"
    }

    override fun createCenterPanel(): JComponent {
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        updatePanel()
        val scrollPane = JScrollPane(panel)
        return scrollPane
    }

    private fun updatePanel() {
        panel.removeAll()
        parameters.forEach { parameter ->
            val parameterPanel = JPanel()
            parameterPanel.layout = FlowLayout(FlowLayout.LEFT)
            parameterPanel.add(JLabel(parameter.name + ": "))

            parameterCheckboxes[parameter]?.forEach { checkbox ->
                parameterPanel.add(checkbox)
            }
            panel.add(parameterPanel)
        }
    }

    override fun doOKAction() {
        super.doOKAction()
        val result = mutableMapOf<String, String>()
        parameters.forEach { parameter ->
            val selectedCheckBox = parameterCheckboxes[parameter]?.filterIsInstance<JCheckBox>()?.firstOrNull {
                it.isSelected
            }
            result.put(
                parameter.name,
                selectedCheckBox?.text ?: parameterCheckboxes[parameter]?.filterIsInstance<JCheckBox>()?.first()?.text
                ?: ""
            )
        }
        onOkPressed(result)
    }

}