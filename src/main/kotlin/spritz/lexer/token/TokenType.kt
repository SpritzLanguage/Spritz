package spritz.lexer.token

/**
 * @author surge
 * @since 25/02/2023
 */
enum class TokenType {
    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE,
    KEYWORD,
    IDENTIFIER,
    INT,
    FLOAT,
    OPEN_BRACE,
    CLOSE_BRACE,
    OPEN_PARENTHESES,
    CLOSE_PARENTHESES,
    OPEN_SQUARE,
    CLOSE_SQUARE,
    END_OF_FILE
}