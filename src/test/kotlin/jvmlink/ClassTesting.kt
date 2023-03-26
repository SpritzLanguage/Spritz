package jvmlink

/**
 * @author surge
 * @since 18/03/2023
 */
class ClassTesting(val a: Int) {

    fun test() {
        println(a)
    }

    companion object {

        @JvmStatic
        fun test2() {
            println("!!!!!!!!!!!!")
        }

    }

}