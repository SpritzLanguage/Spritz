package spritz.value.table

import spritz.lexer.position.Position
import spritz.value.Value

/**
 * Holds the [name], [value], and position of a symbol.
 *
 * @author surge
 * @since 17/03/2023
 */
data class Symbol(val name: String, var value: Value, val start: Position, val end: Position) {

    var immutable = false

    /**
     * Sets the [immutable] state of this symbol.
     * @return This instance.
     */
    fun setImmutability(immutable: Boolean): Symbol {
        this.immutable = immutable

        return this
    }

}