package spritz.error.interpreting

import spritz.interpreter.context.Context
import spritz.lexer.position.Position

/**
 * @author surge
 * @since 02/03/2023
 */
class CallArgumentMismatchError(details: String, start: Position, end: Position, context: Context) : RuntimeError("CallArgumentMismatchError", details, start, end, context)