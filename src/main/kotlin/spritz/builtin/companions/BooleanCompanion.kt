package spritz.builtin.companions

import spritz.value.bool.BooleanValue

/**
 * @author surge
 * @since 26/03/2023
 */
class BooleanCompanion(value: BooleanValue) : Companion(value) {

    fun binary() = if ((value as BooleanValue).value) 1 else 0

}