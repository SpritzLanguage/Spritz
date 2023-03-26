package spritz.builtin.companions

import spritz.api.annotations.Identifier
import spritz.value.NullValue
import spritz.value.Value
import spritz.value.`class`.DefinedInstanceValue
import spritz.value.`class`.InstanceValue

/**
 * @author surge
 * @since 26/03/2023
 */
class InstanceCompanion(value: InstanceValue) : Companion(value) {

    @Identifier("get_parent")
    fun getParent(): Value {
        return if (value is DefinedInstanceValue) {
            value.parent
        } else {
            NullValue()
        }
    }

}