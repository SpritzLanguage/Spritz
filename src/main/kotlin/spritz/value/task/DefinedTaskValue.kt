package spritz.value.task

import spritz.error.interpreting.ReturnTypeMismatchError
import spritz.interfaces.Cloneable
import spritz.interpreter.Interpreter
import spritz.interpreter.RuntimeResult
import spritz.interpreter.context.Context
import spritz.lexer.position.Position
import spritz.parser.nodes.ListNode
import spritz.util.Argument
import spritz.util.PassedArgument
import spritz.util.RequiredArgument
import spritz.value.NullValue
import spritz.value.Value

/**
 * @author surge
 * @since 03/03/2023
 */
class DefinedTaskValue(identifier: String, val arguments: List<RequiredArgument>, val body: ListNode, val expression: Boolean, val returnType: Value?) : TaskValue(identifier, "DefinedTask") {

    override fun execute(passed: List<PassedArgument>, start: Position, end: Position, context: Context): RuntimeResult {
        val result = RuntimeResult()
        val interpreter = Interpreter()

        result.register(this.checkAndPopulate(arguments, passed, start, end, context))

        if (result.shouldReturn()) {
            return result
        }

        val value = result.register(interpreter.visit(this.body, context))

        if (result.shouldReturn() && result.returnValue == null) {
            return result
        }

        val returnValue = (if (expression) value else null) ?: (result.returnValue ?: NullValue())

        if (returnType != null) {
            if (!returnValue.matchesType(returnType)) {
                return result.failure(ReturnTypeMismatchError(
                    "Returned value ($returnValue) was not of type $returnType",
                    returnValue.start,
                    returnValue.end,
                    context
                ))
            }
        }

        return result.success(returnValue)
    }

    override fun clone(): DefinedTaskValue {
        return DefinedTaskValue(identifier, arguments, body, expression, returnType).positioned(this.start, this.end).givenContext(this.context) as DefinedTaskValue
    }

}