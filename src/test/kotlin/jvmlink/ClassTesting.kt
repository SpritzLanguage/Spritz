package jvmlink

import spritz.api.Coercion
import spritz.api.annotations.Identifier
import spritz.value.Value

/**
 * @author surge
 * @since 18/03/2023
 */
class ClassTesting {

    @Identifier("test")
    fun test(value: String) {
        println(value)
    }

    @Identifier("boolean_setting")
    fun registerBoolean(name: String, description: String, default: Boolean): Value {
        return Coercion.IntoSpritz.coerce(default)
    }

}