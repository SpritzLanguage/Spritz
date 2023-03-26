package spritz.value.table

import spritz.interpreter.context.Context
import spritz.lexer.position.Position
import spritz.value.Value
import spritz.value.table.result.Result

/**
 * Holds values.
 *
 * @author surge
 * @since 17/03/2023
 */
class Table(val parent: Table? = null) {

    // used if we don't want to get symbols the normal way
    var getOverride: (Table.(String?, (Value?) -> Boolean, Boolean, Data) -> Result)? = null

    // used if we don't want to set symbols the normal way
    var setOverride: (Table.(Symbol) -> Result)? = null

    // a list of symbols
    val symbols = mutableListOf<Symbol>()

    /**
     * Overrides the [get] method that is found in [TableAccessor].
     * @return This instance.
     */
    fun overrideGet(get: (Table.(String?, (Value?) -> Boolean, Boolean, Data) -> Result)): Table {
        this.getOverride = get

        return this
    }

    /**
     * Overrides the [set] method that is found in [TableAccessor].
     * @return This instance.
     */
    fun overrideSet(set: (Table.(Symbol) -> Result)): Table {
        this.setOverride = set

        return this
    }

    /**
     * Data class that holds [start] and [end] positions, as well as [context].
     */
    data class Data(val start: Position, val end: Position, val context: Context)

}