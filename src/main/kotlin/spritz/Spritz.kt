package spritz

import spritz.api.Coercion
import spritz.builtin.Global
import spritz.builtin.Standard
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
import spritz.value.container.JvmInstanceValue
import spritz.value.symbols.Symbol
import spritz.value.symbols.SymbolData
import spritz.value.symbols.Table
import java.lang.reflect.Modifier

/**
 * @author surge
 * @since 25/02/2023
 */
class Spritz {

    var context = Context("<program>")
    var globalTable = Table()

    init {
        reset()
    }

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

        val time = System.currentTimeMillis()

        val result = interpreter.visit(node, context)

        println("Time: ${System.currentTimeMillis() - time}ms")

        return Pair(result, globalTable)
    }

    fun reset() {
        context = Context("<program>")
        globalTable = Table()

        loadInto(Global, globalTable, context)
        load(Standard, "std")

        //globalTable.set(Symbol("true", BoolValue(true), SymbolData(immutable = true, LinkPosition("true", "global symbol table"), LinkPosition("true", "global symbol table"))), context, true)
        //globalTable.set(Symbol("false", BoolValue(false), SymbolData(immutable = true, LinkPosition("false", "global symbol table"), LinkPosition("false", "global symbol table"))), context, true)
        //globalTable.set(Symbol("std", JvmInstanceValue(Standard).givenContext(context), SymbolData(immutable = true, LinkPosition(), LinkPosition())), context, true)

        context.givenTable(globalTable)
    }

    fun load(instance: Any, identifier: String): Spritz {
        val jvmInstance = JvmInstanceValue(instance)

        globalTable.set(Symbol(identifier, jvmInstance, SymbolData(immutable = true, LinkPosition(), LinkPosition())), context, true)

        return this
    }

    companion object {

        fun loadInto(instance: Any, table: Table, context: Context) {
            instance::class.java.declaredFields.forEach { field ->
                if (field.name == "INSTANCE") {
                    return@forEach
                }

                table.set(
                    Symbol(
                        field.name,
                        Coercion.JvmToSpritz.coerce(field.get(instance)).positioned(LinkPosition(), LinkPosition())
                            .givenContext(context),
                        SymbolData(immutable = Modifier.isFinal(field.modifiers), LinkPosition(), LinkPosition())
                    ),
                    Context(instance::class.java.simpleName),
                    true
                )
            }

            instance::class.java.declaredMethods.forEach { method ->
                table.set(
                    Symbol(
                        method.name,
                        Coercion.JvmToSpritz.coerceMethod(instance, method).positioned(LinkPosition(), LinkPosition())
                            .givenContext(context),
                        SymbolData(immutable = Modifier.isFinal(method.modifiers), LinkPosition(), LinkPosition())
                    ),
                    Context(instance::class.java.simpleName),
                    true
                )
            }

            println(table.symbols)
        }

    }

}