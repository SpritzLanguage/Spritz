package spritz.value.dictionary

import spritz.SpritzEnvironment
import spritz.builtin.companions.DictionaryCompanion
import spritz.interpreter.context.Context
import spritz.value.Value

/**
 * @author surge
 * @since 18/03/2023
 */
class DictionaryValue(val elements: HashMap<String, Value>) : Value("dictionary") {

    init {
        SpritzEnvironment.putIntoTable(DictionaryCompanion(this), this.table, Context("dictionary"))
    }

    override fun asJvmValue() = elements
    override fun toString() = elements.toString()

}