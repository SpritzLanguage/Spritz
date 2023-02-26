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

        val line = this.start.contents.substring(this.start.index - this.start.column until this.end.index)

        result += line

        if (result.last() !in System.lineSeparator()) {
            result += System.lineSeparator()
        }

        result += " " * (start.column - 1)
        result += "^" * (end.column - start.column - 1)

        return result
    }

}