package spritz.value

/**
 * @author surge
 * @since 18/03/2023
 */
class PrimitiveValue(identifier: String, val test: (Value) -> Boolean) : Value(identifier) {

    init {
        add(this)
    }

    override fun asJvmValue() = this
    override fun toString() = "(PRIMITIVE $identifier)"

    companion object {

        val primitives = mutableListOf<PrimitiveValue>()

        fun add(primitiveValue: PrimitiveValue) {
            primitives.add(primitiveValue);
        }

        @JvmStatic
        fun check(value: Value): Boolean {
            return primitives.any { it.test(value) }
        }

    }

}