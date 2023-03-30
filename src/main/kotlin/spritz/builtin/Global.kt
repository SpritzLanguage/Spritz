package spritz.builtin

import spritz.api.annotations.Identifier
import spritz.util.NUMBERS
import spritz.value.PrimitiveValue
import spritz.value.bool.BooleanValue
import spritz.value.`class`.DefinedClassValue
import spritz.value.`class`.DefinedInstanceValue
import spritz.value.`class`.JvmClassValue
import spritz.value.`class`.JvmInstanceValue
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

    @Identifier("true") const val `true` = true
    @Identifier("false") const val `false` = false
    @Identifier("null") @JvmField val `null` = null

    // primitive types
    @JvmField val any = PrimitiveValue("any") { _, required -> required.type == "any" }
    @JvmField val int = PrimitiveValue("int") { given, required -> given is IntValue && required.type == "int" }
    @JvmField val float = PrimitiveValue("float") { given, required -> given is FloatValue && required.type == "float" }
    @JvmField val number = PrimitiveValue("number") { given, required -> given is NumberValue<*> && required.type in NUMBERS }
    @JvmField val boolean = PrimitiveValue("boolean") { given, required -> given is BooleanValue && required.type == "boolean" }
    @JvmField val string = PrimitiveValue("string") { given, required -> given is StringValue && required.type == "string" }
    @JvmField val task = PrimitiveValue("task") { given, required -> given is TaskValue && required.type == "task" }
    @JvmField val list = PrimitiveValue("list") { given, required -> given is ListValue && required.type == "list" }
    @JvmField val `class` = PrimitiveValue("class") { given, required -> (given is DefinedClassValue || given is JvmClassValue) && required.type == "class" }
    @JvmField val instance = PrimitiveValue("instance") { given, required -> (given is DefinedInstanceValue || given is JvmInstanceValue) && required.type == "instance" }

}