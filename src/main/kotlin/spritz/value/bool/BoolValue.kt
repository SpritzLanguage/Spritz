package spritz.value.bool

import spritz.value.PrimitiveReferenceValue
import spritz.value.Value

/**
 * @author surge
 * @since 03/03/2023
 */
class BoolValue(val value: Boolean) : Value("bool") {

    override fun matchesType(type: Value) = super.matchesType(type) || type is PrimitiveReferenceValue && type.type == "bool"
    override fun toString() = value.toString()

    override fun equals(other: Any?): Boolean {
        if (other !is BoolValue) {
            return false
        }

        return this.value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

}