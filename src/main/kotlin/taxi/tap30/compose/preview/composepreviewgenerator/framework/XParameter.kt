package taxi.tap30.compose.preview.composepreviewgenerator.framework

import org.jetbrains.kotlin.psi.KtTypeReference
import taxi.tap30.compose.preview.composepreviewgenerator.*
import taxi.tap30.compose.preview.composepreviewgenerator.Function
import taxi.tap30.compose.preview.composepreviewgenerator.util.removeLastComma
import taxi.tap30.compose.preview.composepreviewgenerator.util.removeLastNullableChar

abstract class XParameter<T>(open var name: String, open val type: KtTypeReference) {
    abstract fun getMockList(): Pair<String, Map<out InputType?, T>>
    abstract fun getStateCode(name: String): Triple<String, String, String>?

    abstract fun copy(newName: String): XParameter<T>
}

class XInt(override var name: String, override val type: KtTypeReference) :
    XParameter<Int>(name = name, type = type) {

    private val mockData = NumberInputType.entries.associateWith { getIntValue(it) }

    override fun getMockList(): Pair<String, Map<out InputType?, Int>> {
        return name to mockData
    }

    override fun getStateCode(name: String): Triple<String, String, String> {
        return generateDropDownCode(name, "intFixtures")
    }


    override fun copy(newName: String): XParameter<Int> {
        return XInt(newName, type)
    }

}


private fun generateDropDownCode(name: String, fixture: String) = Triple(
    "${name}Property", "${name}State", """
        var ${name}State by remember {
            mutableStateOf($fixture.first())
        }
         val ${name}Property = DropDownProperty(
            "${name}",
            ${name}State,
            $fixture
        ) {
           ${name}State=$fixture[it]
        }
       """
)

private fun generateDropDownCode(name: String, list: List<*>) = Triple(
    "${name}Property", "${name}State", """
        var ${name}State by remember {
            mutableStateOf(${list.first()})
        }
         val ${name}Property = DropDownProperty(
            "${name}",
            ${name}State,
            listOf(${list.joinToString(",")})
        ) {
           ${name}State=listOf(${list.joinToString(",")})[it]
        }
       """
)

private fun generateSwitchButtonCode(name: String) = Triple(
    "is${name}Property", "is${name}EnabledState", """
        var is${name}EnabledState by remember {
            mutableStateOf(false)
        }
         val ${name}Property =  SwitchButtonProperty(
        title = "${name}",
        value = is${name}EnabledState,
      ) {
        is${name}EnabledState = it
      }
       """
)

class XDouble(override var name: String, override val type: KtTypeReference) :
    XParameter<Double>(name = name, type = type) {

    private val mockData = NumberInputType.entries.associateWith { getDoubleValue(it) }

    override fun getMockList(): Pair<String, Map<out InputType?, Double>> {
        return name to mockData
    }

    override fun getStateCode(name: String): Triple<String, String, String> {
        return generateDropDownCode(name, "doubleFixtures")
    }

    override fun copy(newName: String): XParameter<Double> {
        return XDouble(newName, type)
    }

}

class XLong(override var name: String, override val type: KtTypeReference) :
    XParameter<Long>(name = name, type = type) {

    private val mockData = NumberInputType.entries.associateWith { getLongValue(it) }

    override fun getMockList(): Pair<String, Map<out InputType?, Long>> {
        return name to mockData
    }

    override fun getStateCode(name: String): Triple<String, String, String> {
        return generateDropDownCode(name, "longFixtures")
    }

    override fun copy(newName: String): XParameter<Long> {
        return XLong(newName, type)
    }
}


class XFloat(override var name: String, override val type: KtTypeReference) :
    XParameter<Float>(name = name, type = type) {

    private val mockData = NumberInputType.entries.associateWith { getFloatValue(it) }

    override fun getMockList(): Pair<String, Map<out InputType?, Float>> {
        return name to mockData
    }

    override fun getStateCode(name: String): Triple<String, String, String> {
        return generateDropDownCode(name, "floatFixtures")
    }

    override fun copy(newName: String): XParameter<Float> {
        return XFloat(newName, type)
    }
}

