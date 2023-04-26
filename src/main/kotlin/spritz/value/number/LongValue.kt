package spritz.value.number

import spritz.SpritzEnvironment
import spritz.builtin.companions.NumberCompanion
import spritz.lexer.token.Token
import spritz.value.Value
import spritz.error.Error
import spritz.interpreter.context.Context

/**
 * @author surge
 * @since 02/03/2023
 */
class LongValue(value: Long) : NumberValue<Long>(value, "long") {

    init {
        SpritzEnvironment.putIntoTable(NumberCompanion(this), this.table, Context("companion"))
    }

    override fun binShl(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        if (other is IntValue) {
            return Pair(LongValue(this.value shl other.value), null)
        }

        return super.binShl(other, operator)
    }

    override fun binShr(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        if (other is IntValue) {
            return Pair(LongValue(this.value shr other.value), null)
        }

        return super.binShr(other, operator)
    }

    override fun binUShr(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        if (other is IntValue) {
            return Pair(LongValue(this.value ushr other.value), null)
        }

        return super.binUShr(other, operator)
    }

    override fun binOr(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        if (other is LongValue) {
            return Pair(LongValue(this.value or other.value), null)
        }

        return super.binOr(other, operator)
    }

    override fun binAnd(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        if (other is LongValue) {
            return Pair(LongValue(this.value and other.value), null)
        }

        return super.binAnd(other, operator)
    }

    override fun binXor(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        if (other is LongValue) {
            return Pair(LongValue(this.value xor other.value), null)
        }

        return super.binXor(other, operator)
    }

    override fun binComplement(operator: Token<*>): Pair<Value?, Error?> {
        return Pair(LongValue(this.value.toString(2).toLong().inv()), null)
    }

}