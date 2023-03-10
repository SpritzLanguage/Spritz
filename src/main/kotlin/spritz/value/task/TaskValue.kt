package spritz.value.task

import spritz.Spritz
import spritz.api.annotations.Excluded
import spritz.api.annotations.Identifier
import spritz.error.Error
import spritz.error.interpreting.CallArgumentMismatchError
import spritz.error.interpreting.TypeMismatchError
import spritz.interpreter.RuntimeResult
import spritz.interpreter.context.Context
import spritz.lexer.position.Position
import spritz.util.RequiredArgument
import spritz.value.PrimitiveReferenceValue
import spritz.value.Value
import spritz.value.string.StringValue
import spritz.value.symbols.Symbol
import spritz.value.symbols.SymbolData
import spritz.value.symbols.Table

/**
 * @author surge
 * @since 03/03/2023
 */
open class TaskValue(identifier: String, type: String) : Value(type, identifier = identifier) {

    protected fun load() {
        Spritz.loadInto(Companion(this), table, Context("task"))
    }

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

        required.forEachIndexed { index, argument ->
            if (!given[index].matchesType(argument.type ?: PrimitiveReferenceValue("any"))) {
                return result.failure(TypeMismatchError(
                    "'${given[index]}' did not conform to type '${argument.type}'",
                    start,
                    end,
                    context
                ))
            }
        }

        return result.success(null)
    }

    private fun populate(required: List<RequiredArgument>, given: List<Value>, context: Context): Error? {
        given.forEachIndexed { index, passedArgument ->
            val matched = required[index]

            passedArgument.givenContext(context)

            val result = context.table.set(Symbol(matched.name.value as String, passedArgument, SymbolData(immutable = true, matched.name.start, matched.name.end)), context, declaration = true)

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

    override fun matchesType(type: Value) = super.matchesType(type) || type is PrimitiveReferenceValue && type.type == "method"

    override fun toString() = "($type: $identifier)"

    class Companion(@Excluded val task: TaskValue) {

        val name = task.identifier

    }

}