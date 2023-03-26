package spritz.value.table

import spritz.error.interpreting.DualDeclarationError
import spritz.error.interpreting.UndefinedReferenceError
import spritz.interpreter.context.Context
import spritz.lexer.position.LinkPosition
import spritz.lexer.position.Position
import spritz.value.Value
import spritz.value.table.result.Result

/**
 * Used for interacting with tables.
 *
 * @author surge
 * @since 24/03/2023
 */
class TableAccessor(private val table: Table) {

    // the identifier we want to fetch
    private var identifier: String? = null

    // any filter we want to apply
    private var predicate: (Value?) -> Boolean = { true }

    // if we only want to get a value from the top level of symbols, and exclude parents.
    private var top: Boolean = false

    // if we want to set the value to be immutable
    private var immutable: Boolean = false

    /**
     * Sets the [identifier] we want to fetch.
     * @return This instance.
     */
    fun identifier(identifier: String?): TableAccessor {
        this.identifier = identifier
        return this
    }

    /**
     * Sets the filter to apply.
     * @return This instance.
     */
    fun predicate(predicate: (Value?) -> Boolean): TableAccessor {
        this.predicate = predicate
        return this
    }

    /**
     * Sets the [top] state.
     * @return This instance.
     */
    fun top(top: Boolean): TableAccessor {
        this.top = top
        return this
    }

    /**
     * Sets the [immutable] state.
     * @return This instance.
     */
    fun immutable(immutable: Boolean): TableAccessor {
        this.immutable = immutable
        return this
    }

    /**
     * Finds the value with the filters that have been applied.
     * @return A result which either contains a [Value] or a [spritz.error.Error].
     */
    fun find(start: Position = LinkPosition(), end: Position = LinkPosition(), context: Context = Context("link")): Result {
        if (table.getOverride != null) {
            return table.getOverride!!(table, identifier, predicate, top, Table.Data(start, end, context))
        }

        val existing = table.symbols.filter {
            if (identifier != null && identifier != it.name) {
                return@filter false
            }

            return@filter predicate(it.value)
        }.firstOrNull()

        if (existing != null) {
            return Result(existing.value, null)
        }

        if (table.parent != null && !top) {
            return TableAccessor(table.parent)
                .identifier(this.identifier)
                .predicate(this.predicate)
                .top(this.top)
                .find(start, end, context)
        }

        return Result(null, UndefinedReferenceError(
            "'${identifier}' was not found",
            start,
            end,
            context
        ))
    }

    /**
     * Finds and sets the value with the filters that have been applied.
     * @return A result which either contains a [Value] or a [spritz.error.Error].
     */
    fun set(value: Value, declaration: Boolean = true, forced: Boolean = false, data: Table.Data = Table.Data(LinkPosition(), LinkPosition(), Context("link"))): Result {
        val symbol = Symbol(this.identifier!!, value, data.start, data.end)

        if (table.setOverride != null) {
            return table.setOverride!!(table, symbol)
        }

        if (declaration) {
            table.symbols.firstOrNull { it.name == symbol.name && this.predicate.invoke(it.value) }?.let {
                if (forced) {
                    it.value = symbol.value
                    return Result(symbol.value, null)
                } else {
                    return Result(
                        null, DualDeclarationError(
                            "${it.name} was already defined",
                            it.start,
                            it.end,
                            data.context
                        )
                    )
                }
            }

            table.symbols.add(symbol)

            return Result(symbol.value, null)
        }

        table.symbols.firstOrNull { it.name == symbol.name && predicate.invoke(it.value) }?.let {
            it.value = symbol.value
            return Result(symbol.value, null)
        }

        if (table.parent != null) {
            return TableAccessor(table.parent)
                .identifier(symbol.name)
                .predicate(this.predicate)
                .set(value, declaration, forced, data)
        }

        return Result(
            null,
            UndefinedReferenceError(
                "'${symbol.name}' was not found in the current scope",
                symbol.start,
                symbol.end,
                data.context
            )
        )
    }

}