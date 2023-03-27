package spritz.value.enum

import spritz.util.coercedName
import spritz.value.`class`.InstanceValue
import spritz.value.`class`.JvmInstanceValue
import spritz.value.table.TableAccessor

/**
 * I am not documenting this.
 *
 * @author surge
 * @since 18/03/2023
 */
class JvmEnumValue(val instance: Class<Enum<*>>) : InstanceValue(instance::class.java.simpleName) {

    init {
        instance.enumConstants.forEach { member ->
            val instance = JvmInstanceValue(member)

            TableAccessor(this.table)
                .identifier(member.javaClass.coercedName())
                .immutable(true)
        }
    }

    override fun asJvmValue() = instance
    override fun toString() = instance.toString()

}