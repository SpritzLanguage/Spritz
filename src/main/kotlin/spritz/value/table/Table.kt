package spritz.value.table

import spritz.interpreter.context.Context
import spritz.lexer.position.Position
import spritz.value.Value
import spritz.value.table.result.Result

/**
 * @author surge
 * @since 17/03/2023
 */
class Table(val parent: Table? = null) {

    var getOverride: (Table.(String?, (Value?) -> Boolean, Boolean, Data) -> Result)? = null
    var setOverride: (Table.(Symbol) -> Result)? = null

    val symbols = mutableListOf<Symbol>()

    fun setGet(get: (Table.(String?, (Value?) -> Boolean, Boolean, Data) -> Result)): Table {
        this.getOverride = get

        return this
    }

    fun setSet(set: (Table.(Symbol) -> Result)): Table {
        this.setOverride = set

        return this
    }

    data class Data(val start: Position, val end: Position, val context: Context)

}