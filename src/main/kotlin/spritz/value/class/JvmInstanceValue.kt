package spritz.value.`class`

import spritz.api.Coercion
import spritz.api.annotations.Excluded
import spritz.error.interpreting.JvmError
import spritz.util.coercedName
import spritz.util.getAllFields
import spritz.util.getAllMethods
import spritz.value.Value
import spritz.value.table.result.Result
import java.lang.reflect.Field

/**
 * @author surge
 * @since 18/03/2023
 */
class JvmInstanceValue(val instance: Any) : Value(instance::class.java.simpleName) {

    init {
        this.table.setGet { identifier, predicate, _, data ->
            try {
                var field: Any? = instance::class.java.getAllFields().filter { !it.isAnnotationPresent(Excluded::class.java) && predicate(Coercion.IntoSpritz.coerce(it.get(instance))) }.firstOrNull { it.coercedName() == identifier }?.also { it.isAccessible = true }

                if (field == null) {
                    field = instance::class.java.getAllMethods().filter { !it.isAnnotationPresent(Excluded::class.java) && predicate(Coercion.IntoSpritz.coerce(it)) }.firstOrNull { it.coercedName() == identifier }?.also { it.isAccessible = true }
                }

                if (field == null) {
                    field = instance::class.java.declaredClasses.filter { !it.isAnnotationPresent(Excluded::class.java) && predicate(Coercion.IntoSpritz.coerce(it)) }.firstOrNull { it.name == identifier }
                }

                if (field == null) {
                    return@setGet Result(null, JvmError(
                        "'$identifier' is not present in ${instance::class.java.simpleName}",
                        data.start,
                        data.end,
                        data.context
                    ))
                }

                return@setGet Result(Coercion.IntoSpritz.coerce(field, instance).positioned(data.start, data.end).givenContext(data.context), null)
            } catch (exception: Exception) {
                return@setGet Result(null, JvmError(
                    exception.message!!,
                    data.start,
                    data.end,
                    data.context
                ))
            }
        }

        this.table.setSet {
            try {
                val field: Field = instance::class.java.declaredFields.filter { !it.isAnnotationPresent(Excluded::class.java) }.firstOrNull { field -> field.coercedName() == identifier }
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