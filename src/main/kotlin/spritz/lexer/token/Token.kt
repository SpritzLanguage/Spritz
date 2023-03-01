package spritz.lexer.token

import spritz.lexer.position.Position

/**
 * @author surge
 * @since 25/02/2023
 */
class Token<T>(val type: TokenType, val value: T?, start: Position, end: Position? = null) {

    val start = start.clone()
    val end = end ?: start.clone().advance()

    fun matches(value: Any?, type: TokenType = TokenType.KEYWORD): Boolean = this.type == type && this.value == value

    override fun toString(): String {
        return "(${type.name.lowercase()}${if (value != null) ", '$value'" else ""})"
    }

}