package spritz

import spritz.api.Coercion
import spritz.api.Config
import spritz.api.LoadType
import spritz.api.annotations.Excluded
import spritz.api.annotations.Identifier
import spritz.builtin.Global
import spritz.builtin.Standard
import spritz.error.Error
import spritz.interpreter.Interpreter
import spritz.interpreter.RuntimeResult
import spritz.interpreter.context.Context
import spritz.lexer.Lexer
import spritz.lexer.position.LinkPosition
import spritz.lexer.token.Token
import spritz.lexer.token.TokenType
import spritz.parser.ParseResult
import spritz.parser.Parser
import spritz.parser.node.Node
import spritz.util.RequiredArgument
import spritz.value.PrimitiveReferenceValue
import spritz.value.container.DefinedContainerValue
import spritz.value.container.JvmContainerValue
import spritz.value.container.JvmInstanceValue
import spritz.value.symbols.Symbol
import spritz.value.symbols.SymbolData
import spritz.value.symbols.Table
import java.lang.reflect.Modifier

/**
 * @author surge
 * @since 25/02/2023
 */
class Spritz(val config: Config = Config()) {

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

        parsingResult.warnings.forEach {
            println(it)
        }

        val interpretingResult = interpret(parsingResult.node!!)

        if (interpretingResult.first.error != null) {
            return interpretingResult.first.error
        }

        return null
    }

    fun lex(name: String, contents: String): Pair<List<Token<*>>, Error?> = Lexer(name, contents).lex()
    fun parse(tokens: List<Token<*>>): ParseResult = Parser(config, tokens).parse()

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

        context.givenTable(globalTable)
    }

    fun load(instance: Any, identifier: String, table: Table = globalTable, type: LoadType = LoadType.INSTANCE): Spritz {
        val value = if (type == LoadType.INSTANCE) {
            JvmInstanceValue(instance)
        } else {
            JvmContainerValue(identifier, instance::class.java, instance)
        }

        table.set(Symbol(identifier, value, SymbolData(immutable = true, LinkPosition(), LinkPosition())), context, true)

        return this
    }

    fun loadAsLibrary(identifier: String, modules: HashMap<String, Pair<Any, LoadType>>): Spritz {
        val container = DefinedContainerValue(identifier, listOf(), null).positioned(LinkPosition(), LinkPosition()).givenContext(this.context)

        modules.forEach { (identifier, data) ->
            val instance = data.first
            val type = data.second

            load(instance, identifier, container.table, type)
        }

        globalTable.set(Symbol(identifier, container, SymbolData(immutable = true, LinkPosition(), LinkPosition())), context, true)

        return this
    }

    fun loadStandard(): Spritz {
        load(Standard, "std")


        return this
    }

    companion object {

        fun loadInto(instance: Any, table: Table, context: Context) {
            instance::class.java.declaredFields.forEach { field ->
                if (field.name == "INSTANCE" || field.getAnnotation(Excluded::class.java) != null) {
                    return@forEach
                }

                field.isAccessible = true

                val name = field.getAnnotation(Identifier::class.java)?.identifier ?: field.name

                table.set(
                    Symbol(
                        name,
                        Coercion.JvmToSpritz.coerce(field.get(instance)).positioned(LinkPosition(), LinkPosition()).givenContext(context),
                        SymbolData(immutable = Modifier.isFinal(field.modifiers), LinkPosition(), LinkPosition())
                    ),
                    Context(instance::class.java.simpleName),
                    true
                )
            }

            instance::class.java.declaredMethods.forEach { method ->
                if (method.isAnnotationPresent(Excluded::class.java)) {
                    return@forEach
                }

                method.isAccessible = true

                val name = method.getAnnotation(Identifier::class.java)?.identifier ?: method.name

                table.set(
                    Symbol(
                        name,
                        Coercion.JvmToSpritz.coerceMethod(instance, name, method).positioned(LinkPosition(), LinkPosition()).givenContext(context),
                        SymbolData(immutable = Modifier.isFinal(method.modifiers), LinkPosition(), LinkPosition())
                    ),
                    Context(instance::class.java.simpleName),
                    true
                )
            }
        }

    }

}