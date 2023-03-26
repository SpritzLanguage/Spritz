package spritz.value.`class`

import spritz.api.Coercion
import spritz.api.annotations.Excluded
import spritz.error.interpreting.JvmError
import spritz.util.coercedName
import spritz.util.getAllFields
import spritz.util.getAllMethods
import spritz.value.table.result.Result
import java.lang.reflect.Field

/**
 * I am not documenting this.
 *
 * @author surge
 * @since 18/03/2023
 */
class JvmInstanceValue(val instance: Any) : InstanceValue(instance::class.java.simpleName) {

    init {
        this.table.overrideGet { identifier, predicate, _, data ->
            try {
                var field: Any? = instance::class.java.getAllFields().filter { !it.isAnnotationPresent(Excluded::class.java) && predicate(Coercion.IntoSpritz.coerce(it.get(instance))) }.firstOrNull { it.coercedName() == identifier }?.also { it.isAccessible = true }

                if (field == null) {
                    field = instance::class.java.getAllMethods().filter { !it.isAnnotationPresent(Excluded::class.java) && predicate(Coercion.IntoSpritz.coerce(it)) }.firstOrNull { it.coercedName() == identifier }?.also { it.isAccessible = true }
                }

                if (field == null) {
                    field = instance::class.java.declaredClasses.filter { !it.isAnnotationPresent(Excluded::class.java) && predicate(Coercion.IntoSpritz.coerce(it)) }.firstOrNull { it.name == identifier }
                }

                if (field == null) {
                    return@overrideGet Result(null, JvmError(
                        "'$identifier' is not present in ${instance::class.java.simpleName}",
                        data.start,
                        data.end,
                        data.context
                    ))
                }

                return@overrideGet Result(Coercion.IntoSpritz.coerce(field, instance).position(data.start, data.end).givenContext(data.context), null)
            } catch (exception: Exception) {
                return@overrideGet Result(null, JvmError(
                    exception.message!!,
                    data.start,
                    data.end,
                    data.context
                ))
            }
        }

        this.table.overrideSet {
            try {
                val field: Field = instance::class.java.declaredFields.filter { !it.isAnnotationPresent(Excluded::class.java) }.firstOrNull { field -> field.coercedName() == identifier }
                    ?: throw Error("'$identifier' is not present in ${instance::class.java.simpleName}")

                field.set(instance, it.value.asJvmValue())

                return@overrideSet Result(null, null)
            } catch (exception: Exception) {
                return@overrideSet Result(null, JvmError(
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