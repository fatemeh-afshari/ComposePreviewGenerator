package taxi.tap30.compose.preview.composepreviewgenerator.setting

import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JPanel

class AppSettingsComponent {
    val panel: JPanel
    private val previewAnnotation = JBTextField()
    private val previewTheme = JBTextField()

    init {
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Enter preview annotation: "), previewAnnotation, 1, true)
            .addLabeledComponent(JBLabel("Enter preview theme: "), previewTheme, 1, true)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    var previewAnnotationText: String
        get() = previewAnnotation.text
        set(newText) {
            previewAnnotation.text = newText
        }


    var previewThemeText: String
        get() = previewTheme.text
        set(newText) {
            previewTheme.text = newText
        }
}