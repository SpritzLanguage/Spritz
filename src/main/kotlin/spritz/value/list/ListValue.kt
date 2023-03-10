package spritz.value.list

import spritz.Spritz
import spritz.api.annotations.Excluded
import spritz.api.annotations.Identifier
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

        @Identifier("remove_value")
        fun removeValue(value: Value) {
            this.list.elements.remove(value)
        }

        fun remove(index: Int) {
            this.list.elements.removeAt(index)
        }

        fun size(): Int = this.list.elements.size

        @Identifier("is_empty")
        fun isEmpty(): Boolean = this.list.elements.isEmpty()

        @Identifier("is_not_empty")
        fun isNotEmpty(): Boolean = this.list.elements.isNotEmpty()

        fun clear() {
            this.list.elements.clear()
        }

        @Identifier("index_of")
        fun indexOf(value: Value): Int {
            return this.list.elements.indexOf(value)
        }

    }

}