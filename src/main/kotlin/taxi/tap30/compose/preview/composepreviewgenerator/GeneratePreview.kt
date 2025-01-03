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


class GeneratePreview : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val settings = AppSettingsState.getInstance()
        val project: Project = event.project ?: return
        val psiFile = event.getData(CommonDataKeys.PSI_FILE) ?: return
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return
        val document = editor.document
        val psiElement: PsiElement? = event.getData(com.intellij.openapi.actionSystem.PlatformDataKeys.PSI_ELEMENT)
        val function = psiElement as? KtNamedFunction ?: return
        val params = function.valueParameters.filter { !it.hasDefaultValue() }.toXParameter(project)
        val flatParameters = flattenParameters(params.filter { it !is XUnKnown && it !is XFunction })
        if (flatParameters.isNotEmpty()) {
            val dialog = ParameterDialog(project, flatParameters) { result ->
                writeCodesToFile(params, result, settings, function, project, document, editor, psiFile)
            }
            dialog.showAndGet()
        } else {
            writeCodesToFile(params, emptyMap(), settings, function, project, document, editor, psiFile)
        }

    }

    private fun writeCodesToFile(
        params: List<XParameter<*>>,
        result: Map<String, String>,
        settings: AppSettingsState,
        function: KtNamedFunction,
        project: Project,
        document: Document,
        editor: Editor,
        psiFile: PsiFile
    ) {
        val parameterInfo = params.joinToString("\n") { param ->
            generateCodeForParams(param, result, function)
        }
        val newLine =
            "${settings.previewAnnotation}\n@Composable\nprivate fun ${function.name}Preview()\n{\n${settings.previewTheme}{\n${function.name}($parameterInfo)\n}\n}"
        generateCode(project, document, newLine, editor, psiFile)

    }



    private fun generateCodeForParams(
        param: XParameter<*>, result: Map<String, String>, function: KtNamedFunction, prefix: String? = null
    ): String {
        return if (param.isPrimitiveOrEnum()) "${param.name}= ${
            param.getMockList().second.entries.firstOrNull {
                if (prefix != null) it.key.toString() == result["$prefix.${param.name}"]
                else it.key.toString() == result[param.name]
            }?.value ?: "null"
        },"
        else if (param is XDataClass) {
            val currentPrefix = if (prefix != null) "$prefix.${param.name}" else param.name
            "${param.name}=${param.type.text.removeLastNullableChar()}(${
                param.xParameters.joinToString("\n") {
                    generateCodeForParams(
                        it, result, function, currentPrefix
                    )
                }
            }),"
        } else if (param is XObject) {
            "${param.name} = ${param.objectName}"
        } else if (param is XSealed) {
            val firstSealedClass = param.getMockList().second.first().value
            val currentPrefix = if (prefix != null) "$prefix.${param.name}" else param.name
            if (firstSealedClass is XDataClass) {
                "${param.name}=${firstSealedClass.name}(${
                    firstSealedClass.xParameters.joinToString("\n") {
                        generateCodeForParams(
                            it, result, function, currentPrefix
                        )
                    }
                }),"
            } else {
                "${param.name} = ${firstSealedClass.type.text}"
            }
        } else if (param is XList) {
            val listTypeClass = param.getMockList().second.first().value
            val currentPrefix = if (prefix != null) "$prefix.${param.name}" else param.name
            "${param.name}=List(3){${
                generateCodeForParams(listTypeClass, result, function, currentPrefix).replace("${param.name}=", "")
                    .removeLastComma()
            }},"

        } else if (param is XFunction) {
            "${param.name}= {},"
        } else if (function.valueParameters.firstOrNull { it.name == param.name && it.hasDefaultValue() } != null) {
            ""
        } else {
            "${param.name}= ${result[param.name] ?: "null"},"
        }
    }

}









