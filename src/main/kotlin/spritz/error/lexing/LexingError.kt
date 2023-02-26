package spritz.error.lexing

import spritz.error.Error
import spritz.lexer.position.Position

/**
 * @author surge
 * @since 25/02/2023
 */
class LexingError(details: String, start: Position, end: Position) : Error("LexingError", details, start, end, )