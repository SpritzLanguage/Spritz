package spritz.value.container

import spritz.Spritz
import spritz.api.Coercion
import spritz.interpreter.context.Context
import spritz.lexer.position.LinkPosition
import spritz.value.Value
import spritz.value.symbols.Symbol
import spritz.value.symbols.SymbolData
import java.lang.reflect.Modifier

/**
 * @author surge
 * @since 04/03/2023
 */
class JvmInstanceValue(val instance: Any) : Value(instance::class.java.simpleName) {

    init {
        this.context = Context(this.identifier)

        Spritz.loadInto(instance, this.table, this.context)

        this.context.table = this.table
    }

    override fun toString() = "(Jvm Instance: ${instance::class.java.simpleName})"

}