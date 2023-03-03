package spritz

import spritz.error.Error
import spritz.interpreter.Interpreter
import spritz.interpreter.RuntimeResult
import spritz.interpreter.context.Context
import spritz.lexer.Lexer
import spritz.lexer.position.LinkPosition
import spritz.lexer.token.Token
import spritz.parser.ParseResult
import spritz.parser.Parser
import spritz.parser.node.Node
import spritz.value.bool.BoolValue
import spritz.value.symbols.Symbol
import spritz.value.symbols.SymbolData
import spritz.value.symbols.Table
import spritz.value.task.DefinedTaskValue

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

        if (interpretingResult.first.error != null) {
            return interpretingResult.first.error
        }

        return null
    }

    fun lex(name: String, contents: String): Pair<List<Token<*>>, Error?> = Lexer(name, contents).lex()
    fun parse(tokens: List<Token<*>>): ParseResult = Parser(tokens).parse()

    fun interpret(node: Node): Pair<RuntimeResult, Table> {
        val interpreter = Interpreter()

        val context = Context("<program>")

        val global = Table()
        global.set(Symbol("true", BoolValue(true), SymbolData(immutable = true, LinkPosition("true", "global symbol table"), LinkPosition("true", "global symbol table"))), context, declaration = true)
        global.set(Symbol("false", BoolValue(false), SymbolData(immutable = true, LinkPosition("false", "global symbol table"), LinkPosition("false", "global symbol table"))), context, declaration = true)

        context.givenTable(global)

        val time = System.currentTimeMillis()
        val result = interpreter.visit(node, context)
        println("Time: ${System.currentTimeMillis() - time}ms")

        return Pair(result, global)
    }


}