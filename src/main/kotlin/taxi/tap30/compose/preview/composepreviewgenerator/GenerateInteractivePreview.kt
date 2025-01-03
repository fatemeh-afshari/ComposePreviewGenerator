package taxi.tap30.compose.preview.composepreviewgenerator

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.jetbrains.rd.util.first
import org.jetbrains.kotlin.psi.KtNamedFunction
import taxi.tap30.compose.preview.composepreviewgenerator.framework.*
import taxi.tap30.compose.preview.composepreviewgenerator.setting.AppSettingsState
import taxi.tap30.compose.preview.composepreviewgenerator.util.*


class GenerateInteractivePreview : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val settings = AppSettingsState.getInstance()
        val project: Project = event.project ?: return
        val psiFile = event.getData(CommonDataKeys.PSI_FILE) ?: return
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return
        val document = editor.document
        val psiElement: PsiElement? = event.getData(com.intellij.openapi.actionSystem.PlatformDataKeys.PSI_ELEMENT)
        val function = psiElement as? KtNamedFunction ?: return
        val params = function.valueParameters.filter { !it.hasDefaultValue() }.toXParameter(project)
        val flatParameters = flattenInteractivePreviewParameters(params.filter { it !is XUnKnown && it !is XFunction })
        if (flatParameters.isNotEmpty()) {
            writeCodesToFile(params, flatParameters, settings, function, project, document, psiFile, editor)
        }
    }

    private fun writeCodesToFile(
        params: List<XParameter<*>>,
        flatParamList: List<XParameter<*>>,
        settings: AppSettingsState,
        function: KtNamedFunction,
        project: Project,
        document: Document,
        psiFile: PsiFile,
        editor: Editor
    ) {
        val parameterInfo = params.joinToString("\n") { param ->
            generateCodeForParams(param, function)
        }

        val properties = flatParamList.joinToString("\n") { param ->
            param.getStateCode(transformString(param.name))?.third ?: ""
        }
        val newLine =
            "${settings.previewAnnotation}\n@Composable\nprivate fun ${function.name}Preview()\n{\n${settings.previewTheme}{$properties\nColumn{\n${function.name}($parameterInfo)\n ${
                generateSettingPanelCode(
                    flatParamList.mapNotNull { it.getStateCode(transformString(it.name))?.first }.toList()
                )
            }\n}\n}\n}"
        generateCode(project, document, newLine, editor, psiFile)
    }

    private fun generateSettingPanelCode(properties: List<String>): String {
        val propertiesText = properties.joinToString(",\n") { prop ->
            prop
        }
        return """ SettingPanel(
            propertyList = listOf(
                $propertiesText
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topEnd = 16.dp, topStart = 16.dp))
        )
        """
    }


    fun transformString(input: String): String {
        val result = StringBuilder()
        var isDot = false
        for (ch in input) {
            if (ch == '.') {
                isDot = true
            } else {
                if (isDot) {
                    result.append(ch.uppercaseChar())
                    isDot = false
                } else {
                    result.append(ch)
                }
            }
        }
        return result.toString()
    }

    private fun generateCodeForParams(
        param: XParameter<*>, function: KtNamedFunction, prefix: String? = null
    ): String {
        return if (param.isPrimitiveOrEnum() || param is XSealed) "${param.name}= ${
            param.getStateCode(if (prefix != null) transformString("$prefix.${param.name}") else param.name)?.second ?: "null"
        }," else if (param is XDataClass) {
            val currentPrefix = if (prefix != null) "$prefix.${param.name}" else param.name
            "${param.name}=${param.type.text.removeLastNullableChar()}(${
                param.xParameters.joinToString("\n") {
                    generateCodeForParams(
                        it, function, currentPrefix
                    )
                }
            }),"
        } else if (param is XObject) {
            "${param.name} = ${param.objectName}"
        } else if (param is XList) {
            val listTypeClass = param.getMockList().second.first().value
            val currentPrefix = if (prefix != null) "$prefix.${param.name}" else param.name
            "${param.name}=List(3){${
                generateCodeForParams(listTypeClass, function, currentPrefix).replace("${param.name}=", "")
                    .removeLastComma()
            }},"

        } else if (param is XFunction) {
            "${param.name}= {},"
        } else if (function.valueParameters.firstOrNull { it.name == param.name && it.hasDefaultValue() } != null) {
            ""
        } else {
            "${param.name}= null,"
        }
    }

}









