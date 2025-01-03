package taxi.tap30.compose.preview.composepreviewgenerator.setting

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "org.intellij.sdk.settings.AppSettingsState",
    storages = [Storage("SdkSettingsPlugin.xml")]
)
class AppSettingsState : PersistentStateComponent<AppSettingsState> {
    var previewAnnotation: String = "@Preview"
    var previewTheme: String = "PassengerPreviewTheme"
    var isDialogShown: Boolean = false
    override fun getState(): AppSettingsState = this

    override fun loadState(state: AppSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): AppSettingsState {
            return ServiceManager.getService(AppSettingsState::class.java)
        }
    }
}