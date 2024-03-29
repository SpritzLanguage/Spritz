package spritz.error.interpreting

import spritz.error.Error
import spritz.interpreter.context.Context
import spritz.lexer.position.Position

/**
 * @author surge
 * @since 01/03/2023
 */
open class RuntimeError(name: String, details: String, start: Position, end: Position, val context: Context) : Error(name, details, start, end) {

    private fun generateTraceback(): String {
        var result = ""
        var position: Position? = this.start
        var context: Context? = this.context

        while (context != null && position != null) {
            result = "  File ${context.name}, line ${position.line + 1}\n$result"
            position = context.parentEntryPosition
            context = context.parent
        }

        return "Traceback (most recent call last):\n$result"
    }

    override fun toString(): String {
        return super.toString() + "\n" + generateTraceback()
    }

}