package spritz.warning

import spritz.lexer.position.Position

/**
 * @author surge
 * @since 10/03/2023
 */
open class Warning(val details: String, val start: Position) {

    override fun toString() = "Warning: line ${start.line + 1}: $details"

}