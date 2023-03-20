package spritz.value.number

import spritz.lexer.token.Token
import spritz.value.Value
import spritz.error.Error

/**
 * @author surge
 * @since 02/03/2023
 */
class IntValue(value: Int) : NumberValue<Int>(value, "int") {

    override fun binShl(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        if (other is IntValue) {
            return Pair(IntValue(this.value shl other.value), null)
        }

        return super.binShl(other, operator)
    }

    override fun binShr(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        if (other is IntValue) {
            return Pair(IntValue(this.value shr other.value), null)
        }

        return super.binShr(other, operator)
    }

    override fun binUShr(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        if (other is IntValue) {
            return Pair(IntValue(this.value ushr other.value), null)
        }

        return super.binUShr(other, operator)
    }

    override fun binOr(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        if (other is IntValue) {
            return Pair(IntValue(this.value or other.value), null)
        }

        return super.binOr(other, operator)
    }

    override fun binAnd(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        if (other is IntValue) {
            return Pair(IntValue(this.value and other.value), null)
        }

        return super.binAnd(other, operator)
    }

    override fun binXor(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        if (other is IntValue) {
            return Pair(IntValue(this.value xor other.value), null)
        }

        return super.binXor(other, operator)
    }

    override fun binComplement(operator: Token<*>): Pair<Value?, Error?> {
        return Pair(IntValue(this.value.toString(2).toInt().inv()), null)
    }

}