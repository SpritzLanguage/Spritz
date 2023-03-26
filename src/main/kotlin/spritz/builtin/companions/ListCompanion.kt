package spritz.builtin.companions

import spritz.api.annotations.Identifier
import spritz.value.Value
import spritz.value.list.ListValue

/**
 * @author surge
 * @since 20/03/2023
 */
class ListCompanion(value: ListValue) : Companion(value) {

    fun add(value: Value) {
        (this.value as ListValue).elements.add(value)
    }

    fun get(index: Int): Value {
        return (this.value as ListValue).elements[index]
    }

    fun remove(value: Value): Boolean {
        return (this.value as ListValue).elements.remove(value)
    }

    fun removeAt(index: Int): Value {
        return (this.value as ListValue).elements.removeAt(index)
    }

    fun length(): Int {
        return (this.value as ListValue).elements.size
    }

    fun after(index: Int): List<Value> {
        val elements = mutableListOf<Value>()

        for (i in index until (this.value as ListValue).elements.size) {
            elements.add(this.value.elements[i])
        }

        return elements
    }

    @Identifier("is_empty")
    fun isEmpty(): Boolean {
        return (this.value as ListValue).elements.isEmpty()
    }

    fun join(separator: String, format: String): String {
        var concat = ""

        (this.value as ListValue).elements.forEachIndexed { index, value ->
            concat += format.replace("%", value.toString()) + if (index != this.value.elements.lastIndex) separator else ""
        }

        return concat
    }

}