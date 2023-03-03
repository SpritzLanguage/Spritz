package spritz.value.number

import spritz.error.Error
import spritz.lexer.token.Token
import spritz.value.Value

/**
 * @author surge
 * @since 03/03/2023
 */
class ByteValue(value: Byte) : NumberValue<Byte>(value, "byte") {

    override fun plus(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        if (other is FloatValue) {
            return delegateToIllegal(this, other, operator)
        }

        return super.plus(other, operator)
    }

    override fun minus(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        if (other is FloatValue) {
            return delegateToIllegal(this, other, operator)
        }

        return super.minus(other, operator)
    }

    override fun multiply(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        if (other is FloatValue) {
            return delegateToIllegal(this, other, operator)
        }

        return super.multiply(other, operator)
    }

    override fun divide(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        if (other is FloatValue) {
            return delegateToIllegal(this, other, operator)
        }

        return super.divide(other, operator)
    }

    override fun toString() = value.toString()

}