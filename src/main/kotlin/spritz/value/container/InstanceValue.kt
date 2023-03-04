package spritz.value.container

import spritz.value.Value
import spritz.value.symbols.Table

/**
 * @author surge
 * @since 04/03/2023
 */
open class InstanceValue(val parent: DefinedContainerValue, table: Table) : Value(parent.type) {

    init {
        this.table = table
    }

    override fun matchesType(type: Value) = super.matchesType(type) || type.type == parent.type
    override fun toString() = "(Instance of $type: ${table.symbols})"

}