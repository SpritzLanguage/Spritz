package spritz.value

/**
 * @author surge
 * @since 18/03/2023
 */
class NullValue : Value("null") {

    override fun asJvmValue() = null
    override fun toString() = "null"

}