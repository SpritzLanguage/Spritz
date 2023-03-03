package spritz.value.symbols

import spritz.lexer.position.Position

/**
 * @author surge
 * @since 02/03/2023
 */
data class SymbolData(val immutable: Boolean, val start: Position, val end: Position)