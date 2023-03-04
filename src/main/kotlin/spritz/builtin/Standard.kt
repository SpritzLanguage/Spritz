package spritz.builtin

import spritz.value.Value

/**
 * @author surge
 * @since 04/03/2023
 */
object Standard {

    @JvmField
    val a = 55427

    fun print(input: Value) {
        kotlin.io.print(input)
    }

    fun println(input: Value) {
        kotlin.io.println(input)
    }

    fun readln(): String {
        return kotlin.io.readln()
    }

}