package spritz.builtin

import spritz.api.annotations.Identifier
import spritz.value.PrimitiveValue
import spritz.value.bool.BooleanValue
import spritz.value.container.DefinedContainerValue
import spritz.value.container.InstanceValue
import spritz.value.container.JvmContainerValue
import spritz.value.container.JvmInstanceValue
import spritz.value.list.ListValue
import spritz.value.number.FloatValue
import spritz.value.number.IntValue
import spritz.value.number.NumberValue
import spritz.value.string.StringValue
import spritz.value.task.TaskValue

/**
 * @author surge
 * @since 04/03/2023
 */
object Global {

    @Identifier("true")
    const val TRUE = true

    @Identifier("false")
    const val FALSE = false

    @Identifier("null")
    @JvmField val NULL = null

    // primitive types
    @JvmField val int = PrimitiveValue("int") { it is IntValue }
    @JvmField val float = PrimitiveValue("float") { it is FloatValue }
    @JvmField val number = PrimitiveValue("number") { it is NumberValue<*> }
    @JvmField val boolean = PrimitiveValue("boolean") { it is BooleanValue }
    @JvmField val string = PrimitiveValue("string") { it is StringValue }
    @JvmField val task = PrimitiveValue("task") { it is TaskValue }
    @JvmField val list = PrimitiveValue("list") { it is ListValue }
    @JvmField val container = PrimitiveValue("container") { it is DefinedContainerValue || it is JvmContainerValue }
    @JvmField val instance = PrimitiveValue("instance") { it is InstanceValue || it is JvmInstanceValue }

}