package spritz.value.task

import spritz.api.CallData
import spritz.interpreter.RuntimeResult
import spritz.interpreter.context.Context
import spritz.lexer.position.Position
import spritz.util.RequiredArgument
import spritz.value.Value
import java.lang.reflect.Method

/**
 * @author surge
 * @since 03/03/2023
 */
class JvmTaskValue(identifier: String, val method: Method, val invoke: (functionData: CallData) -> RuntimeResult, val arguments: List<RequiredArgument>) : TaskValue(identifier, "Jvm Task") {

    override fun asJvmValue() = method

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
        return JvmTaskValue(this.identifier, this.method, this.invoke, this.arguments)
            .positioned(this.start, this.end)
            .givenContext(this.context)
    }

    override fun toString() = "(Jvm Task: $identifier)"

}