package spritz

import spritz.api.Coercion
import spritz.api.Config
import spritz.api.annotations.Excluded
import spritz.api.annotations.Identifier
import spritz.builtin.Global
import spritz.builtin.Standard
import spritz.builtin.System
import spritz.error.Error
import spritz.interpreter.Interpreter
import spritz.interpreter.context.Context
import spritz.lexer.Lexer
import spritz.lexer.position.LinkPosition
import spritz.parser.Parser
import spritz.util.coercedName
import spritz.util.getAllFields
import spritz.util.getAllMethods
import spritz.value.`class`.JvmClassValue
import spritz.value.table.Table
import spritz.value.table.TableAccessor
import spritz.warning.Warning
import java.io.File
import java.lang.reflect.Modifier
import java.nio.charset.Charset

/**
 * An environment that contains the global value table, origin context, handlers, etc.
 *
 * @author surge
 * @since 25/02/2023
 */
class SpritzEnvironment(val config: Config = Config()) {

    // bottom of the table hierarchy
    val global = Table()

    // bottom of the context hierarchy
    val origin = Context("<program>", config = config)
        .givenTable(global)
        .also {
            it.environment = this
        }

    init {
        // load builtins
        if (config.loadDefaults) {
            this.putIntoGlobal(Global)
            this.addLibrary("std", Standard)
            this.addLibrary("system", System)
        }
    }

    /**
     * Evaluates the given [content]. [fileName] is used for error handling.
     * @return An [EvaluationResult], which contains the returned value, warnings, and an error, if one was produced.
     */
    fun evaluate(fileName: String, content: String): EvaluationResult {
        origin.name = fileName

        val lexer = Lexer(fileName, content).lex()

        if (lexer.second != null) {
            config.errorStream(lexer.second!!)
            return EvaluationResult(null, listOf(), lexer.second)
        }

        val parser = Parser(config, lexer.first).parse()

        parser.warnings.forEach(config.warningStream::invoke)

        if (parser.error != null) {
            config.errorStream(parser.error!!)
            return EvaluationResult(null, parser.warnings, parser.error)
        }

        val interpreter = Interpreter().visit(parser.node!!, origin)

        if (interpreter.error != null) {
            config.errorStream(interpreter.error!!)
            return EvaluationResult(null, parser.warnings, interpreter.error)
        }

        return EvaluationResult(interpreter.value, parser.warnings, null)
    }

    /**
     * Evaluates the given [file].
     * @see evaluate
     */
    fun evaluate(file: File): EvaluationResult = evaluate(file.name, file.readText(Charset.defaultCharset()))

    /**
     * Adds a referencable value to the [global] value table, which can be used in a script.
     * @return This environment
     */
    fun putInstance(identifier: String, instance: Any): SpritzEnvironment {
        TableAccessor(global)
            .identifier(identifier)
            .immutable(true)
            .set(Coercion.IntoSpritz.coerce(instance).linked())

        return this
    }

    /**
     * Adds a referencable class to the [global] value table, which can be instantiated in a script.
     * @return This environment
     */
    fun putClass(identifier: String, clazz: Class<*>): SpritzEnvironment {
        TableAccessor(global)
            .identifier(identifier)
            .immutable(true)
            .set(JvmClassValue(identifier, clazz).linked())

        return this
    }

    /**
     * Directly adds the (current) fields and methods to the [global] value table. The values will never update.
     * @return This environment
     */
    fun putIntoGlobal(instance: Any): SpritzEnvironment {
        putIntoTable(instance, global, origin)

        return this
    }

    /**
     * Adds a library to the map of imports in the origin context
     * @return This environment
     */
    fun addLibrary(identifier: String, instance: Any): SpritzEnvironment {
        origin.putImport(identifier, Coercion.IntoSpritz.coerce(instance).linked())
        return this
    }

    companion object {

        /**
         * Directly adds the (current) fields and methods from the given [instance] into the given [table].
         * Values are given [context].
         */
        @JvmStatic
        fun putIntoTable(instance: Any, table: Table, context: Context) {
            instance::class.java.getAllFields().forEach {
                if (it.isAnnotationPresent(Excluded::class.java) || it.isSynthetic) {
                    return@forEach
                }

                it.isAccessible = true

                TableAccessor(table)
                    .identifier(it.coercedName())
                    .immutable(true)
                    .set(Coercion.IntoSpritz.coerce(it, instance).linked(), data = Table.Data(LinkPosition(), LinkPosition(), context))
            }

            instance::class.java.getAllMethods().forEach {
                if (it.isAnnotationPresent(Excluded::class.java) || it.isSynthetic) {
                    return@forEach
                }

                it.isAccessible = true

                TableAccessor(table)
                    .identifier(it.coercedName())
                    .immutable(true)
                    .set(Coercion.IntoSpritz.coerce(it, instance).linked(), data = Table.Data(LinkPosition(), LinkPosition(), context))
            }
        }

        /**
         * Loads the static fields and methods of a [clazz] into a given [table].
         */
        @JvmStatic
        fun staticLoad(clazz: Class<*>, table: Table, context: Context) {
            clazz.declaredFields.forEach {
                if (it.isAnnotationPresent(Excluded::class.java) || it.isSynthetic) {
                    return@forEach
                }

                if (Modifier.isPublic(it.modifiers) && Modifier.isStatic(it.modifiers)) {
                    TableAccessor(table)
                        .identifier(it.coercedName())
                        .immutable(true)
                        .set(Coercion.IntoSpritz.coerce(it, null).linked(), data = Table.Data(LinkPosition(), LinkPosition(), context))
                }
            }

            clazz.declaredMethods.forEach {
                if (it.isAnnotationPresent(Excluded::class.java) || it.isSynthetic) {
                    return@forEach
                }

                if (Modifier.isPublic(it.modifiers) && Modifier.isStatic(it.modifiers)) {
                    TableAccessor(table)
                        .identifier(it.coercedName())
                        .immutable(true)
                        .set(Coercion.IntoSpritz.coerce(it, null).linked(), data = Table.Data(LinkPosition(), LinkPosition(), context))
                }
            }

            clazz.declaredClasses.forEach {
                if (it.isAnnotationPresent(Excluded::class.java) || it.isSynthetic) {
                    return@forEach
                }

                if (Modifier.isPublic(it.modifiers)) {
                    TableAccessor(table)
                        .identifier(it.coercedName())
                        .immutable(true)
                        .set(
                            Coercion.IntoSpritz.coerce(it, null).linked(),
                            data = Table.Data(LinkPosition(), LinkPosition(), context)
                        )
                }
            }

            clazz.enumConstants?.forEach {
                it as Enum<*>

                if (it.declaringClass.getField(it.name).isAnnotationPresent(Excluded::class.java)) {
                    return@forEach
                }

                TableAccessor(table)
                    .identifier(it.coercedName())
                    .immutable(true)
                    .set(
                        Coercion.IntoSpritz.coerce(it, null).linked(),
                        data = Table.Data(LinkPosition(), LinkPosition(), context)
                    )
            }
        }

    }

}