package jvmlink

/**
 * @author surge
 * @since 27/03/2023
 */
class ClassWithEnum(val test: TestEnum) {

    enum class TestEnum(@JvmField val a: Int) {
        MEMBER_ONE(2),
        MEMBER_TWO(5);

        fun test() {
            println(a)
        }
    }

}