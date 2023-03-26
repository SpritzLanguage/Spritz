package spritz.value.`class`

import spritz.SpritzEnvironment
import spritz.builtin.companions.ClassCompanion
import spritz.error.interpreting.JvmError
import spritz.interpreter.RuntimeResult
import spritz.interpreter.context.Context
import spritz.lexer.position.Position
import spritz.util.allIndexed
import spritz.value.Value

/**
 * @author surge
 * @since 04/03/2023
 */
class JvmClassValue(identifier: String, val clazz: Class<*>) : ClassValue(identifier = identifier, identifier) {

    init {
        SpritzEnvironment.putIntoTable(ClassCompanion(this), this.table, Context("companion"))
        SpritzEnvironment.staticLoad(clazz, this.table, Context("static"))
    }

    override fun asJvmValue() = clazz

    override fun execute(passed: List<Value>, start: Position, end: Position, context: Context): RuntimeResult {
        val constructorArgs = mutableListOf<Any?>()

        // convert all passed arguments to their JVM representation.
        passed.forEachIndexed { index, it ->
            constructorArgs.add(it.asJvmValue())
        }

        // find first constructor with the same parameter size as the passed arguments.
        val constructor = clazz.constructors.first { constructor ->
            constructor.parameterCount == passed.size && constructor.parameters.allIndexed { index, parameter ->
                parameter.type.kotlin.javaObjectType.isAssignableFrom(constructorArgs[index]?.let { it::class.java })
            }
        }

        try {
            // instantiate class
            val classInstance = constructor.newInstance(*constructorArgs.toTypedArray())

            val instance = JvmInstanceValue(classInstance)
                .position(start, end)
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