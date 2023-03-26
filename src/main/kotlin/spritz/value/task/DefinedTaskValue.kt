package spritz.value.task

import spritz.SpritzEnvironment
import spritz.error.interpreting.TypeMismatchError
import spritz.interpreter.Interpreter
import spritz.interpreter.RuntimeResult
import spritz.interpreter.context.Context
import spritz.lexer.position.Position
import spritz.parser.node.Node
import spritz.util.ANONYMOUS
import spritz.util.RequiredArgument
import spritz.value.NullValue
import spritz.value.PrimitiveValue
import spritz.value.Value

/**
 * A value representing a task that has been defined in a script.
 *
 * @author surge
 * @since 03/03/2023
 */
class DefinedTaskValue(identifier: String, val arguments: List<RequiredArgument>, val body: Node, val expression: Boolean, val returnType: Value?) : TaskValue(identifier) {

    override fun asJvmValue() = this

    override fun execute(passed: List<Value>, start: Position, end: Position, context: Context): RuntimeResult {
        val result = RuntimeResult()
        val interpreter = Interpreter()

        // we want to allow references to variables that have been defined in the current context
        // if this is an anonymous function, so we just check this identifier against the [ANONYMOUS]
        // identifier.
        val execContext = if (identifier == ANONYMOUS) context else generateExecuteContext()

        // attempt to check and populate the arguments
        result.register(this.checkAndPopulate(arguments, passed, start, end, execContext))

        if (result.shouldReturn()) {
            return result
        }

        // execute the task
        val value = result.register(interpreter.visit(this.body, execContext))

        if (result.error != null) {
            return result
        }

        // get the returned value
        val returnValue = (if (expression) value else null) ?: (result.returnValue ?: NullValue().position(start, end)).givenContext(context)

        // make sure the returned value conforms to the given return type.
        if (returnValue !is NullValue && returnType != null && !(PrimitiveValue.check(returnValue, returnType) || returnValue.type == returnType.type)) {
            return result.failure(TypeMismatchError(
                "Returned value did not conform to type '${returnType.type}' (got '${returnValue.type}')",
                returnValue.start,
                returnValue.end,
                context
            ))
        }

        return result.success(returnValue)
    }

    override fun clone(): DefinedTaskValue {
        return DefinedTaskValue(identifier, arguments, body, expression, returnType).position(this.start, this.end).givenContext(this.context) as DefinedTaskValue
    }

}