package spritz.api

import spritz.api.result.Failure
import spritz.api.result.Success
import spritz.interpreter.RuntimeResult
import spritz.interpreter.context.Context
import spritz.lexer.position.LinkPosition
import spritz.lexer.token.Token
import spritz.lexer.token.TokenType
import spritz.util.RequiredArgument
import spritz.util.coercedName
import spritz.value.NullValue
import spritz.value.Value
import spritz.value.bool.BooleanValue
import spritz.value.container.JvmInstanceValue
import spritz.value.list.ListValue
import spritz.value.number.ByteValue
import spritz.value.number.FloatValue
import spritz.value.number.IntValue
import spritz.value.number.NumberValue
import spritz.value.string.StringValue
import spritz.value.task.JvmTaskValue
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * @author surge
 * @since 18/03/2023
 */
object Coercion {

    object IntoSpritz {

        fun coerce(any: Any?, instance: Any? = any): Value {
            if (any == null) {
                return NullValue().positioned(LinkPosition(), LinkPosition()).givenContext(Context("null"))
            }

            return when (any) {
                is Value -> any

                is Field -> {
                    coerce(any.get(instance))
                }

                is Boolean -> BooleanValue(any)
                is Number -> coerceNumber(any)
                is String -> StringValue(any)
                is Method -> coerceMethod(instance!!, any.coercedName(), any)
                is List<*> -> ListValue(any.map { coerce(it) }.toMutableList())

                else -> JvmInstanceValue(any)

            }.positioned(LinkPosition(), LinkPosition()).givenContext(Context(any::class.java.simpleName))
        }

        fun coerceMethod(instance: Any, identifier: String, method: Method): Value {
            val types = method.parameterTypes.filter { it != CallData::class.java }
            val converted = arrayListOf<Class<*>>()

            types.forEach {
                converted.add(getEquivalentValue(it))
            }

            return JvmTaskValue(
                identifier,

                method,

                { data ->
                    val arguments = arrayListOf<Any>()

                    if (method.parameters.any { it.type == CallData::class.java }) {
                        arguments.add(data)
                    }

                    data.arguments.forEachIndexed { index, value ->
                        arguments.add(getEquivalentPrimitive(value, types[index]))
                    }

                    val result = method.invoke(instance, *arguments.toTypedArray())

                    if (result is RuntimeResult) {
                        return@JvmTaskValue result
                    }

                    if (result is Success) {
                        RuntimeResult().success(result.value)
                    } else if (result is Failure) {
                        RuntimeResult().failure(result.error!!)
                    } else {
                        RuntimeResult().success(coerce(result))
                    }
                },

                ArrayList(method.parameters.filter { it.type != CallData::class.java }.map { RequiredArgument(
                    Token(TokenType.IDENTIFIER, it.name, LinkPosition(), LinkPosition()), object : Value("any") {

                        override fun asJvmValue() = null
                        override fun toString() = it.toString()

                    }) }.toList())
            )
        }

    }

    fun coerceNumber(number: Number): NumberValue<*> {
        return when (number) {
            is Byte -> ByteValue(number)
            is Int, is Short, is Long -> IntValue(number.toInt())
            else -> FloatValue(number.toFloat())
        }
    }

    fun getEquivalentValue(clazz: Class<*>): Class<*> {
        if (isValue(clazz)) {
            return clazz
        }

        when (clazz) {
            Int::class.java, Float::class.java, Double::class.java, Long::class.java, Short::class.java -> {
                return NumberValue::class.java
            }

            String::class.java -> {
                return StringValue::class.java
            }

            Boolean::class.java -> {
                return BooleanValue::class.java
            }
        }

        throw IllegalStateException("No equivalent value found! (Got $clazz)")
    }

    fun getEquivalentPrimitive(value: Value, clazz: Class<*>): Any {
        if (isValue(clazz)) {
            return value
        }

        when (value) {
            is NumberValue<*> -> {
                when (clazz) {
                    Int::class.java -> {
                        return value.value.toInt()
                    }

                    Float::class.java -> {
                        return value.value.toFloat()
                    }

                    Double::class.java -> {
                        return value.value.toDouble()
                    }

                    Long::class.java -> {
                        return value.value.toLong()
                    }

                    Short::class.java -> {
                        return value.value.toShort()
                    }
                }
            }

            is StringValue -> {
                return value.value
            }

            is BooleanValue -> {
                return value.value
            }
        }

        throw IllegalStateException("No equivalent primitive found! (Got $value, $clazz)")
    }

    private fun isValue(clazz: Class<*>): Boolean {
        return clazz == Value::class.java || clazz.superclass == Value::class.java || clazz.superclass?.superclass == Value::class.java
    }

}