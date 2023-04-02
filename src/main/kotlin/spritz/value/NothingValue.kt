package spritz.value

/**
 * @author surge
 * @since 02/04/2023
 */
class NothingValue : Value("nothing") {

    override fun asJvmValue() = null
    override fun toString() = "nothing"

}