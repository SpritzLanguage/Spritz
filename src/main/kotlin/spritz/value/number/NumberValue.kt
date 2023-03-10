package spritz.value.number

import spritz.error.Error
import spritz.error.interpreting.MathsError
import spritz.interpreter.context.Context
import spritz.lexer.position.Position
import spritz.lexer.token.Token
import spritz.value.PrimitiveReferenceValue
import spritz.value.Value
import spritz.value.bool.BoolValue

/**
 * @author surge
 * @since 02/03/2023
 */
open class NumberValue<T : Number>(val value: T, type: String) : Value(type) {

    override fun plus(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        if (other is NumberValue<*>) {
            return Pair(convert(if (shouldConvert(other)) this.value.toFloat() + other.value.toFloat() else this.value.toInt() + other.value.toInt()), null)
        }

        return delegateToIllegal(this, other, operator)
    }

    override fun minus(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        if (other is NumberValue<*>) {
            return Pair(convert(if (shouldConvert(other)) this.value.toFloat() - other.value.toFloat() else this.value.toInt() - other.value.toInt()), null)
        }

        return delegateToIllegal(this, other, operator)
    }

    override fun multiply(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        if (other is NumberValue<*>) {
            return Pair(convert(if (shouldConvert(other)) this.value.toFloat() * other.value.toFloat() else this.value.toInt() * other.value.toInt()), null)
        }

        return delegateToIllegal(this, other, operator)
    }

    override fun divide(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        if (other is NumberValue<*>) {
            if (other.value == 0) {
                return Pair(null, MathsError(
                    "Division by 0",
                    other.start,
                    other.end,
                    this.context
                ))
            }

            return Pair(convert(if (shouldConvert(other)) this.value.toFloat() / other.value.toFloat() else this.value.toInt() / other.value.toInt()), null)
        }

        return delegateToIllegal(this, other, operator)
    }

    override fun modulo(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        if (other is NumberValue<*>) {
            return Pair(convert(if (shouldConvert(other)) this.value.toFloat() % other.value.toFloat() else this.value.toInt() % other.value.toInt()), null)
        }

        return delegateToIllegal(this, other, operator)
    }

    override fun equality(other: Value, operator: Token<*>): Pair<BoolValue?, Error?> {
        if (other is NumberValue<*>) {
            return Pair(BoolValue(if (shouldConvert(other)) this.value.toFloat() == other.value.toFloat() else this.value.toInt() == other.value.toInt()), null)
        }

        return delegateToIllegal(this, other, operator) as Pair<BoolValue?, Error?>
    }

    override fun inequality(other: Value, operator: Token<*>): Pair<BoolValue?, Error?> {
        if (other is NumberValue<*>) {
            return Pair(BoolValue(if (shouldConvert(other)) this.value.toFloat() != other.value.toFloat() else this.value.toInt() != other.value.toInt()), null)
        }

        return delegateToIllegal(this, other, operator) as Pair<BoolValue?, Error?>
    }

    override fun lessThan(other: Value, operator: Token<*>): Pair<BoolValue?, Error?> {
        if (other is NumberValue<*>) {
            return Pair(BoolValue(if (shouldConvert(other)) this.value.toFloat() < other.value.toFloat() else this.value.toInt() < other.value.toInt()), null)
        }

        return delegateToIllegal(this, other, operator) as Pair<BoolValue?, Error?>
    }

    override fun greaterThan(other: Value, operator: Token<*>): Pair<BoolValue?, Error?> {
        if (other is NumberValue<*>) {
            return Pair(BoolValue(if (shouldConvert(other)) this.value.toFloat() > other.value.toFloat() else this.value.toInt() > other.value.toInt()), null)
        }

        return delegateToIllegal(this, other, operator) as Pair<BoolValue?, Error?>
    }

    override fun lessThanOrEqualTo(other: Value, operator: Token<*>): Pair<BoolValue?, Error?> {
        if (other is NumberValue<*>) {
            return Pair(BoolValue(if (shouldConvert(other)) this.value.toFloat() <= other.value.toFloat() else this.value.toInt() <= other.value.toInt()), null)
        }

        return delegateToIllegal(this, other, operator) as Pair<BoolValue?, Error?>
    }

    override fun greaterThanOrEqualTo(other: Value, operator: Token<*>): Pair<BoolValue?, Error?> {
        if (other is NumberValue<*>) {
            return Pair(BoolValue(if (shouldConvert(other)) this.value.toFloat() >= other.value.toFloat() else this.value.toInt() >= other.value.toInt()), null)
        }

        return delegateToIllegal(this, other, operator) as Pair<BoolValue?, Error?>
    }

    override fun toString() = this.value.toString()

    override fun matchesType(type: Value) = super.matchesType(type) || type is PrimitiveReferenceValue && type.type == "number"

    private fun convert(result: Number): NumberValue<*> {
        return if (result is Float) {
            FloatValue(result.toFloat())
        } else {
            IntValue(result.toInt())
        }
    }

    private fun shouldConvert(other: NumberValue<*>): Boolean = other.value is Float

}