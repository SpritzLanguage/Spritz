package spritz.value.number

import spritz.SpritzEnvironment
import spritz.builtin.companions.NumberCompanion
import spritz.lexer.token.Token
import spritz.value.Value
import spritz.error.Error
import spritz.interpreter.context.Context

/**
 * @author surge
 * @since 03/03/2023
 */
class ByteValue(value: Byte) : NumberValue<Byte>(value, "byte") {

    init {
        SpritzEnvironment.putIntoTable(NumberCompanion(this), this.table, Context("companion"))
    }

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