class XString(override var name: String, override val type: KtTypeReference) :
    XParameter<String>(name = name, type = type) {
    private val mockData = StringInputType.entries.associateWith { getStringValue(it) }

    override fun getMockList(): Pair<String, Map<out InputType?, String>> {
        return name to mockData
    }

    override fun getStateCode(name: String): Triple<String, String, String> {
        return generateDropDownCode(name, "stringFixtures")
    }

    override fun copy(newName: String): XParameter<String> {
        return XString(newName, type)
    }
}


class XColor(override var name: String, override val type: KtTypeReference) :
    XParameter<String>(name = name, type = type) {
    private val mockData = ColorInputType.entries.associateWith { it.value }

    override fun getMockList(): Pair<String, Map<out InputType?, String>> {
        return name to mockData
    }

    override fun getStateCode(name: String): Triple<String, String, String> {
        return generateDropDownCode(name, mockData.values.toList())
    }

    override fun copy(newName: String): XParameter<String> {
        return XColor(newName, type)
    }
}


class XBoolean(override var name: String, override val type: KtTypeReference) :
    XParameter<Boolean>(name = name, type = type) {
    private val mockData = BooleanInputType.entries.associateWith { getBooleanValue(it) }
    override fun getMockList(): Pair<String, Map<out InputType?, Boolean>> {
        return name to mockData
    }

    override fun getStateCode(name: String): Triple<String, String, String> {
        return generateSwitchButtonCode(name)
    }

    override fun copy(newName: String): XParameter<Boolean> {
        return XBoolean(newName, type)
    }
}

class XDataClass(
    override var name: String,
    override val type: KtTypeReference,
    vararg val xParameters: XParameter<*>
) :
    XParameter<List<XParameter<*>>>(name = name, type = type) {


    override fun getMockList(): Pair<String, Map<out InputType, List<XParameter<*>>>> {
        return name to mapOf(DataInputType.instance to xParameters.toList())
    }

    override fun getStateCode(name: String): Triple<String, String, String>? {
        return null
    }

    override fun copy(newName: String): XParameter<List<XParameter<*>>> {
        return XDataClass(newName, type, *xParameters)
    }
}

class XEnumClass(
    override var name: String,
    override val type: KtTypeReference,
    private vararg val declaration: String
) : XParameter<String>(name = name, type = type) {

    override fun getMockList(): Pair<String, Map<out InputType, String>> {
        return name to declaration.toList().associateBy { EnumInputType(it) }
    }

    override fun getStateCode(name: String): Triple<String, String, String> {
        return generateDropDownCode(name, declaration.toList())
    }

    override fun copy(newName: String): XParameter<String> {
        return XEnumClass(newName, type, *declaration)
    }
}


class XObject(
    override var name: String,
    override val type: KtTypeReference,
    val objectName: String,
) : XParameter<String>(name = name, type = type) {

    override fun getMockList(): Pair<String, Map<out ObjectInputType, String>> {
        return name to mapOf(ObjectInputType(objectName) to objectName)
    }

    override fun getStateCode(name: String): Triple<String, String, String> {
        return Triple("", objectName, "")
    }

    override fun copy(newName: String): XParameter<String> {
        return XObject(newName, type, objectName)
    }
}


class XSealed(
    override var name: String,
    override val type: KtTypeReference,
    vararg val xParameters: XParameter<*>
) : XParameter<XParameter<*>>(name = name, type = type) {

    override fun getMockList(): Pair<String, Map<out InputType, XParameter<*>>> {
        return name to xParameters.associateBy { SealedInputType(it.name) }
    }

    override fun getStateCode(name: String): Triple<String, String, String> {
        val typesWithDefaultValue = getDefaultMockDataForSealed(this)
        return Triple(
            "${name}Property", "${name}State", """
        val ${name}PropertyList = listOf(${typesWithDefaultValue.joinToString(",")})
        var ${name}State by remember {
            mutableStateOf(${typesWithDefaultValue.first()})
        }
         val ${name}Property = DropDownProperty(
            "${name}",
            ${name}State,
            ${name}PropertyList
        ) {
           ${name}State=${name}PropertyList[it]
        }
       """
        )
    }

    override fun copy(newName: String): XParameter<XParameter<*>> {
        return XSealed(newName, type, *xParameters)
    }
}

