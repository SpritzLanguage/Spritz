package spritz.value.task

import spritz.error.Error
import spritz.error.interpreting.CallArgumentMismatchError
import spritz.interpreter.RuntimeResult
import spritz.interpreter.context.Context
import spritz.lexer.position.Position
import spritz.util.RequiredArgument
import spritz.value.Value
import spritz.value.table.Symbol
import spritz.value.table.Table

/**
 * @author surge
 * @since 18/03/2023
 */
abstract class TaskValue(identifier: String, type: String = "task") : Value(type, identifier) {

    abstract override fun asJvmValue(): Any

    protected fun generateContext(): Context {
        val new = Context(this.identifier, this.context, this.start)
        new.givenTable(Table(parent = new.parent?.table))

        return new
    }

    protected fun check(required: List<RequiredArgument>, given: List<Value>, start: Position, end: Position, context: Context): RuntimeResult {
        val result = RuntimeResult()

        if (given.size > required.size) {
            return result.failure(CallArgumentMismatchError(
                "Too many arguments passed when calling '$identifier', expected ${required.size}, got ${given.size}",
                start,
                end,
                context
            ))
        }

        if (given.size < required.size) {
            return result.failure(CallArgumentMismatchError(
                "Too little arguments passed when calling '$identifier', expected ${required.size}, got ${given.size}",
                start,
                end,
                context
            ))
        }

        // TODO: Fix types

        return result.success(null)
    }

    private fun populate(required: List<RequiredArgument>, given: List<Value>, context: Context): Error? {
        given.forEachIndexed { index, passedArgument ->
            val matched = required[index]

            passedArgument.givenContext(context)

            val result = context.table.set(Symbol(matched.name.value as String, passedArgument, passedArgument.start, passedArgument.end).setImmutability(true), context, declaration = true)

            if (result.error != null) {
                return result.error
            }
        }

        return null
    }

    protected fun checkAndPopulate(required: List<RequiredArgument>, given: List<Value>, start: Position, end: Position, context: Context): RuntimeResult {
        val result = RuntimeResult()

        result.register(this.check(required, given, start, end, context))

        if (result.shouldReturn()) {
            return result
        }

        val error = this.populate(required, given, context)

        if (error != null) {
            return result.failure(error)
        }

        return result.success(null)
    }

    override fun toString() = "($type: $identifier)"

}