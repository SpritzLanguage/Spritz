package spritz.builtin

import spritz.api.StandardOverride
import spritz.api.annotations.Excluded
import spritz.api.annotations.Identifier
import spritz.api.result.Result
import spritz.api.result.Success
import spritz.value.Value
import spritz.value.list.ListValue
import spritz.value.number.FloatValue
import spritz.value.number.IntValue
import spritz.value.string.StringValue

/**
 * @author surge
 * @since 04/03/2023
 */
object Standard {

    fun print(input: Value) {
        StandardOverride.output(input.toString())
    }

    fun printf(input: StringValue, formatted: Value) {
        StandardOverride.output(format(input.value, if (formatted is ListValue) formatted.elements else listOf(formatted)))
    }

    fun println(input: Value) {
        StandardOverride.output(input.toString() + System.lineSeparator())
    }

    fun printlnf(input: StringValue, formatted: Value) {
        StandardOverride.output(format(input.value, if (formatted is ListValue) formatted.elements else listOf(formatted)) + System.lineSeparator())
    }

    fun readln(): String {
        return StandardOverride.input()
    }

    @Identifier("int_range")
    fun intRange(start: IntValue, end: IntValue, step: IntValue): Result {
        val elements = mutableListOf<IntValue>()

        if (start.value > end.value) {
            var i = start.value

            while (i > end.value) {
                elements.add(IntValue((i)))
                i -= step.value
            }
        } else {
            var i = start.value

            while (i < end.value) {
                elements.add(IntValue((i)))
                i += step.value
            }
        }

        return Success(ListValue(elements.toMutableList()))
    }

    @Identifier("float_range")
    fun floatRange(start: FloatValue, end: FloatValue, step: FloatValue): Result {
        val elements = mutableListOf<FloatValue>()

        if (start.value > end.value) {
            var i = start.value

            while (i > end.value) {
                elements.add(FloatValue((i)))
                i -= step.value
            }
        } else {
            var i = start.value

            while (i < end.value) {
                elements.add(FloatValue((i)))
                i += step.value
            }
        }

        return Success(ListValue(elements.toMutableList()))
    }

    @Identifier("exit_process")
    fun exitProcess(status: Int) {
        StandardOverride.exit(status)
    }

    // utility functions
    @Excluded
    private fun format(input: String, formatted: List<Value>): String {
        var result = ""

        var formatIndex = 0

        input.forEachIndexed { index, char ->
            if (char == '%') {
                if (formatIndex <= formatted.lastIndex) {
                    result += formatted[formatIndex].toString()
                    formatIndex++
                } else {
                    result += '%'
                }
            } else {
                result += char
            }
        }

        return result
    }

}