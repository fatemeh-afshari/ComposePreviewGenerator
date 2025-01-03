package taxi.tap30.compose.preview.composepreviewgenerator.util

import com.intellij.codeInsight.actions.ReformatCodeProcessor
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.DocumentUtil
import com.intellij.util.containers.toArray
import com.jetbrains.rd.util.first
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import org.jetbrains.kotlin.idea.intentions.typeArguments
import org.jetbrains.kotlin.idea.testIntegration.framework.KotlinPsiBasedTestFramework.Companion.asKtClassOrObject
import org.jetbrains.kotlin.load.kotlin.toSourceElement
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getValueParameters
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getImportableDescriptor
import org.jetbrains.kotlin.resolve.source.getPsi
import org.jetbrains.kotlin.types.KotlinType
import taxi.tap30.compose.preview.composepreviewgenerator.framework.*

fun List<KtParameter>.toXParameter(project: Project): List<XParameter<*>> {

    return this.map {
        mapToxParameter(it.name!!, it.typeReference, project)
    }
}

private fun mapToxParameter(
    name: String,
    typeReference: KtTypeReference?,
    project: Project
): XParameter<*> {
    return typeReference.toPrimitiveXParameter(name) ?: getNonPrimitiveParameters(name, typeReference, project)
}

private fun getNonPrimitiveParameters(

    name: String,
    typeReference: KtTypeReference?,
    project: Project
): XParameter<*> {
    val isFunction = typeReference?.typeElement is KtFunctionType
    return getDataClassTypeValues(name, typeReference!!, project)
        ?: getEnumClassTypeValues(typeReference, name)
        ?: getSealedClassesOfType(typeReference, name, project)
        ?: getListType(name, typeReference, project)
        ?: if (isFunction) {
            XFunction(name, typeReference)
        } else {
            XUnKnown(name, typeReference)
        }
}


fun getDataClassTypeValues(name: String, typeReference: KtTypeReference, project: Project): XParameter<*>? {

    val ktCLasses = resolveKotlinClasses(typeReference)
    val gType = getGenericTypeRef(typeReference)
    val psiClass = ktCLasses.firstOrNull()
    if (psiClass != null && psiClass.isData()) {
        val properties: List<XParameter<*>> = psiClass.getValueParameters().mapNotNull { param ->
            if (param.typeReference?.isPrimitive() == true) {
                param.typeReference.toPrimitiveXParameter(param.name!!)
            } else {
                getDataClassTypeValues(param.name!!, param.typeReference!!, project) ?: gType?.let {
                    getDataClassTypeValues(
                        param.name!!,
                        gType,
                        project
                    )
                }
            }
        }
        val data = XDataClass(name, typeReference, *properties.toArray(empty = emptyArray()))
        return data
    }
    return getEnumClassTypeValues(
        typeReference,
        name
    ) ?: getListType(name, typeReference, project)
    ?: getSealedClassesOfType(
        typeReference, name, project
    )
}


fun getSealedClassesOfType(typeReference: KtTypeReference, name: String, project: Project ): XSealed? {

    val ktCLasses = resolveKotlinClasses(typeReference!!)

    val sealedClass = ktCLasses.firstOrNull()

    if (sealedClass == null || !sealedClass.isSealed()) return null

    val containingFile = sealedClass.containingFile

    val classes =
        PsiTreeUtil.findChildrenOfType(containingFile.virtualFile.toPsiFile(project), KtClassOrObject::class.java)

    val genericTypeRef = getGenericTypeRef(typeReference)
    val subClasses = classes.filter {
        it.isSubclassOf(sealedClass)
    }.map { it.asKtClassOrObject()!! }.mapNotNull {
        if (it.isData()) {
            val properties: List<XParameter<*>> = it.getValueParameters().mapNotNull { param ->
                if (!param.hasDefaultValue() && param.typeReference != null) {
                    if (param.typeReference?.isPrimitive() == true) {
                        param.typeReference.toPrimitiveXParameter(param.name!!)
                    } else {
                        getDataClassTypeValues(
                            param.name!!, param.typeReference!!,
                            project
                        ) ?: getEnumClassTypeValues(
                            typeReference, name
                        ) ?: genericTypeRef?.let {
                            getDataClassTypeValues(param.name!!, genericTypeRef, project)
                        }
                    }
                } else {
                    null
                }
            }
            XDataClass(it.name!!, typeReference, *properties.toArray(empty = emptyArray()))
        } else if (it is KtObjectDeclaration) {
            XObject(it.name!!, typeReference, it.name!!)
        } else {
            null
        }
    }
    return XSealed(name, typeReference, *subClasses.toArray(empty = emptyArray()))

}

fun isGenericType(typeReference: KtTypeReference): Boolean {
    val typeElement = typeReference.typeElement ?: return false
    if (typeElement is KtUserType) {
        return typeElement.typeArgumentList?.arguments.isNullOrEmpty()
    }
    return false
}

private fun getGenericTypeRef(typeReference: KtTypeReference): KtTypeReference? {
    typeReference?.let {
        if (it.typeElement is KtUserType) {
            it.typeArguments().forEach { arg ->
                return arg.typeReference

            }
        } else if (it.typeElement is KtNullableType) {
            return it.typeElement?.typeArgumentsAsTypes?.firstOrNull()
        }
    }
    return null
}

fun KtClassOrObject.isSubclassOf(sealedClass: KtClassOrObject): Boolean {
    return this.superTypeListEntries.any { it.text.contains(sealedClass.name ?: "") }
}


