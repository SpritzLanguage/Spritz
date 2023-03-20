package spritz.value.table

import spritz.lexer.position.Position
import spritz.value.Value

/**
 * @author surge
 * @since 17/03/2023
 */
data class Symbol(val name: String, var value: Value, val start: Position, val end: Position) {

    var immutable = false

    fun setImmutability(immutable: Boolean): Symbol {
        this.immutable = immutable

        return this
    }

}