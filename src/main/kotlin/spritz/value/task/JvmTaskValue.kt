package spritz.value.task

import spritz.api.CallData
import spritz.error.interpreting.ReturnTypeMismatchError
import spritz.interpreter.Interpreter
import spritz.interpreter.RuntimeResult
import spritz.interpreter.context.Context
import spritz.lexer.position.Position
import spritz.util.RequiredArgument
import spritz.value.NullValue
import spritz.value.Value

/**
 * @author surge
 * @since 03/03/2023
 */
class JvmTaskValue(identifier: String, val invoke: (functionData: CallData) -> RuntimeResult, val arguments: List<RequiredArgument>) : TaskValue(identifier, "Jvm Task") {

    override fun execute(passed: List<Value>, start: Position, end: Position, context: Context): RuntimeResult {
        val result = RuntimeResult()

        result.register(this.check(this.arguments, passed, start, end, context))

        if (result.shouldReturn()) {
            return result
        }

        val value = result.register(invoke(CallData(this.start, this.end, context, passed, this)))

        if (result.shouldReturn()) {
            return result
        }

        return result.success(value)
    }

    override fun clone(): Value {
        return JvmTaskValue(this.identifier, this.invoke, this.arguments)
            .positioned(this.start, this.end)
            .givenContext(this.context)
    }

    override fun toString() = "(Jvm Task: $identifier)"

}