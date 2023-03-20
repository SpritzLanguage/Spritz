package spritz.value.bool

import spritz.error.Error
import spritz.lexer.token.Token
import spritz.value.Value

/**
 * @author surge
 * @since 18/03/2023
 */
class BooleanValue(var value: Boolean) : Value("boolean") {

    override fun asJvmValue() = value

    override fun and(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        if (other is BooleanValue) {
            return Pair(BooleanValue(this.value && other.value), null)
        }

        return super.and(other, operator)
    }

    override fun or(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        if (other is BooleanValue) {
            return Pair(BooleanValue(this.value || other.value), null)
        }

        return super.and(other, operator)
    }

    override fun negated(token: Token<*>): Pair<Value?, Error?> {
        return Pair(BooleanValue(!this.value), null)
    }

    override fun toString() = value.toString()

    override fun equals(other: Any?): Boolean {
        if (other !is BooleanValue) {
            return false
        }

        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

}