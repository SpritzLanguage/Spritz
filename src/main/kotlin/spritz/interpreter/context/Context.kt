package spritz.interpreter.context

import spritz.SpritzEnvironment
import spritz.api.Config
import spritz.error.interpreting.ImportError
import spritz.interpreter.RuntimeResult
import spritz.lexer.position.Position
import spritz.value.Value
import spritz.value.`class`.DefinedClassValue
import spritz.value.`class`.DefinedInstanceValue
import spritz.value.`class`.InstanceValue
import spritz.value.table.Table
import java.io.File
import java.nio.charset.Charset

/**
 * @author surge
 * @since 01/03/2023
 */
data class Context(var name: String, val parent: Context? = null, val parentEntryPosition: Position? = null, val config: Config? = null) {

    lateinit var table: Table
    lateinit var environment: SpritzEnvironment

    private val imports = hashMapOf<String, Value>()

    fun givenTable(table: Table): Context {
        this.table = table
        return this
    }

    fun getOrigin(): Context {
        if (this.parent == null) {
            return this
        }

        return this.parent.getOrigin()
    }

    /**
     * This should ONLY be called from the origin
     */
    fun getImport(identifier: String, start: Position, end: Position, context: Context): RuntimeResult {
        val result = RuntimeResult()

        var import = imports[identifier]

        if (import == null && identifier.endsWith(".sz")) {
            val file = File(identifier)

            if (file.exists()) {
                val environment = SpritzEnvironment(environment.config)

                val res = environment.evaluate(file.name, file.readText(Charset.defaultCharset()))

                if (res.error != null) {
                    return result.failure(res.error)
                }

                import = DefinedInstanceValue(DefinedClassValue(identifier, arrayListOf(), null), environment.global)
            }
        }

        if (import == null) {
            return result.failure(
                ImportError(
                "Couldn't locate '$identifier'",
                start,
                end,
                context
            ))
        }

        return result.success(import)
    }

    fun putImport(identifier: String, value: Value) {
        imports[identifier] = value
    }

}