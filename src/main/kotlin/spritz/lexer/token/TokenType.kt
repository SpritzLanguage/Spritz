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
    OPENING_BRACE,
    CLOSING_BRACE,
    OPENING_PARENTHESIS,
    CLOSING_PARENTHESIS,
    OPENING_SQUARE,
    CLOSING_SQUARE,
    END_OF_FILE
}