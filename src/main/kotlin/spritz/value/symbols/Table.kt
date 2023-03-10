package spritz.value.symbols

import spritz.error.interpreting.DualDeclarationError
import spritz.error.interpreting.MemberNotFoundError
import spritz.error.interpreting.MutabilityAssignationError
import spritz.error.interpreting.UndefinedReferenceError
import spritz.interpreter.context.Context
import spritz.lexer.position.LinkPosition
import spritz.lexer.position.Position
import spritz.value.symbols.result.GetResult
import spritz.value.symbols.result.RemoveResult
import spritz.value.symbols.result.SetResult

/**
 * @author surge
 * @since 02/03/2023
 */
class Table(val parent: Table? = null) {

    val symbols = arrayListOf<Symbol>()

    fun get(identifier: String): GetResult {
        return get(identifier, LinkPosition(), LinkPosition(), Context(""))
    }

    fun get(identifier: String, start: Position, end: Position, context: Context): GetResult {
        if (this.symbols.any { it.identifier == identifier }) {
            return GetResult(this.symbols.first { it.identifier == identifier }.value, null)
        }

        if (this.parent != null) {
            return this.parent.get(identifier, start, end, context)
        }

        return GetResult(null, UndefinedReferenceError(
            "'$identifier'",
            start,
            end,
            context
        ))
    }

    fun set(symbol: Symbol, context: Context, declaration: Boolean, forced: Boolean = false): SetResult {
        if (symbols.any { it.identifier == symbol.identifier }) {
            return if (declaration && !forced) {
                SetResult(
                    null,
                    DualDeclarationError(
                        "'${symbol.identifier}' was already defined in the current scope",
                        symbol.data.start,
                        symbol.data.end,
                        context
                    )
                )
            } else {
                val existing = this.symbols.first { it.identifier == symbol.identifier }

                if (existing.data.immutable && !forced) {
                    return SetResult(null, MutabilityAssignationError(
                        "'${symbol.identifier}' is immutable!",
                        symbol.data.start,
                        symbol.data.end,
                        context
                    ))
                }

                existing.value = symbol.value
                SetResult(symbol.value)
            }
        }

        if (declaration) {
            this.symbols.add(symbol)
            return SetResult(symbol.value)
        }

        if (this.parent != null) {
            return this.parent.set(symbol, context, false)
        }

        return SetResult(
            null,
            UndefinedReferenceError(
                "'${symbol.identifier}' was not found in the current scope",
                symbol.data.start,
                symbol.data.end,
                context
            )
        )
    }

    fun remove(identifier: String, start: Position, end: Position, context: Context): RemoveResult {
        if (symbols.any { it.identifier == identifier }) {
            symbols.removeIf { it.identifier == identifier }
            return RemoveResult(null)
        }

        if (this.parent != null) {
            return this.parent.remove(identifier, start, end, context)
        }

        return RemoveResult(MemberNotFoundError(
            "'$identifier' was not found",
            start,
            end,
            context
        ))
    }

}