package spritz.value.container

import spritz.error.interpreting.JvmError
import spritz.error.interpreting.RuntimeError
import spritz.error.interpreting.TypeMismatchError
import spritz.interpreter.Interpreter
import spritz.interpreter.RuntimeResult
import spritz.interpreter.context.Context
import spritz.lexer.position.Position
import spritz.parser.node.Node
import spritz.util.RequiredArgument
import spritz.value.NullValue
import spritz.value.Value
import spritz.value.bool.BoolValue
import spritz.value.list.ListValue
import spritz.value.number.NumberValue
import spritz.value.string.StringValue
import spritz.value.symbols.Symbol
import spritz.value.symbols.SymbolData
import spritz.value.symbols.Table
import spritz.value.task.TaskValue

/**
 * @author surge
 * @since 04/03/2023
 */
class JvmContainerValue(identifier: String, val clazz: Class<*>, instance: Any) : TaskValue(identifier = identifier, "container") {

    override fun execute(passed: List<Value>, start: Position, end: Position, context: Context): RuntimeResult {
        val constructorArgs = mutableListOf<Any?>()

        val constructor = clazz.constructors.first { it.parameterCount == passed.size }

        passed.forEachIndexed { index, it ->
            constructorArgs.add(when (it) {
                is BoolValue -> it.value
                is JvmInstanceValue -> it.instance
                is ListValue -> it.elements
                is NullValue -> null

                is NumberValue<*> -> {
                    if (it.value is Int) {
                        it.value.toInt()
                    } else {
                        it.value.toFloat()
                    }
                }

                is StringValue -> {
                    it.value
                }

                else -> {
                    if (Value::class.java.isAssignableFrom(constructor.parameters[index].type)) {
                        it
                    } else {
                        return RuntimeResult().failure(
                            TypeMismatchError(
                                "Failed to coerce constructor arguments: '$it'",
                                this.start,
                                this.end,
                                this.context
                            )
                        )
                    }
                }
            })
        }

        try {
            val classInstance = constructor.newInstance(*constructorArgs.toTypedArray())

            val instance = JvmInstanceValue(classInstance)
                .positioned(start, end)
                .givenContext(context)

            return RuntimeResult().success(instance)
        } catch (exception: Exception) {
            return RuntimeResult().failure(JvmError(
                "Failed to instantiate JVM Class instance!\n\nJVM STACK TRACE WILL FOLLOW\n\n${
                    run {
                        exception.printStackTrace()
                        ""
                    }
                }",
                start,
                end,
                context
            ))
        }
    }

    override fun toString() = "($identifier)"

}