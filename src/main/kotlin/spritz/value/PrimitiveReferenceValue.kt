package spritz.value

/**
 * @author surge
 * @since 03/03/2023
 */
class PrimitiveReferenceValue(type: String) : Value(type) {

    override fun toString() = "($type)"

}