private fun getDefaultMockDataForDataClasses(param: XParameter<*>, prefix: String? = null): String {
    if (param.isPrimitiveOrEnum())
        return "${param.name}= ${
            param.getMockList().second.values.firstOrNull() ?: "null"
        },"
    else if (param is XDataClass) {
        return "${if (prefix == null) param.name.removeLastNullableChar() else param.type.text.removeLastNullableChar()}(${
            param.xParameters.joinToString("\n") {
                getDefaultMockDataForDataClasses(
                    it,
                    param.name
                )
            }
        }),"
    } else {
        return ""
    }
}

fun getDefaultMockDataForSealed(xParameter: XSealed): List<String> {
    return xParameter.getMockList().second.values.mapNotNull { data ->
        when (data) {
            is XDataClass -> {
                getDefaultMockDataForDataClasses(data).removeLastComma()
            }

            is XObject -> {
                data.getMockList().second.values.firstOrNull() ?: "null"
            }

            else -> null
        }
    }

}


class XList(
    override var name: String,
    override val type: KtTypeReference,
    private val xParameter: XParameter<*>
) : XParameter<XParameter<*>>(name = name, type = type) {

    override fun getMockList(): Pair<String, Map<out InputType, XParameter<*>>> {
        return name to mapOf(ListInputType to xParameter)
    }

    override fun getStateCode(name: String): Triple<String, String, String>? {
        return null
    }

    override fun copy(newName: String): XParameter<XParameter<*>> {
        return XSealed(newName, type, xParameter)
    }
}

class XFunction(
    override var name: String,
    override val type: KtTypeReference,
) : XParameter<String?>(name = name, type = type) {

    override fun getMockList(): Pair<String, Map<out InputType, String?>> {
        return name to mapOf(Function to "{}")
    }

    override fun getStateCode(name: String): Triple<String, String, String>? {
        return null
    }

    override fun copy(newName: String): XParameter<String?> {
        return XUnKnown(newName, type)
    }
}

class XUnKnown(
    override var name: String,
    override val type: KtTypeReference,
) : XParameter<String?>(name = name, type = type) {

    override fun getMockList(): Pair<String, Map<out InputType, String?>> {
        return name to mapOf(UnKnown to null)
    }

    override fun getStateCode(name: String): Triple<String, String, String>? {
        return null
    }

    override fun copy(newName: String): XParameter<String?> {
        return XUnKnown(newName, type)
    }
}

fun <T> XParameter<T>?.isPrimitiveOrEnum(): Boolean = this is XInt ||
        this is XDouble ||
        this is XString ||
        this is XFloat ||
        this is XLong ||
        this is XBoolean ||
        this is XColor ||
        this is XObject ||
        this is XFunction ||
        this is XEnumClass

fun KtTypeReference.isPrimitive(): Boolean =
    this.text == "String" || this.text == "String?" || this.text == "Int" || this.text == "Int?" ||
            this.text == "Boolean" || this.text == "Boolean?" || this.text == "Double" || this.text == "Double?" ||
            this.text == "Long" || this.text == "Long?" || this.text == "Float" || this.text == "Float?" || this.text == "Color" || this.text == "Modifier" || this.text == "Throwable" || this.text == "Throwable?"

fun KtTypeReference?.toPrimitiveXParameter(name: String): XParameter<*>? {
    return when (this?.text) {
        "Int" -> XInt(name, this)
        "Int?" -> XInt(name, this)
        "String" -> XString(name, this)
        "String?" -> XString(name, this)
        "Boolean" -> XBoolean(name, this)
        "Boolean?" -> XBoolean(name, this)
        "Double" -> XDouble(name, this)
        "Double?" -> XDouble(name, this)
        "Float" -> XFloat(name, this)
        "Float?" -> XFloat(name, this)
        "Long" -> XLong(name, this)
        "Long?" -> XLong(name, this)
        "Color" -> XColor(name, this)
        "Modifier" -> XObject(name, this, "Modifier")
        "Throwable" -> XObject(name, this, "NullPointerException()")
        "Throwable?" -> XObject(name, this, "NullPointerException()")
        else -> null
    }
}









