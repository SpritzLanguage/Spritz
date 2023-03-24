package spritz.value.`class`

import spritz.error.interpreting.JvmError
import spritz.interpreter.RuntimeResult
import spritz.interpreter.context.Context
import spritz.lexer.position.Position
import spritz.value.Value
import spritz.value.task.TaskValue

/**
 * @author surge
 * @since 04/03/2023
 */
class JvmClassValue(identifier: String, val clazz: Class<*>) : TaskValue(identifier = identifier, identifier) {

    override fun asJvmValue() = clazz

    override fun execute(passed: List<Value>, start: Position, end: Position, context: Context): RuntimeResult {
        val constructorArgs = mutableListOf<Any?>()

        val constructor = clazz.constructors.first { it.parameterCount == passed.size }

        passed.forEachIndexed { index, it ->
            constructorArgs.add(it.asJvmValue())
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