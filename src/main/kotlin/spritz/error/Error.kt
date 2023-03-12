package spritz.error

import spritz.lexer.position.LinkPosition
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

        var endIndex = this.start.contents.indexOf('\n', this.start.index - this.start.column)

        if (endIndex == -1) {
            endIndex = this.start.contents.length
        }

        val raw = this.start.contents.substring(this.start.index - this.start.column until endIndex)
        val line = raw.trim()

        result += line.replace("\r", "")

        if (result.last() !in System.lineSeparator()) {
            result += System.lineSeparator()
        }

        if (start !is LinkPosition && end !is LinkPosition && end.column - start.column >= 0) {
            result += " ".repeat(start.column - countLeadingWhitespace(raw))
            result += "^".repeat(end.column - start.column)
        }

        return result
    }

    private fun countLeadingWhitespace(str: String): Int {
        var count = 0

        for (i in str.indices) {
            if (!str[i].isWhitespace()) {
                break
            }

            count++
        }

        return count
    }

}