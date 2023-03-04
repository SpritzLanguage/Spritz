package spritz.value.list

import spritz.Spritz
import spritz.api.annotations.Excluded
import spritz.interpreter.context.Context
import spritz.value.PrimitiveReferenceValue
import spritz.value.Value
import spritz.value.number.IntValue

/**
 * @author surge
 * @since 02/03/2023
 */
class ListValue(val elements: MutableList<Value>) : Value("list") {

    init {
        Spritz.loadInto(Companion(this), table, Context("list"))
    }

    override fun matchesType(type: Value) = super.matchesType(type) || type is PrimitiveReferenceValue && type.type == "list"

    override fun toString() = elements.toString()

    class Companion(@Excluded val list: ListValue) {

        fun add(value: Value): Value {
            this.list.elements.add(value)
            return value
        }

        fun remove(index: Int) {
            this.list.elements.removeAt(index)
        }

        fun size(): Int {
            return this.list.elements.size
        }

    }

}