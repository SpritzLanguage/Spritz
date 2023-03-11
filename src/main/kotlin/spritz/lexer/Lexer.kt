package spritz.lexer

import spritz.error.Error
import spritz.error.lexing.LexingError
import spritz.lexer.position.Position
import spritz.lexer.token.Token
import spritz.lexer.token.TokenType.*
import spritz.util.keyword

/**
 * @author surge
 * @since 25/02/2023
 */
class Lexer(val name: String, val contents: String) {

    private val position: Position = Position(name, contents, -1, 0, -1)
    private var currentChar: Char? = null

    private val digits = "0123456789"
    private val identifierLetters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_$digits"

    init {
        advance()
    }

    /**
     * Generates and returns a list of [Token]s and an [Error], if the lexing failed.
     */
    fun lex(): Pair<List<Token<*>>, Error?> {
        val tokens = mutableListOf<Token<*>>()

        while (this.currentChar != null) {
            when (this.currentChar!!) {
                // skip spaces and tabs
                in " \t;${System.lineSeparator()}" -> this.advance()

                in digits -> {
                    var result = ""
                    var dots = 0
                    var type = INT
                    val start = this.position.clone()

                    while (this.currentChar != null && this.currentChar!! in "$digits.") {
                        result += if (this.currentChar == '.') {
                            if (dots == 1) {
                                break
                            }

                            dots++
                            type = FLOAT

                            '.'
                        } else {
                            this.currentChar
                        }

                        this.advance()
                    }

                    if (this.currentChar == 'f' || this.currentChar == 'F') {
                        type = FLOAT

                        if (dots == 0) {
                            result += ".0"
                        }

                        this.advance()
                    }

                    if (dots == 1) {
                        type = FLOAT
                    }

                    if (this.currentChar == 'b' || this.currentChar == 'B') {
                        this.advance()

                        if (dots > 0) {
                            return Pair(mutableListOf(), LexingError("The floating point literal does not conform to type 'byte'", start, this.position.clone()))
                        }

                        type = BYTE
                    }

                    tokens.add(Token(type, result, start, this.position.clone()))
                }

                in identifierLetters -> {
                    var identifier = ""
                    val start = this.position.clone()

                    while (this.currentChar != null && this.currentChar!! in identifierLetters) {
                        identifier += this.currentChar
                        this.advance()
                    }

                    tokens.add(Token(if (keyword(identifier)) KEYWORD else IDENTIFIER, identifier, start, this.position.clone()))
                }

                '+' -> {
                    var type = PLUS
                    val start = this.position.clone()

                    this.advance()

                    if (this.currentChar == '+') {
                        type = INCREMENT
                        this.advance()
                    } else if (this.currentChar == '=') {
                        type = INCREASE_BY
                        this.advance()
                    }

                    tokens.add(Token(type, null, start, this.position.clone()))
                }

                '-' -> {
                    var type = MINUS
                    val start = this.position.clone()

                    this.advance()

                    when (this.currentChar) {
                        '-' -> {
                            type = DEINCREMENT
                            this.advance()
                        }

                        '=' -> {
                            type = DECREASE_BY
                            this.advance()
                        }

                        '>' -> {
                            type = ARROW
                            this.advance()
                        }
                    }

                    tokens.add(Token(type, null, start, this.position.clone()))
                }

                '*' -> {
                    var type = ASTERISK
                    val start = this.position.clone()

                    this.advance()

                    if (this.currentChar == '=') {
                        type = MULTIPLY_BY
                        this.advance()
                    }

                    tokens.add(Token(type, null, start, this.position.clone()))
                }

                '/' -> {
                    var type = DIVIDE
                    val start = this.position.clone()

                    this.advance()

                    if (this.currentChar == '*') {
                        this.advance()

                        while (this.currentChar != null) {
                            if (this.currentChar == '*') {
                                this.advance()

                                if (this.currentChar == '/') {
                                    this.advance()
                                    break
                                }
                            } else {
                                this.advance()
                            }
                        }

                    } else if (this.currentChar == '/') {
                        while (this.currentChar != null && this.currentChar!! !in System.lineSeparator()) {
                            this.advance()
                        }
                    } else {
                        if (this.currentChar == '=') {
                            type = DIVIDE_BY
                            this.advance()
                        }

                        tokens.add(Token(type, null, start, this.position.clone()))
                    }
                }

                '%' -> {
                    var type = MODULO
                    val start = this.position.clone()

                    this.advance()

                    if (this.currentChar == '=') {
                        type = MODULO_BY
                        this.advance()
                    }

                    tokens.add(Token(type, null, start, this.position.clone()))
                    this.advance()
                }

                '{' -> {
                    tokens.add(Token(OPEN_BRACE, null, this.position.clone()))
                    this.advance()
                }

                '}' -> {
                    tokens.add(Token(CLOSE_BRACE, null, this.position.clone()))
                    this.advance()
                }

                '(' -> {
                    tokens.add(Token(OPEN_PARENTHESES, null, this.position.clone()))
                    this.advance()
                }

                ')' -> {
                    tokens.add(Token(CLOSE_PARENTHESES, null, this.position.clone()))
                    this.advance()
                }

                '[' -> {
                    tokens.add(Token(OPEN_SQUARE, null, this.position.clone()))
                    this.advance()
                }

                ']' -> {
                    tokens.add(Token(CLOSE_SQUARE, null, this.position.clone()))
                    this.advance()
                }

                ':' -> {
                    var type = COLON
                    val start = this.position.clone()

                    this.advance()

                    if (this.currentChar == ':') {
                        type = ACCESSOR
                        this.advance()
                    }

                    tokens.add(Token(type, null, start, this.position.clone()))
                }

                ',' -> {
                    tokens.add(Token(COMMA, null, this.position.clone()))
                    this.advance()
                }

                '"' -> {
                    var result = ""
                    val start = this.position.clone()

                    var escape = false
                    this.advance()

                    val escapeCharacters = hashMapOf(
                        Pair('n', '\n'),
                        Pair('t', '\t')
                    )

                    while (this.currentChar != null && (this.currentChar != '"' || escape)) {
                        if (escape) {
                            result += escapeCharacters[this.currentChar]
                        } else {
                            if (this.currentChar == '\\') {
                                escape = true
                            } else {
                                result += this.currentChar
                                escape = false
                            }
                        }

                        this.advance()
                    }

                    tokens.add(Token(STRING, result, start, this.position.clone()))
                    this.advance()
                }

                '<' -> {
                    var type = ARROW_LEFT
                    val start = this.position.clone()

                    this.advance()

                    if (this.currentChar == '=') {
                        type = LESS_THAN_OR_EQUAL_TO
                        this.advance()
                    } else if (this.currentChar == '<') {
                        type = BIN_SHIFT_LEFT
                        this.advance()
                    }

                    tokens.add(Token(type, null, start, this.position.clone()))
                }

                '>' -> {
                    var type = ARROW_RIGHT
                    val start = this.position.clone()

                    this.advance()

                    if (this.currentChar == '=') {
                        type = GREATER_THAN_OR_EQUAL_TO
                        this.advance()
                    } else if (this.currentChar == '>') {
                        type = BIN_SHIFT_RIGHT
                        this.advance()

                        if (this.currentChar == '>') {
                            type = BIN_UNSIGNED_SHIFT_RIGHT
                            this.advance()
                        }
                    }

                    tokens.add(Token(type, null, start, this.position.clone()))
                }

                '=' -> {
                    var type = ASSIGNMENT
                    val start = this.position.clone()

                    this.advance()

                    if (this.currentChar == '=') {
                        type = EQUALITY
                        this.advance()
                    }

                    tokens.add(Token(type, null, start, this.position.clone()))
                }

                '!' -> {
                    var type = NEGATE
                    val start = this.position.clone()

                    this.advance()

                    if (this.currentChar == '=') {
                        type = INEQUALITY
                        this.advance()
                    }

                    tokens.add(Token(type, null, start, this.position.clone()))
                }

                '&' -> {
                    var type = BIN_AND
                    val start = this.position.clone()

                    this.advance()

                    if (this.currentChar == '&') {
                        type = AND
                        this.advance()
                    }

                    tokens.add(Token(type, null, start, this.position.clone()))
                }

                '|' -> {
                    var type = BIN_OR
                    val start = this.position.clone()

                    this.advance()

                    if (this.currentChar == '|') {
                        type = OR
                        this.advance()
                    }

                    tokens.add(Token(type, null, start, this.position.clone()))
                }

                '^' -> {
                    tokens.add(Token(BIN_XOR, null, this.position.clone()))
                    this.advance()
                }

                '~' -> {
                    var type = BIN_COMPLEMENT
                    val start = this.position.clone()

                    this.advance()

                    if (this.currentChar == '!') {
                        this.advance()

                        if (this.currentChar == '=') {
                            type = ROUGH_INEQUALITY
                            this.advance()
                        }
                    } else if (this.currentChar == '=') {
                        type = ROUGH_EQUALITY
                        this.advance()
                    }

                    tokens.add(Token(type, null, start, this.position.clone()))
                }

                else -> {
                    val start = this.position.clone()
                    val char = this.currentChar!!

                    this.advance()

                    return Pair(mutableListOf(), LexingError("Illegal Character: '$char'", start, this.position.clone()))
                }
            }
        }

        tokens.add(Token(END_OF_FILE, null, start = this.position.clone()))
        return Pair(tokens, null)
    }

    private fun advance(amount: Int = 1) {
        this.position.advance(currentChar, amount)
        this.currentChar = if (this.position.index < this.contents.length) this.contents[this.position.index] else null
    }

}