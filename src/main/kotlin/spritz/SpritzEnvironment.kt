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
import spritz.value.table.TableAccessor
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

    init {
        if (config.loadDefaults) {
            this.putInstance("std", Standard)
            this.putIntoGlobal(Global)
        }
    }

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

    fun putInstance(identifier: String, instance: Any): SpritzEnvironment {
        TableAccessor(global)
            .identifier(identifier)
            .immutable(true)
            .set(Coercion.IntoSpritz.coerce(instance).linked())

        return this
    }

    fun putClass(identifier: String, clazz: Class<*>): SpritzEnvironment {
        TableAccessor(global)
            .identifier(identifier)
            .immutable(true)
            .set(JvmContainerValue(identifier, clazz).linked())

        return this
    }

    fun putIntoGlobal(instance: Any): SpritzEnvironment {
        putIntoTable(instance, global, origin)

        return this
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

                TableAccessor(table)
                    .identifier(it.coercedName())
                    .immutable(true)
                    .set(Coercion.IntoSpritz.coerce(it).linked(), data = Table.Data(LinkPosition(), LinkPosition(), context))
            }

            instance::class.java.declaredMethods.forEach {
                if (it.isAnnotationPresent(Excluded::class.java)) {
                    return@forEach
                }

                it.isAccessible = true

                TableAccessor(table)
                    .identifier(it.coercedName())
                    .immutable(true)
                    .set(Coercion.IntoSpritz.coerce(it, instance).linked(), data = Table.Data(LinkPosition(), LinkPosition(), context))
            }
        }

    }

}