package spritz.value.task

import spritz.error.interpreting.TypeMismatchError
import spritz.interpreter.Interpreter
import spritz.interpreter.RuntimeResult
import spritz.interpreter.context.Context
import spritz.lexer.position.Position
import spritz.parser.node.Node
import spritz.util.RequiredArgument
import spritz.value.NullValue
import spritz.value.PrimitiveValue
import spritz.value.Value
import spritz.value.table.Table

/**
 * @author surge
 * @since 03/03/2023
 */
class DefinedTaskValue(identifier: String, val arguments: List<RequiredArgument>, val body: Node, val expression: Boolean, val returnType: Value?) : TaskValue(identifier) {

    override fun asJvmValue() = this

    override fun execute(passed: List<Value>, start: Position, end: Position, context: Context): RuntimeResult {
        val result = RuntimeResult()
        val interpreter = Interpreter()

        val execContext = generateExecuteContext()

        result.register(this.checkAndPopulate(arguments, passed, start, end, execContext))

        if (result.shouldReturn()) {
            return result
        }

        val value = result.register(interpreter.visit(this.body, execContext))

        if (result.error != null) {
            return result
        }

        val returnValue = (if (expression) value else null) ?: (result.returnValue ?: NullValue().positioned(start, end)).givenContext(context)

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
        return DefinedTaskValue(identifier, arguments, body, expression, returnType).positioned(this.start, this.end).givenContext(this.context) as DefinedTaskValue
    }

}