fun getEnumClassTypeValues(typeReference: KtTypeReference, name: String): XEnumClass? {
    // Get the type of the parameter

    val ktCLasses = resolveKotlinClasses(typeReference)

    // Check if the type is a data class
    val psiClass = ktCLasses.firstOrNull()
    if (psiClass != null && psiClass.isEnum()) {
        val properties: List<String> = psiClass.declarations.map {
            "${psiClass.name}.${it.name}"
        }
        val enum = XEnumClass(name, typeReference, *properties.toArray(empty = emptyArray()))
        return enum
    }
    return null
}


private fun resolveKotlinClasses(typeRef: KtTypeReference): List<KtClass> {
    // Analyze the type reference to get the binding context
    val bindingContext = typeRef.typeElement?.analyze()

    // Get the type corresponding to the type reference
    val kotlinType: KotlinType = bindingContext?.get(BindingContext.TYPE, typeRef) ?: return emptyList()

    // Get all possible class descriptors for the type (handles type aliases, etc.)
    val descriptors = kotlinType.constructor.declarationDescriptor?.let { descriptor ->
        listOfNotNull(descriptor.original) + descriptor.getImportableDescriptor()
    } ?: emptyList()
    val ktClasses = descriptors.mapNotNull { descriptor ->
        // Obtain the PSI element associated with the descriptor
        val psi = descriptor.toSourceElement.getPsi()
        psi as? KtClass
    }

    return ktClasses
}


fun getListType(name: String, typeRef: KtTypeReference, project: Project): XParameter<*>? {
    val bindingContext = typeRef.typeElement?.analyze()
    val paramType: KotlinType? = bindingContext?.get(BindingContext.TYPE, typeRef)
    if (paramType != null) {
        val isList = paramType.constructor.declarationDescriptor?.fqNameSafe?.asString() == "kotlin.collections.List"
        val isMutableList =
            paramType.constructor.declarationDescriptor?.fqNameSafe?.asString() == "kotlin.collections.MutableList"

        if (isList || isMutableList) {
            val listArgType = paramType.arguments.firstOrNull()?.type
            if (listArgType != null) {
                typeRef.typeElement?.let { typeElement ->
                    val param = if (typeElement is KtUserType) {
                        mapToxParameter(name, typeElement.typeArguments.firstOrNull()?.typeReference, project)
                    } else if (typeElement is KtNullableType) {
                        mapToxParameter(
                            name,
                            typeElement.typeArgumentsAsTypes.firstOrNull(),
                            project
                        )
                    } else null

                    if (param != null) {
                        return XList(name, typeRef, param)
                    }
                }

            }
        }
    }
    return null
}

fun flattenInteractivePreviewParameters(params: List<XParameter<*>>, prefix: String? = null): List<XParameter<*>> {
    val result = mutableListOf<XParameter<*>>()
    params.forEach {
        if (it.isPrimitiveOrEnum() || it is XObject || it is XSealed) {
            result.add(if (prefix != null) it.copy("$prefix.${it.name}") else it)
        } else if (it is XDataClass) {
            val currentPrefix = if (prefix == null) it.name else "$prefix.${it.name}"
            result.addAll(flattenParameters(it.getMockList().second.values.flatten(), currentPrefix))
        } else if (it is XList) {
            val currentPrefix = if (prefix == null) it.name else "$prefix.${it.name}"
            result.addAll(flattenParameters(it.getMockList().second.values.toList(), currentPrefix))
        }
    }
    return result.toList()
}


fun flattenParameters(params: List<XParameter<*>>, prefix: String? = null): List<XParameter<*>> {
    val result = mutableListOf<XParameter<*>>()
    params.forEach {
        if (it.isPrimitiveOrEnum() || it is XObject) {
            result.add(if (prefix != null) it.copy("$prefix.${it.name}") else it)
        } else if (it is XDataClass) {
            val currentPrefix = if (prefix == null) it.name else "$prefix.${it.name}"
            result.addAll(flattenParameters(it.getMockList().second.values.flatten(), currentPrefix))
        } else if (it is XList) {
            val currentPrefix = if (prefix == null) it.name else "$prefix.${it.name}"
            result.addAll(flattenParameters(it.getMockList().second.values.toList(), currentPrefix))
        } else if (it is XSealed) {
            val firstSealedClass = it.getMockList().second.first().value
            if (firstSealedClass is XDataClass) {
                result.addAll(flattenParameters(firstSealedClass.getMockList().second.values.flatten(), it.name))
            } else if (firstSealedClass is XObject) {
                result.addAll(flattenParameters(listOf(firstSealedClass), it.name))
            }
        }
    }
    return result.toList()
}


fun String.removeLastComma(): String {
    val lastCommaIndex = this.lastIndexOf(',')
    return if (lastCommaIndex != -1) {
        this.removeRange(lastCommaIndex, lastCommaIndex + 1)
    } else {
        this
    }
}

fun String.removeLastNullableChar(): String {
    val lastChar = this.last()
    return if (lastChar == '?') {
        this.removeRange(this.lastIndex, this.lastIndex + 1)
    } else {
        this
    }
}


fun generateCode(
    project: Project,
    document: Document,
    newLine: String,
    editor: Editor,
    psiFile: PsiFile
) {
    WriteCommandAction.runWriteCommandAction(project) {
        val text = document.text
        document.insertString(text.length, "\n" + newLine + "\n")
        FileDocumentManager.getInstance().saveDocument(editor.getDocument())
        WriteAction.run<Throwable> {
            DocumentUtil.writeInRunUndoTransparentAction {
                ReformatCodeProcessor(
                    project,
                    psiFile,
                    null,
                    false
                ).run()
            }
        }
    }
}