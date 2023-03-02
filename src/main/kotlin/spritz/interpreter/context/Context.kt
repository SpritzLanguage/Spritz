package spritz.interpreter.context

import spritz.lexer.position.Position

/**
 * @author surge
 * @since 01/03/2023
 */
data class Context(val name: String, val parent: Context? = null, val parentEntryPosition: Position? = null)