package spritz.value.table

import spritz.error.interpreting.DualDeclarationError
import spritz.error.interpreting.UndefinedReferenceError
import spritz.interpreter.context.Context
import spritz.lexer.position.Position
import spritz.value.Value
import spritz.value.table.result.Result

/**
 * @author surge
 * @since 17/03/2023
 */
class Table(val parent: Table? = null) {

    var getOverride: (Table.(String?, (Value?) -> Boolean, Boolean, Data) -> Result)? = null
    var setOverride: (Table.(Symbol) -> Result)? = null

    val symbols = mutableListOf<Symbol>()

    fun set(symbol: Symbol, context: Context, declaration: Boolean = true, forced: Boolean = false): Result {
        if (setOverride != null) {
            return this.setOverride!!(symbol)
        }

        if (declaration) {
            this.symbols.firstOrNull { it.name == symbol.name }?.let {
                if (forced) {
                    it.value = symbol.value
                    return Result(symbol.value, null)
                } else {
                    return Result(
                        null, DualDeclarationError(
                            "${it.name} was already defined",
                            it.start,
                            it.end,
                            context
                        )
                    )
                }
            }

            this.symbols.add(symbol)

            return Result(symbol.value, null)
        }

        this.symbols.firstOrNull { it.name == symbol.name }?.let {
            it.value = symbol.value
            return Result(symbol.value, null)
        }

        if (parent != null) {
            return parent.set(symbol,  context, false, forced)
        }

        return Result(
            null,
            UndefinedReferenceError(
                "'${symbol.name}' was not found in the current scope",
                symbol.start,
                symbol.end,
                context
            )
        )
    }

    fun setGet(get: (Table.(String?, (Value?) -> Boolean, Boolean, Data) -> Result)): Table {
        this.getOverride = get

        return this
    }

    fun setSet(set: (Table.(Symbol) -> Result)): Table {
        this.setOverride = set

        return this
    }

    data class Data(val start: Position, val end: Position, val context: Context)

}