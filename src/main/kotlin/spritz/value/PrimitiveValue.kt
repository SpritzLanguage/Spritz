package spritz.value

/**
 * Represents a primitive value.
 *
 * @author surge
 * @since 18/03/2023
 */
class PrimitiveValue(identifier: String, val test: (Value, Value) -> Boolean) : Value(identifier) {

    init {
        // add this to the companion object's list of primitives
        add(this)
    }

    override fun asJvmValue() = this
    override fun toString() = identifier

    companion object {

        // a list of primitive values present in the current environment.
        private val primitives = mutableListOf<PrimitiveValue>()

        /**
         * Adds a given [primitiveValue] to the list of primitives.
         */
        fun add(primitiveValue: PrimitiveValue) {
            primitives.add(primitiveValue)
        }

        /**
         * Checks if a given [value] matches the [required] value.
         */
        @JvmStatic
        fun check(value: Value, required: Value): Boolean {
            if (required.type == "any") {
                return true
            }

            if (required !is PrimitiveValue && value.type != required.type) {
                return false
            }

            return primitives.any { it.test(value, required) }
        }

    }

}