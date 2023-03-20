package spritz

import spritz.api.Coercion
import spritz.api.Config
import spritz.api.annotations.Excluded
import spritz.builtin.Global
import spritz.builtin.Standard
import spritz.error.Error
import spritz.interpreter.Interpreter
import spritz.interpreter.context.Context
import spritz.lexer.Lexer
import spritz.lexer.position.LinkPosition
import spritz.parser.Parser
import spritz.util.coercedName
import spritz.value.Value
import spritz.value.container.JvmContainerValue
import spritz.value.table.Symbol
import spritz.value.table.Table
import spritz.warning.Warning

/**
 * @author surge
 * @since 25/02/2023
 */
class SpritzEnvironment(val config: Config = Config()) {

    val global = Table()
    val origin = Context("<program>").givenTable(global)

    private var warningHandler: (Warning) -> Unit = {}
    private var errorHandler: (Error) -> Unit = {}

    fun evaluate(fileName: String, content: String): EvaluationResult {
        val lexer = Lexer(fileName, content).lex()

        if (lexer.second != null) {
            errorHandler(lexer.second!!)
            return EvaluationResult(null, listOf(), lexer.second)
        }

        val parser = Parser(config, lexer.first).parse()

        parser.warnings.forEach(warningHandler::invoke)

        if (parser.error != null) {
            errorHandler(parser.error!!)
            return EvaluationResult(null, parser.warnings, parser.error)
        }

        val interpreter = Interpreter().visit(parser.node!!, origin)

        if (interpreter.error != null) {
            errorHandler(interpreter.error!!)
            return EvaluationResult(null, parser.warnings, interpreter.error)
        }

        return EvaluationResult(interpreter.value, parser.warnings, null)
    }

    fun loadDefaults(): SpritzEnvironment {
        this.putInstance("std", Standard)
        this.putIntoGlobal(Global)

        return this
    }

    fun putInstance(identifier: String, instance: Any): SpritzEnvironment {
        global.set(Symbol(identifier, Coercion.IntoSpritz.coerce(instance).linked(), LinkPosition(), LinkPosition()).setImmutability(true), origin)

        return this
    }

    fun putClass(identifier: String, clazz: Class<*>): SpritzEnvironment {
        global.set(Symbol(identifier, JvmContainerValue(identifier, clazz).linked(), LinkPosition(), LinkPosition()).setImmutability(true), origin)

        return this
    }

    fun putIntoGlobal(instance: Any): SpritzEnvironment {
        putIntoTable(instance, global, origin)

        return this
    }

    fun get(identifier: String): Value? {
        return global.get(identifier, LinkPosition(), LinkPosition(), origin).value
    }

    fun setWarningHandler(handler: (Warning) -> Unit): SpritzEnvironment {
        warningHandler = handler
        return this
    }

    fun setErrorHandler(handler: (Error) -> Unit): SpritzEnvironment {
        errorHandler = handler
        return this
    }

    companion object {

        @JvmStatic
        fun putIntoTable(instance: Any, table: Table, context: Context) {
            instance::class.java.declaredFields.forEach {
                if (it.isAnnotationPresent(Excluded::class.java)) {
                    return@forEach
                }

                it.isAccessible = true
                table.set(Symbol(it.coercedName(), Coercion.IntoSpritz.coerce(it).linked(), LinkPosition(), LinkPosition()).setImmutability(true), context)
            }

            instance::class.java.declaredMethods.forEach {
                if (it.isAnnotationPresent(Excluded::class.java)) {
                    return@forEach
                }

                it.isAccessible = true
                table.set(Symbol(it.coercedName(), Coercion.IntoSpritz.coerce(it, instance).linked(), LinkPosition(), LinkPosition()).setImmutability(true), context)
            }
        }

    }

}