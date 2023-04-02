package spritz.error.interpreting

import spritz.interpreter.context.Context
import spritz.lexer.position.Position

/**
 * @author surge
 * @since 02/03/2023
 */
class ImportError(details: String, start: Position, end: Position, context: Context) : RuntimeError("ImportError", details, start, end, context)