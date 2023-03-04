package spritz.api

import spritz.api.result.Failure
import spritz.api.result.Success
import spritz.interpreter.RuntimeResult
import spritz.interpreter.context.Context
import spritz.lexer.position.LinkPosition
import spritz.lexer.token.Token
import spritz.lexer.token.TokenType
import spritz.util.RequiredArgument
import spritz.value.NullValue
import spritz.value.PrimitiveReferenceValue
import spritz.value.Value
import spritz.value.bool.BoolValue
import spritz.value.container.JvmInstanceValue
import spritz.value.list.ListValue
import spritz.value.number.ByteValue
import spritz.value.number.FloatValue
import spritz.value.number.IntValue
import spritz.value.number.NumberValue
import spritz.value.string.StringValue
import spritz.value.task.JvmTaskValue
import java.lang.IllegalStateException
import java.lang.reflect.Method
import java.util.Arrays

/**
 * @author surge
 * @since 04/03/2023
 */
object Coercion {

    object JvmToSpritz {

        fun coerce(instance: Any?): Value {
            if (instance == null) {
                return NullValue()
            }

            return when (instance) {
                is Number -> coerceNumber(instance)
                is Boolean -> coerceBoolean(instance)
                is String -> coerceString(instance)
                is List<*> -> coerceList(instance)
                is Array<*> -> coerceList(listOf(instance))

                else -> {
                    coerceAny(instance)
                }
            }.positioned(LinkPosition(), LinkPosition()).givenContext(Context(instance::class.java.simpleName))
        }

        fun coerceNumber(number: Number): NumberValue<*> {
            return when (number) {
                is Byte -> ByteValue(number)
                is Int, is Short, is Long -> IntValue(number.toInt())
                else -> FloatValue(number.toFloat())
            }
        }

        fun coerceBoolean(boolean: Boolean): BoolValue {
            return BoolValue(boolean)
        }

        fun coerceString(string: String): StringValue {
            return StringValue(string)
        }

        fun coerceList(list: List<*>): ListValue {
            val elements = mutableListOf<Value>()

            list.forEach {
                elements.add(coerce(it!!))
            }

            return ListValue(elements)
        }

        fun coerceMethod(instance: Any, method: Method): JvmTaskValue {
            val types = method.parameterTypes.filter { it != CallData::class.java }
            val converted = arrayListOf<Class<*>>()

            types.forEach {
                converted.add(getEquivalentValue(it))
            }

            return JvmTaskValue(
                method.name,

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

                    when (result) {
                        is Success -> {
                            RuntimeResult().success(result.value)
                        }

                        is Failure -> {
                            RuntimeResult().failure(result.error!!)
                        }

                        is Boolean -> {
                            RuntimeResult().success(BoolValue(result))
                        }

                        is Number -> {
                            RuntimeResult().success(if (result.toString().contains('.')) {
                                FloatValue(result.toFloat())
                            } else if (result is Byte) {
                                ByteValue(result.toByte())
                            } else {
                                IntValue(result.toInt())
                            })
                        }

                        is String -> {
                            RuntimeResult().success(StringValue(result))
                        }

                        else -> {
                            RuntimeResult().success(NullValue())
                        }
                    }
                },

                ArrayList(method.parameters.filter { it.type != CallData::class.java }.map { RequiredArgument(Token(TokenType.IDENTIFIER, it.name, LinkPosition(), LinkPosition()), PrimitiveReferenceValue("any")) }.toList())
            )
        }

        fun coerceAny(any: Any): JvmInstanceValue {
            return JvmInstanceValue(any)
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
                return BoolValue::class.java
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

            is BoolValue -> {
                return value.value
            }
        }

        throw IllegalStateException("No equivalent primitive found! (Got $value, $clazz)")
    }

    private fun isValue(clazz: Class<*>): Boolean {
        return clazz == Value::class.java || clazz.superclass == Value::class.java || clazz.superclass?.superclass == Value::class.java
    }

}