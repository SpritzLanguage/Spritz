package spritz.builtin.companions

import spritz.api.annotations.Identifier
import spritz.value.string.StringValue

/**
 * @author surge
 * @since 20/03/2023
 */
class StringCompanion(value: StringValue) : Companion(value) {

    fun length(): Int {
        return (this.value as StringValue).value.length
    }

    @Identifier("is_empty")
    fun isEmpty(): Boolean {
        return (this.value as StringValue).value.isEmpty()
    }

    @Identifier("char_at")
    fun charAt(index: Int): String {
        return (this.value as StringValue).value[index].toString()
    }

    fun replace(old: String, new: String): String {
        return (this.value as StringValue).value.replace(old, new)
    }

    fun upper(): String {
        return (this.value as StringValue).value.uppercase()
    }

    fun lower(): String {
        return (this.value as StringValue).value.lowercase()
    }

    @Identifier("to_char_list")
    fun toCharList(): List<String> {
        return (this.value as StringValue).value.map { it.toString() }
    }

    fun split(delimiter: String): List<String> {
        return (this.value as StringValue).value.split(delimiter)
    }

    fun after(index: Int): String {
        var result = ""

        for (i in index until (this.value as StringValue).value.length) {
            result += this.value.value[i]
        }

        return result
    }

    fun int(): Int {
        return (this.value as StringValue).value.toInt()
    }

    fun float(): Float {
        return (this.value as StringValue).value.toFloat()
    }

    fun byte(): Byte {
        return (this.value as StringValue).value.toByte()
    }

    fun boolean(): Boolean {
        return (this.value as StringValue).value.toBooleanStrict()
    }

}