package spritz.value.list

import spritz.value.Value

/**
 * @author surge
 * @since 18/03/2023
 */
class ListValue(val elements: MutableList<Value>) : Value("list") {

    override fun asJvmValue() = elements
    override fun toString() = elements.toString()

}