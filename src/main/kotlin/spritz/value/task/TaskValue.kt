package spritz.value.task

import spritz.error.Error
import spritz.error.interpreting.CallArgumentMismatchError
import spritz.error.interpreting.TypeMismatchError
import spritz.interpreter.RuntimeResult
import spritz.interpreter.context.Context
import spritz.lexer.position.Position
import spritz.util.RequiredArgument
import spritz.value.NullValue
import spritz.value.PrimitiveValue
import spritz.value.Value
import spritz.value.table.Table
import spritz.value.table.TableAccessor

/**
 * A value representing a task.
 *
 * @author surge
 * @since 18/03/2023
 */
abstract class TaskValue(identifier: String, type: String = "task") : Value(type, identifier) {

    abstract override fun asJvmValue(): Any

    /**
     * Generates a new context which we can execute from.
     */
    protected fun generateExecuteContext(): Context {
        val context = Context(this.identifier, this.context, this.start)
        context.table = Table(context.parent!!.table)
        return context
    }

    /**
     * Checks whether the [given] arguments are valid for the [required] arguments.
     * @return A [RuntimeResult] that contains an optional error.
     */
    protected fun check(required: List<RequiredArgument>, given: List<Value>, start: Position, end: Position, context: Context): RuntimeResult {
        val result = RuntimeResult()

        // too many arguments
        if (given.size > required.size) {
            return result.failure(CallArgumentMismatchError(
                "Too many arguments passed when calling '$identifier', expected ${required.size}, got ${given.size}",
                start,
                end,
                context
            ))
        }

        // too little arguments
        if (given.size < required.size) {
            return result.failure(CallArgumentMismatchError(
                "Too little arguments passed when calling '$identifier', expected ${required.size}, got ${given.size}",
                start,
                end,
                context
            ))
        }

        // check types
        given.forEachIndexed { index, givenValue ->
            val matched = required[index]

            // any type
            if (matched.type == null || matched.type.type == "any") {
                return@forEachIndexed
            }

            // if the given value isn't null, and it doesn't match the correct type
            if (givenValue !is NullValue && !(PrimitiveValue.check(givenValue, matched.type) || givenValue.type == matched.type.type)) {
                return result.failure(TypeMismatchError(
                    "Given value did not conform to type '${matched.type.type}' (got '${givenValue.type}')",
                    givenValue.start,
                    givenValue.end,
                    context
                ))
            }
        }

        return result.success(null)
    }

    /**
     * Adds the [given] arguments to the given [context].
     * @return An optional error, if we couldn't successfully set the argument.
     */
    private fun populate(required: List<RequiredArgument>, given: List<Value>, context: Context): Error? {
        given.forEachIndexed { index, passedArgument ->
            val matched = required[index]

            passedArgument.givenContext(context)

            val result = TableAccessor(context.table)
                .identifier(matched.name.value.toString())
                .immutable(true)
                .set(passedArgument, declaration = true, data = Table.Data(passedArgument.start, passedArgument.end, context))

            if (result.error != null) {
                return result.error
            }
        }

        return null
    }

    /**
     * Calls the [check] and [populate] methods.
     * @return A [RuntimeResult] that contains any error we may have run into.
     */
    protected fun checkAndPopulate(required: List<RequiredArgument>, given: List<Value>, start: Position, end: Position, context: Context): RuntimeResult {
        val result = RuntimeResult()

        // check arguments
        result.register(this.check(required, given, start, end, context))

        if (result.shouldReturn()) {
            return result
        }

        // populate arguments
        val error = this.populate(required, given, context)

        if (error != null) {
            return result.failure(error)
        }

        return result.success(null)
    }

    override fun toString() = "($type: $identifier)"

}