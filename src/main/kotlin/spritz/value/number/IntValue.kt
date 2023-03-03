package spritz.value.number

import spritz.value.PrimitiveReferenceValue
import spritz.value.Value

/**
 * @author surge
 * @since 02/03/2023
 */
class IntValue(value: Int) : NumberValue<Int>(value, "int") {

    override fun matchesType(type: Value) = super.matchesType(type) || type is PrimitiveReferenceValue && type.type == "int"

}