package spritz.value.list

import spritz.SpritzEnvironment
import spritz.api.Coercion
import spritz.api.annotations.Excluded
import spritz.builtin.companions.ListCompanion
import spritz.error.interpreting.JvmError
import spritz.interpreter.context.Context
import spritz.util.coercedName
import spritz.value.Value
import spritz.value.table.result.Result
import java.lang.reflect.Field

/**
 * @author surge
 * @since 18/03/2023
 */
class ListValue(val elements: MutableList<Value>) : Value("list") {

    init {
        SpritzEnvironment.putIntoTable(ListCompanion(this), this.table, Context("list"))
    }

    override fun asJvmValue() = elements
    override fun toString() = elements.toString()

}