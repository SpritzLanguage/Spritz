package spritz.value.list

import spritz.SpritzEnvironment
import spritz.builtin.companions.ListCompanion
import spritz.interpreter.context.Context
import spritz.value.Value

/**
 * @author surge
 * @since 18/03/2023
 */
class ListValue(val elements: MutableList<Value>) : Value("list") {

    init {
        SpritzEnvironment.putIntoTable(ListCompanion(this), this.table, Context("companion"))
    }

    override fun asJvmValue() = elements
    override fun toString() = elements.toString()

}