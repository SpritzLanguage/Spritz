package spritz.value.string

import spritz.lexer.token.Token
import spritz.value.Value
import spritz.value.bool.BooleanValue
import spritz.error.Error

/**
 * @author surge
 * @since 18/03/2023
 */
class StringValue(val value: String) : Value("string") {

    override fun asJvmValue() = value

    override fun plus(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        return Pair(StringValue(value + other.toString()), null)
    }

    override fun equality(other: Value, operator: Token<*>): Pair<BooleanValue?, Error?> {
        return if (other is StringValue) {
            Pair(BooleanValue(this.value == other.value), null)
        } else {
            Pair(BooleanValue(false), null)
        }
    }

    override fun inequality(other: Value, operator: Token<*>): Pair<BooleanValue?, Error?> {
        return if (other is StringValue) {
            Pair(BooleanValue(this.value != other.value), null)
        } else {
            Pair(BooleanValue(true), null)
        }
    }

    override fun roughEquality(other: Value, operator: Token<*>): Pair<BooleanValue?, Error?> {
        return if (other is StringValue) {
            Pair(BooleanValue(this.value.equals(other.value, true)), null)
        } else {
            Pair(BooleanValue(false), null)
        }
    }

    override fun roughInequality(other: Value, operator: Token<*>): Pair<BooleanValue?, Error?> {
        return if (other is StringValue) {
            Pair(BooleanValue(!this.value.equals(other.value, true)), null)
        } else {
            Pair(BooleanValue(true), null)
        }
    }
    
    override fun toString() = value
    
}