package spritz.value.table

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
class TableFinder(private val table: Table) {

    private var identifier: String? = null
    private var filter: (Value?) -> Boolean = { true }
    private var top: Boolean = false

    fun identifier(identifier: String?): TableFinder {
        this.identifier = identifier
        return this
    }

    fun filter(filter: (Value?) -> Boolean): TableFinder {
        this.filter = filter
        return this
    }

    fun top(top: Boolean): TableFinder {
        this.top = top
        return this
    }

    fun find(start: Position = LinkPosition(), end: Position = LinkPosition(), context: Context = Context("link")): Result {
        if (table.getOverride != null) {
            return table.getOverride!!(table, identifier, filter, top, Table.Data(start, end, context))
        }

        val existing = table.symbols.filter {
            if (identifier != null && identifier != it.name) {
                return@filter false
            }

            return@filter filter(it.value)
        }.firstOrNull()

        if (existing != null) {
            return Result(existing.value, null)
        }

        if (table.parent != null && !top) {
            return TableFinder(table.parent)
                .identifier(this.identifier)
                .filter(this.filter)
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

}