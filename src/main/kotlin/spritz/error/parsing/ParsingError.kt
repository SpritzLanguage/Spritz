package spritz.error.parsing

import spritz.error.Error
import spritz.lexer.position.Position

/**
 * @author surge
 * @since 25/02/2023
 */
class ParsingError(details: String, start: Position, end: Position) : Error("ParsingError", details, start, end)