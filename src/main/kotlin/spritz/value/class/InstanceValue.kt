package spritz.value.`class`

import spritz.value.Value
import spritz.value.table.Table

/**
 * @author surge
 * @since 04/03/2023
 */
open class InstanceValue(val parent: DefinedClassValue, table: Table) : Value(parent.type) {

    init {
        this.table = table
    }

    override fun asJvmValue() = this
    override fun toString() = super.toString().ifEmpty { "(Instance of $identifier)" }

}