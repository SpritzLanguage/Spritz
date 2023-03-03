package spritz.value.list

import spritz.value.PrimitiveReferenceValue
import spritz.value.Value

/**
 * @author surge
 * @since 02/03/2023
 */
class ListValue(val elements: MutableList<Value>) : Value("list") {

    override fun matchesType(type: Value) = super.matchesType(type) || type is PrimitiveReferenceValue && type.type == "list"

    override fun toString() = elements.toString()

}