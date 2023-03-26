package spritz.value.number

import spritz.SpritzEnvironment
import spritz.builtin.companions.NumberCompanion
import spritz.interpreter.context.Context

/**
 * @author surge
 * @since 18/03/2023
 */
class FloatValue(value: Float) : NumberValue<Float>(value, "float") {

    init {
        SpritzEnvironment.putIntoTable(NumberCompanion(this), this.table, Context("companion"))
    }

}