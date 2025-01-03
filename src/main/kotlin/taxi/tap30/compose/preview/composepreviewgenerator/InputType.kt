package taxi.tap30.compose.preview.composepreviewgenerator

interface InputType

class DataInputType : InputType {
    companion object {
        val instance = DataInputType()
    }
}


object Function : InputType
object UnKnown : InputType
object ListInputType : InputType


class ObjectInputType(val name: String) : InputType {
    override fun toString(): String {
        return name
    }
}

class SealedInputType(val name: String) : InputType {
    override fun toString(): String {
        return name
    }
}

class EnumInputType(val name: String) : InputType {
    override fun toString(): String {
        return name
    }
}

enum class ColorInputType(val value: String) : InputType {
    red("Color.Red"), blue("Color.Blue"), black("Color.Black") , white("Color.White");

    companion object {
        fun getValueByName(value: String?): ColorInputType {
            return entries.firstOrNull { it.name == value } ?: black
        }

    }
}

enum class NumberInputType : InputType {
    low, mid, high;

    companion object {
        fun getValueByName(value: String?): NumberInputType {
            return entries.firstOrNull { it.name == value } ?: low
        }

    }
}

enum class StringInputType : InputType {
    short, medium, long;

    companion object {
        fun getValueByName(value: String?): StringInputType {
            return entries.firstOrNull { it.name == value } ?: short
        }

    }
}


enum class BooleanInputType : InputType {
    TRUE, FALSE;

    companion object {
        fun getValueByName(value: String?): BooleanInputType {
            return entries.firstOrNull { it.name == value } ?: FALSE
        }

    }
}