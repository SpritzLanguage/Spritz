package spritz.builtin.companions

import spritz.api.annotations.Excluded
import spritz.api.annotations.Identifier
import spritz.value.list.ListValue
import spritz.value.string.StringValue

/**
 * @author surge
 * @since 20/03/2023
 */
class StringCompanion(@Excluded val string: StringValue) {

    fun length(): Int {
        return string.value.length
    }

    @Identifier("is_empty")
    fun isEmpty(): Boolean {
        return string.value.isEmpty()
    }

    @Identifier("char_at")
    fun charAt(index: Int): String {
        return string.value[index].toString()
    }

    fun replace(old: String, new: String): String {
        return string.value.replace(old, new)
    }

    fun upper(): String {
        return string.value.uppercase()
    }

    fun lower(): String {
        return string.value.lowercase()
    }

    @Identifier("to_char_list")
    fun toCharList(): List<String> {
        return string.value.map { it.toString() }
    }

    fun split(delimiter: String): List<String> {
        return string.value.split(delimiter)
    }

}