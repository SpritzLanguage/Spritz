package spritz.parser

import spritz.error.parsing.ParsingError
import spritz.lexer.token.Token
import spritz.lexer.token.TokenType

/**
 * @author surge
 * @since 26/02/2023
 */
class Parser(val tokens: List<Token<*>>) {

    private var index = -1
    private lateinit var currentToken: Token<*>

    fun parse(): ParseResult {
        val result = this.statements()

        if (result.error != null && this.currentToken.type != TokenType.END_OF_FILE) {
            return result.failure(ParsingError(
                "Expected an operator!",
                this.currentToken.start,
                this.currentToken.end
            ))
        }

        return result
    }

    private fun statements(): ParseResult {
        val result = ParseResult()

        return result
    }

}