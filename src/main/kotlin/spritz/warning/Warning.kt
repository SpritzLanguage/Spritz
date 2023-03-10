package spritz.warning

import spritz.lexer.position.Position
import spritz.util.times

/**
 * @author surge
 * @since 10/03/2023
 */
open class Warning(val details: String, val start: Position, val end: Position) {

    override fun toString(): String {
        var result = "Warning: line ${start.line + 1}: $details\n\n"

        var endIndex = this.start.contents.indexOf('\n', this.start.index - this.start.column)

        if (endIndex == -1) {
            endIndex = this.start.contents.length
        }

        val line = "> " + this.start.contents.substring(this.start.index - this.start.column until endIndex).trim()

        result += line.replace("\r", "")

        if (result.last() !in System.lineSeparator()) {
            result += System.lineSeparator()
        }

        return result
    }

}