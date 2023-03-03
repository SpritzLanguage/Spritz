package spritz.value

import spritz.error.Error
import spritz.error.interpreting.IllegalOperationError
import spritz.interpreter.context.Context
import spritz.lexer.position.Position
import spritz.lexer.token.Token

/**
 * @author surge
 * @since 01/03/2023
 */
abstract class Value(val type: String, val identifier: String = type) {

    lateinit var start: Position
    lateinit var end: Position
    lateinit var context: Context

    // TODO: Symbol table

    open fun and(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)
    open fun or(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)

    open fun plus(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)
    open fun minus(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)
    open fun multiply(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)
    open fun divide(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)
    open fun modulo(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)

    open fun equality(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)
    open fun inequality(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)
    open fun lessThan(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)
    open fun greaterThan(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)
    open fun lessThanOrEqualTo(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)
    open fun greaterThanOrEqualTo(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)

    open fun negated(token: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, this, token)

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
                    value.context
                )
            )
        }
    }

}