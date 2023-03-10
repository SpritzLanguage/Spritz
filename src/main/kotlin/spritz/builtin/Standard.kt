package spritz.builtin

import spritz.api.annotations.Identifier
import spritz.api.result.Result
import spritz.api.result.Success
import spritz.value.Value
import spritz.value.list.ListValue
import spritz.value.number.FloatValue
import spritz.value.number.IntValue
import spritz.value.number.NumberValue

/**
 * @author surge
 * @since 04/03/2023
 */
object Standard {

    fun print(input: Value) {
        kotlin.io.print(input)
    }

    fun println(input: Value) {
        kotlin.io.println(input)
    }

    fun readln(): String {
        return kotlin.io.readln()
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

}