package spritz.value.string

import spritz.Spritz
import spritz.api.annotations.Excluded
import spritz.api.annotations.Identifier
import spritz.error.Error
import spritz.interpreter.context.Context
import spritz.lexer.token.Token
import spritz.value.PrimitiveReferenceValue
import spritz.value.Value
import spritz.value.bool.BoolValue
import spritz.value.list.ListValue

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

    override fun equality(other: Value, operator: Token<*>): Pair<BoolValue?, Error?> {
        return if (other is StringValue) {
            Pair(BoolValue(this.value == other.value), null)
        } else {
            Pair(BoolValue(false), null)
        }
    }

    override fun inequality(other: Value, operator: Token<*>): Pair<BoolValue?, Error?> {
        return if (other is StringValue) {
            Pair(BoolValue(this.value != other.value), null)
        } else {
            Pair(BoolValue(true), null)
        }
    }

    override fun roughEquality(other: Value, operator: Token<*>): Pair<BoolValue?, Error?> {
        return if (other is StringValue) {
            Pair(BoolValue(this.value.equals(other.value, true)), null)
        } else {
            Pair(BoolValue(false), null)
        }
    }

    override fun roughInequality(other: Value, operator: Token<*>): Pair<BoolValue?, Error?> {
        return if (other is StringValue) {
            Pair(BoolValue(!this.value.equals(other.value, true)), null)
        } else {
            Pair(BoolValue(true), null)
        }
    }

    override fun matchesType(type: Value) = super.matchesType(type) || type is PrimitiveReferenceValue && type.type == "string"
    override fun toString() = value

    class Companion(@Excluded val string: StringValue) {

        @Identifier("char_at")
        fun charAt(index: Int): String {
            return this.string.value[index].toString()
        }

        fun substring(start: Int, end: Int): String {
            return this.string.value.substring(start, end)
        }

        fun length(): Int {
            return this.string.value.length
        }

        @Identifier("is_empty")
        fun isEmpty(): Boolean {
            return this.string.value.isEmpty()
        }

        @Identifier("starts_with")
        fun startsWith(prefix: String): Boolean {
            return this.string.value.startsWith(prefix)
        }

        @Identifier("ends_with")
        fun endsWith(suffix: String): Boolean {
            return this.string.value.endsWith(suffix)
        }

        @Identifier("index_of")
        fun indexOf(string: String): Int {
            return this.string.value.indexOf(string)
        }

        @Identifier("last_index_of")
        fun lastIndexOf(string: String): Int {
            return this.string.value.lastIndexOf(string)
        }

        fun replace(var1: String, var2: String): String {
            return this.string.value.replace(var1, var2)
        }

        fun split(deliminator: String): ListValue {
            return ListValue(this.string.value.split(deliminator).map { StringValue(it) }.toMutableList())
        }

        fun trim(): String {
            return this.string.value.trim()
        }

    }

}