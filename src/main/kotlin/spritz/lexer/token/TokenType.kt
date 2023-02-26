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

    // binary shifts
    BIN_SHIFT_LEFT,
    BIN_SHIFT_RIGHT,
    BIN_UNSIGNED_SHIFT_RIGHT,
    BIN_OR,
    BIN_AND,
    BIN_XOR,
    BIN_COMPLEMENT,

    // comparison operators
    EQUALITY,
    LESS_THAN,
    GREATER_THAN,
    LESS_THAN_OR_EQUAL_TO,
    GREATER_THAN_OR_EQUAL_TO,

    // condition operators
    AND,
    OR,
    NEGATE,

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