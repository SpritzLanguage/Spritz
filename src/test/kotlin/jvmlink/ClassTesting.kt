package jvmlink

import spritz.api.annotations.Identifier

/**
 * @author surge
 * @since 18/03/2023
 */
class ClassTesting(val a: Int) {

    @Identifier("test")
    fun test(value: String) {
        println(value)
    }

    @Identifier("test2")
    fun test2(value: String): String {
        println(value + "2")
        return "testdfgds"
    }

}