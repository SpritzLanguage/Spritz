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
                    val start = this.position.clone()

                    var value = ""
                    var dots = 0

                    while (this.currentChar != null && this.currentChar!! in "$digits.") {
                        value += if (this.currentChar == '.') {
                            if (dots == 1) {
                                break
                            }

                            dots++
                        } else {
                            this.currentChar
                        }

                        this.advance()
                    }

                    /**
                     * TODO: Instead of adding `INT` and `FLOAT` tokens, transform them to classes.
                     * This will be similar to Kotlin, where `1.toString()` (etc) is perfectly valid.
                     * This will come into play once variables and types are re-added.
                     */

                    /**
                     * TODO: Adding `f` or `F` after a number will automatically force it to be a float.
                     * Again, just an extra feature to be similar to other languages, also useful when
                     * type declarations are added.
                     */

                    tokens.add(if (dots == 0) {
                        Token(INT, value.toInt(), start, this.position)
                    } else {
                        Token(FLOAT, value.toFloat(), start, this.position)
                    })
                }

                in identifierLetters -> {
                    var identifier = ""
                    val start = this.position.clone()

                    while (this.currentChar != null && this.currentChar!! in identifierLetters) {
                        identifier += this.currentChar
                        this.advance()
                    }

                    tokens.add(Token(if (keyword(identifier)) KEYWORD else IDENTIFIER, identifier, start, this.position))
                }

                '+' -> {
                    /**
                     * TODO: `+=` and `++` operators
                     */

                    tokens.add(Token(PLUS, null, this.position))
                    this.advance()
                }

                '-' -> {
                    /**
                     * TODO: `-=` and `--` operators
                     */

                    tokens.add(Token(MINUS, null, this.position))
                    this.advance()
                }

                '*' -> {
                    /**
                     * TODO: `*=` operator
                     */

                    tokens.add(Token(MULTIPLY, null, this.position))
                    this.advance()
                }

                '/' -> {
                    /**
                     * TODO: `/=` operator
                     */

                    tokens.add(Token(DIVIDE, null, this.position))
                    this.advance()
                }

                '{' -> {
                    tokens.add(Token(OPEN_BRACE, null, this.position))
                    this.advance()
                }

                '}' -> {
                    tokens.add(Token(CLOSE_BRACE, null, this.position))
                    this.advance()
                }

                '(' -> {
                    tokens.add(Token(OPEN_PARENTHESES, null, this.position))
                    this.advance()
                }

                ')' -> {
                    tokens.add(Token(CLOSE_PARENTHESES, null, this.position))
                    this.advance()
                }

                '[' -> {
                    tokens.add(Token(OPEN_SQUARE, null, this.position))
                    this.advance()
                }

                ']' -> {
                    tokens.add(Token(CLOSE_SQUARE, null, this.position))
                    this.advance()
                }

                ':' -> {
                    tokens.add(Token(COLON, null, this.position))
                    this.advance()
                }

                ',' -> {
                    tokens.add(Token(COMMA, null, this.position))
                    this.advance()
                }

                else -> {
                    val start = this.position.clone()
                    val char = this.currentChar!!

                    this.advance()

                    return Pair(mutableListOf(), LexingError("Illegal Character: '$char'", start, this.position))
                }
            }
        }

        tokens.add(Token(END_OF_FILE, null, start = this.position))
        return Pair(tokens, null)
    }

    private fun advance() {
        this.position.advance(currentChar)
        this.currentChar = if (this.position.index < this.contents.length) this.contents[this.position.index] else null
    }

}