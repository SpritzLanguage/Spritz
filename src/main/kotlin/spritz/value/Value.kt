package spritz.value

import spritz.builtin.Global
import spritz.error.Error
import spritz.error.interpreting.IllegalOperationError
import spritz.interfaces.Cloneable
import spritz.interpreter.RuntimeResult
import spritz.interpreter.context.Context
import spritz.lexer.position.LinkPosition
import spritz.lexer.position.Position
import spritz.lexer.token.Token
import spritz.value.bool.BooleanValue
import spritz.value.table.Table
import spritz.value.table.TableAccessor
import spritz.value.task.DefinedTaskValue

/**
 * The main variable class.
 *
 * @author surge
 * @since 01/03/2023
 */
abstract class Value(val type: String, val identifier: String = type) : Cloneable {

    // the start, end, and context of this value
    lateinit var start: Position
    lateinit var end: Position
    lateinit var context: Context

    // a value table of all values that are contained within this value, or it's companion
    var table = Table()

    /**
     * @return This value, as it's JVM representation.
     */
    abstract fun asJvmValue(): Any?

    /**
     * Attempts to check if this value and the given [other] value is true.
     * @return A pair of the resulting value and the error.
     */
    open fun and(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)

    /**
     * Attempts to check if this value or the given [other] value is true.
     * @return A pair of the resulting value and the error.
     */
    open fun or(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)

    /**
     * Attempts to add this value and the given [other] value.
     * @return A pair of the resulting value and the error.
     */
    open fun plus(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)

    /**
     * Attempts to subtract this value from the given [other] value.
     * @return A pair of the resulting value and the error.
     */
    open fun minus(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)

    /**
     * Attempts to multiply this value by the given [other] value.
     * @return A pair of the resulting value and the error.
     */
    open fun multiply(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)

    /**
     * Attempts to divide this value by the given [other] value.
     * @return A pair of the resulting value and the error.
     */
    open fun divide(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)

    /**
     * Attempts to modulo this value by the given [other] value.
     * @return A pair of the resulting value and the error.
     */
    open fun modulo(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)

    /**
     * Attempts to left shift this value by the given [other] value.
     * @return A pair of the resulting value and the error.
     */
    open fun binShl(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)

    /**
     * Attempts to right shift this value by the given [other] value.
     * @return A pair of the resulting value and the error.
     */
    open fun binShr(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)

    /**
     * Attempts to (unsigned) right shift this value by the given [other] value.
     * @return A pair of the resulting value and the error.
     */
    open fun binUShr(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)

    /**
     * Attempts to binary or this value by the given [other] value.
     * @return A pair of the resulting value and the error.
     */
    open fun binOr(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)

    /**
     * Attempts to binary and this value by the given [other] value.
     * @return A pair of the resulting value and the error.
     */
    open fun binAnd(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)

    /**
     * Attempts to binary xor this value by the given [other] value.
     * @return A pair of the resulting value and the error.
     */
    open fun binXor(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)

    /**
     * Attempts to binary invert this value.
     * @return A pair of the resulting value and the error.
     */
    open fun binComplement(operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, this, operator)

    /**
     * Attempts to check if this value and the given [other] value are equal.
     * @return A pair of the resulting value and the error.
     */
    open fun equality(other: Value, operator: Token<*>): Pair<BooleanValue?, Error?> = Pair(BooleanValue(this == other), null)

    /**
     * Attempts to check if this value and the given [other] value are not equal.
     * @return A pair of the resulting value and the error.
     */
    open fun inequality(other: Value, operator: Token<*>): Pair<Value?, Error?> = Pair(BooleanValue(this != other), null)

    /**
     * Attempts to check if this value and the given [other] value are nearly equal.
     * @return A pair of the resulting value and the error.
     */
    open fun roughEquality(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)

    /**
     * Attempts to check if this value and the given [other] value are not nearly equal.
     * @return A pair of the resulting value and the error.
     */
    open fun roughInequality(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)

    /**
     * Attempts to check if this value is less than the given [other] value.
     * @return A pair of the resulting value and the error.
     */
    open fun lessThan(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)

    /**
     * Attempts to check if this value is greater than the given [other] value.
     * @return A pair of the resulting value and the error.
     */
    open fun greaterThan(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)

    /**
     * Attempts to check if this value is less than or equal to the given [other] value.
     * @return A pair of the resulting value and the error.
     */
    open fun lessThanOrEqualTo(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)

    /**
     * Attempts to check if this value is greater than or equal to the given [other] value.
     * @return A pair of the resulting value and the error.
     */
    open fun greaterThanOrEqualTo(other: Value, operator: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, other, operator)

    /**
     * Attempts to get the opposite of this value.
     * @return A pair of the resulting value and the error.
     */
    open fun negated(token: Token<*>): Pair<Value?, Error?> = delegateToIllegal(this, this, token)

    /**
     * Attempts to execute this value, with the given [passed] arguments.
     * @return The result of executing this value.
     */
    fun execute(passed: List<Value>): RuntimeResult = execute(passed, LinkPosition(), LinkPosition(), this.context)

    /**
     * Attempts to execute this value, with the given [passed] arguments, and setting the resulting value's [start], [end], and [context].
     * @return The result of executing this value.
     */
    open fun execute(passed: List<Value>, start: Position, end: Position, context: Context): RuntimeResult = RuntimeResult().failure(IllegalOperationError("Couldn't execute '$this'", start, end, this.context))

    /**
     * @return A cloned value.
     */
    override fun clone(): Value {
        return this
    }

    /**
     * Checks if this value's [table] contains a <code>task</code> that returns <code>int</code> and has 0 parameters, and executes it.
     * @return The result of executing <code>task<int> repr</code>, or '' if it is not found.
     */
    override fun toString(): String {
        return TableAccessor(this.table)

            // called "repr"
            .identifier("repr")

            // is a task, returns string, and has no arguments
            .predicate { it is DefinedTaskValue && it.returnType == Global.string && it.arguments.isEmpty() }

            // find and execute
            .find(this.start, this.end, this.context).value?.execute(arrayListOf())?.value?.toString() ?: ""
    }

    /**
     * Sets the [start] and [end] positions of this value.
     * @return This value.
     */
    fun position(start: Position, end: Position): Value {
        this.start = start
        this.end = end

        return this
    }

    /**
     * Sets the [context] of this value.
     * @return This value.
     */
    fun givenContext(context: Context): Value {
        this.context = context

        return this
    }

    /**
     * Sets the [start], [end], and [context] of this value to what we would assume as linked positions.
     * @return This value.
     */
    fun linked(): Value {
        return position(LinkPosition(), LinkPosition()).givenContext(Context(this.identifier))
    }

    companion object {

        /**
         * @return A pair of null and an [IllegalOperationError].
         */
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