package spritz

import spritz.error.Error
import spritz.lexer.Lexer
import spritz.lexer.token.Token
import spritz.parser.ParseResult
import spritz.parser.Parser

/**
 * @author surge
 * @since 25/02/2023
 */
class Spritz {

    fun evaluate(name: String, contents: String): Error? {
        val lexingResult = lex(name, contents)

        if (lexingResult.second != null) {
            return lexingResult.second
        }

        val parsingResult = parse(lexingResult.first)

        if (parsingResult.error != null) {
            return parsingResult.error
        }

        return null
    }

    fun lex(name: String, contents: String): Pair<List<Token<*>>, Error?> {
        val lexer = Lexer(name, contents)
        return lexer.lex()
    }

    fun parse(tokens: List<Token<*>>): ParseResult {
        val parser = Parser(tokens)
        return parser.parse()
    }

}