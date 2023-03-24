package spritz.builtin.companions

import spritz.api.annotations.Excluded
import spritz.api.annotations.Identifier
import spritz.value.NullValue
import spritz.value.Value
import spritz.value.dictionary.DictionaryValue
import spritz.value.list.ListValue
import spritz.value.string.StringValue

/**
 * @author surge
 * @since 20/03/2023
 */
class ListCompanion(@Excluded val list: ListValue) {

    fun get(index: Int): Value {
        return list.elements[index]
    }

    fun remove(value: Value): Boolean {
        return list.elements.remove(value)
    }

    fun removeAt(index: Int): Value {
        return list.elements.removeAt(index)
    }

    fun length(): Int {
        return list.elements.size
    }

    fun after(index: Int): List<Value> {
        val elements = mutableListOf<Value>()

        for (i in index until this.list.elements.size) {
            elements.add(this.list.elements[i])
        }

        return elements
    }

    @Identifier("is_empty")
    fun isEmpty(): Boolean {
        return this.list.elements.isEmpty()
    }

    fun join(separator: String): String {
        var concat = ""

        this.list.elements.forEach {
            concat += it.toString() + separator
        }

        concat.removeSuffix(separator)

        return concat
    }

}