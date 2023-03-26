package spritz.value.`class`

import spritz.SpritzEnvironment
import spritz.builtin.companions.ClassCompanion
import spritz.builtin.companions.InstanceCompanion
import spritz.interpreter.context.Context
import spritz.value.table.Table

/**
 * @author surge
 * @since 04/03/2023
 */
class DefinedInstanceValue(val parent: DefinedClassValue, table: Table) : InstanceValue(parent.type) {

    init {
        this.table = table
        SpritzEnvironment.putIntoTable(InstanceCompanion(this), this.table, Context("companion"))
    }

    override fun asJvmValue() = this
    override fun toString() = super.toString().ifEmpty { "(Instance of $identifier)" }

}