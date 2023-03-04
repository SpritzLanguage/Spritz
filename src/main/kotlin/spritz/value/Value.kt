package spritz.value

import spritz.error.Error
import spritz.error.interpreting.IllegalOperationError
import spritz.interfaces.Cloneable
import spritz.interpreter.RuntimeResult
import spritz.interpreter.context.Context
import spritz.lexer.position.LinkPosition
import spritz.lexer.position.Position
import spritz.lexer.token.Token
import spritz.value.bool.BoolValue
import spritz.value.symbols.Table

/**
 * @author surge
 * @since 01/03/2023
 */
abstract class Value(val type: String, val identifier: String = type) : Cloneable {

    lateinit var start: Position
    lateinit var end: Position
    lateinit var context: Context

    var table = Table()

    open fun and(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)
    open fun or(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)

    open fun plus(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)
    open fun minus(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)
    open fun multiply(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)
    open fun divide(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)
    open fun modulo(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)

    // binary shifts
    open fun binShl(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)
    open fun binShr(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)
    open fun binUShr(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)
    open fun binOr(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)
    open fun binAnd(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)
    open fun binXor(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)
    open fun binComplement(operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, this, operator)

    open fun equality(other: Value, operator: Token<*>): Pair<BoolValue?, Error?> = Pair(BoolValue(this == other), null)
    open fun inequality(other: Value, operator: Token<*>): Pair<Value?, Error?> = Pair(BoolValue(this != other), null)
    open fun lessThan(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)
    open fun greaterThan(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)
    open fun lessThanOrEqualTo(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)
    open fun greaterThanOrEqualTo(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)

    open fun negated(token: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, this, token)

    fun execute(passed: List<Value>): RuntimeResult = execute(passed, LinkPosition(), LinkPosition(), this.context)
    open fun execute(passed: List<Value>, start: Position, end: Position, context: Context): RuntimeResult = RuntimeResult().failure(IllegalOperationError("Couldn't execute '$this'", start, end, this.context))

    open fun matchesType(type: Value): Boolean = type is PrimitiveReferenceValue && type.type == "any"

    override fun clone(): Value {
        return this
    }

    abstract override fun toString(): String

    fun positioned(start: Position, end: Position): Value {
        this.start = start
        this.end = end

        return this
    }

    fun givenContext(context: Context): Value {
        this.context = context

        return this
    }

    companion object {
        fun delegateToIllegal(value: Value, other: Value, operator: Token<*>): Pair<Value?, IllegalOperationError> {
            return Pair(
                null,
                IllegalOperationError(
                    "Illegal operation on value of type '${value.type}': '$operator' and '$other'",
                    operator.start,
                    other.end,
                    other.context
                )
            )
        }
    }

}