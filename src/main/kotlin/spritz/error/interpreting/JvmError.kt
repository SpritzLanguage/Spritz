package spritz.error.interpreting

import spritz.interpreter.context.Context
import spritz.lexer.position.Position

/**
 * @author surge
 * @since 02/03/2023
 */
class JvmError(details: String, start: Position, end: Position, context: Context) : RuntimeError("JvmError", details, start, end, context)