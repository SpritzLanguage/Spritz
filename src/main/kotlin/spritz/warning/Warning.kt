package spritz.warning

import spritz.lexer.position.Position

/**
 * A warning that has been produced during parsing. Contains [details] of
 * the warning as well as the [start] of where the warning was produced.
 *
 * @author surge
 * @since 10/03/2023
 */
open class Warning(val details: String, val start: Position) {

    override fun toString() = "Warning: line ${start.line + 1}: $details"

}