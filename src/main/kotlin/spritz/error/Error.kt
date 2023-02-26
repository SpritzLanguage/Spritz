package spritz.error

import spritz.lexer.position.Position
import spritz.util.times
import java.lang.Integer.max

/**
 * @author surge
 * @since 25/02/2023
 */
open class Error(val name: String, val details: String, val start: Position, val end: Position) {

    override fun toString(): String {
        var result = "$name, line ${start.line + 1}: $details\n\n"

        val line = this.start.contents.substring(max(start.contents.lastIndexOfAny(charArrayOf('\n'), start.index), 0)..start.contents.indexOf('\n', start.index))

        result += line

        result += " " * (start.column - 1)
        result += "^" * (end.column - start.column - 1)

        return result
    }

}