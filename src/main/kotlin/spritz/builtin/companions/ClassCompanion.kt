package spritz.builtin.companions

import spritz.value.`class`.ClassValue

/**
 * @author surge
 * @since 26/03/2023
 */
class ClassCompanion(value: ClassValue) : Companion(value) {

    @JvmField
    val name = value.identifier

}