package spritz.value.container

import spritz.api.Coercion
import spritz.error.interpreting.JvmError
import spritz.util.coercedName
import spritz.value.Value
import spritz.value.table.result.Result
import java.lang.reflect.Field

/**
 * @author surge
 * @since 18/03/2023
 */
class JvmInstanceValue(val instance: Any) : Value(instance::class.java.simpleName) {

    init {
        this.table.setGet {identifier, start, end, context ->
            try {
                var field: Any? = instance::class.java.declaredFields.firstOrNull { it.coercedName() == identifier }?.also { it.isAccessible = true }

                if (field == null) {
                    field = instance::class.java.declaredMethods.firstOrNull { it.coercedName() == identifier }?.also { it.isAccessible = true }
                }

                if (field == null) {
                    field = instance::class.java.declaredClasses.firstOrNull { it.name == identifier }
                }

                if (field == null) {
                    throw Error("'$identifier' is not present in ${instance::class.java.simpleName}")
                }

                return@setGet Result(Coercion.IntoSpritz.coerce(field, instance), null)
            } catch (exception: Exception) {
                return@setGet Result(null, JvmError(
                    exception.message!!,
                    start,
                    end,
                    context
                ))
            }
        }

        this.table.setSet {
            try {
                val field: Field = instance::class.java.declaredFields.firstOrNull { field -> field.coercedName() == identifier }
                    ?: throw Error("'$identifier' is not present in ${instance::class.java.simpleName}")

                field.set(instance, it.value.asJvmValue())

                return@setSet Result(null, null)
            } catch (exception: Exception) {
                return@setSet Result(null, JvmError(
                    exception.message!!,
                    start,
                    end,
                    context
                ))
            }
        }
    }

    override fun asJvmValue() = instance
    override fun toString() = instance.toString()

}