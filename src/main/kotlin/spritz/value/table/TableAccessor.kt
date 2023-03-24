package spritz.value.table

import spritz.error.interpreting.DualDeclarationError
import spritz.error.interpreting.UndefinedReferenceError
import spritz.interpreter.context.Context
import spritz.lexer.position.LinkPosition
import spritz.lexer.position.Position
import spritz.value.Value
import spritz.value.table.result.Result

/**
 * @author surge
 * @since 24/03/2023
 */
class TableAccessor(private val table: Table) {

    private var identifier: String? = null
    private var predicate: (Value?) -> Boolean = { true }
    private var top: Boolean = false
    private var immutable: Boolean = false

    fun identifier(identifier: String?): TableAccessor {
        this.identifier = identifier
        return this
    }

    fun predicate(predicate: (Value?) -> Boolean): TableAccessor {
        this.predicate = predicate
        return this
    }

    fun top(top: Boolean): TableAccessor {
        this.top = top
        return this
    }

    fun immutable(immutable: Boolean): TableAccessor {
        this.immutable = immutable
        return this
    }

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