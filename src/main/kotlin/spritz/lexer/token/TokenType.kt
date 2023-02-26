package spritz.lexer.token

/**
 * @author surge
 * @since 25/02/2023
 */
enum class TokenType {
    // operators
    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE,
    INCREMENT,
    DEINCREMENT,
    INCREASE_BY,
    DECREASE_BY,
    MULTIPLY_BY,
    DIVIDE_BY,

    // comparison operators
    EQUALITY,
    NEGATE,
    LESS_THAN,
    GREATER_THAN,
    LESS_THAN_OR_EQUAL_TO,
    GREATER_THAN_OR_EQUAL_TO,

    // references
    KEYWORD,
    IDENTIFIER,

    // inbuilt types
    INT,
    FLOAT,
    BYTE,
    STRING,

    // braces
    OPEN_BRACE,
    CLOSE_BRACE,
    OPEN_PARENTHESES,
    CLOSE_PARENTHESES,
    OPEN_SQUARE,
    CLOSE_SQUARE,

    // other syntax
    COLON,
    COMMA,
    ASSIGNMENT,
    ACCESSOR,

    END_OF_FILE
}