package spritz

import spritz.error.Error
import spritz.interpreter.Interpreter
import spritz.interpreter.RuntimeResult
import spritz.interpreter.context.Context
import spritz.lexer.Lexer
import spritz.lexer.token.Token
import spritz.parser.ParseResult
import spritz.parser.Parser
import spritz.parser.node.Node
import spritz.value.symbols.Table

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

        val interpretingResult = interpret(parsingResult.node!!)

        if (interpretingResult.error != null) {
            return interpretingResult.error
        }

        println(interpretingResult.value)

        return null
    }

    fun lex(name: String, contents: String): Pair<List<Token<*>>, Error?> = Lexer(name, contents).lex()
    fun parse(tokens: List<Token<*>>): ParseResult = Parser(tokens).parse()

    fun interpret(node: Node): RuntimeResult {
        val interpreter = Interpreter()

        val global = Table()

        val context = Context("<program>").givenTable(global)

        val time = System.currentTimeMillis()
        val result = interpreter.visit(node, context)
        println("Time: ${System.currentTimeMillis() - time}ms")

        return result
    }


}