package taxi.tap30.compose.preview.composepreviewgenerator.setting

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project

class MyPluginProjectComponent(private val project: Project) : ProjectComponent {

    override fun projectOpened() {
        // Show the onboarding dialog when the project is opened
        showOnboardingDialog()
    }

    private fun showOnboardingDialog() {
        val settings = AppSettingsState.getInstance()
        if (!settings.isDialogShown) {
            StartupDialog().show()
            settings.isDialogShown = true
        }

    }
}