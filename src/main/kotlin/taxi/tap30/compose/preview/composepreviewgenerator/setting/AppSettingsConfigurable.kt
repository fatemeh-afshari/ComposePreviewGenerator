package taxi.tap30.compose.preview.composepreviewgenerator.setting

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class AppSettingsConfigurable : Configurable {
    private var mySettingsComponent: AppSettingsComponent? = null

    override fun getDisplayName(): String = "ComposePreviewGenerator"

    override fun createComponent(): JComponent {
        mySettingsComponent = AppSettingsComponent()
        return mySettingsComponent!!.panel
    }

    override fun isModified(): Boolean {
        val settings = AppSettingsState.getInstance()
        return mySettingsComponent!!.previewAnnotationText != settings.previewAnnotation ||
                mySettingsComponent!!.previewThemeText != settings.previewTheme
    }

    override fun apply() {
        val settings = AppSettingsState.getInstance()
        settings.previewAnnotation = mySettingsComponent!!.previewAnnotationText
        settings.previewTheme = mySettingsComponent!!.previewThemeText
    }

    override fun reset() {
        val settings = AppSettingsState.getInstance()
        mySettingsComponent!!.previewAnnotationText = settings.previewAnnotation
        mySettingsComponent!!.previewThemeText = settings.previewTheme
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }
}