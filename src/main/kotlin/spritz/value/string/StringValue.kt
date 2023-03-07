package spritz.value.string

import spritz.Spritz
import spritz.api.annotations.Excluded
import spritz.error.Error
import spritz.interpreter.context.Context
import spritz.lexer.token.Token
import spritz.value.PrimitiveReferenceValue
import spritz.value.Value
import spritz.value.number.IntValue

/**
 * @author surge
 * @since 03/03/2023
 */
class StringValue(val value: String) : Value("string") {

    init {
        Spritz.loadInto(Companion(this), table, Context("string"))
    }

    override fun plus(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        return Pair(StringValue(value + other.toString()), null)
    }

    override fun matchesType(type: Value) = super.matchesType(type) || type is PrimitiveReferenceValue && type.type == "string"
    override fun toString() = value

    class Companion(@Excluded val string: StringValue) {

        fun get(index: Int): String {
            return this.string.value[index].toString()
        }

        fun substring(start: Int, end: Int): String {
            return this.string.value.substring(start, end)
        }

        fun size(): Int {
            return this.string.value.length
        }

    }

}