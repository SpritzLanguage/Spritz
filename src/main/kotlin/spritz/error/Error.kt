package spritz.error

import spritz.lexer.position.Position
import spritz.util.times

/**
 * @author surge
 * @since 25/02/2023
 */
open class Error(val name: String, val details: String, val start: Position, val end: Position) {

    override fun toString(): String {
        var result = "$name, line ${start.line}: $details\n\n"

        //val line = start.contents.split('\n')[start.line]

        /* val startColumn = start.column else 0
		val endColumn = end.column if i == line_count - 1 else len(line) - 1 */

        val line = this.start.contents.substring(start.index..end.index)

        val startColumn = start.column - 1
        val endColumn = end.column - 3

        result += line
        result += (" " * startColumn) + ("^" * (endColumn - startColumn))

        return result
    }

}