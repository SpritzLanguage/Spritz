package spritz.value.table

import spritz.error.interpreting.DualDeclarationError
import spritz.error.interpreting.UndefinedReferenceError
import spritz.interpreter.context.Context
import spritz.lexer.position.Position
import spritz.value.table.result.Result

/**
 * @author surge
 * @since 17/03/2023
 */
class Table(val parent: Table? = null) {

    private var getOverride: (Table.(String, Position, Position, Context) -> Result)? = null
    private var setOverride: (Table.(Symbol) -> Result)? = null

    val symbols = mutableListOf<Symbol>()

    fun get(identifier: String, start: Position, end: Position, context: Context, top: Boolean = false): Result {
        if (getOverride != null) {
            return this.getOverride!!(identifier, start, end, context)
        }

        if (parent != null && !top) {
            return parent.get(identifier, start, end, context)
        }

        val existing = symbols.firstOrNull { it.name == identifier }

        if (existing != null) {
            return Result(existing.value, null)
        }

        return Result(null, UndefinedReferenceError(
            "'${identifier}' was not found",
            start,
            end,
            context
        ))
    }

    fun set(symbol: Symbol, context: Context, declaration: Boolean = true, forced: Boolean = false): Result {
        if (setOverride != null) {
            return this.setOverride!!(symbol)
        }

        if (declaration) {
            this.symbols.firstOrNull { it.name == symbol.name }?.let {
                return Result(null, DualDeclarationError(
                    "${it.name} was already defined",
                    it.start,
                    it.end,
                    context
                ))
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

    fun setGet(get: (Table.(String, Position, Position, Context) -> Result)): Table {
        this.getOverride = get

        return this
    }

    fun setSet(set: (Table.(Symbol) -> Result)): Table {
        this.setOverride = set

        return this
    }

}