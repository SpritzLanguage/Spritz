package spritz.parser

import spritz.error.Error
import spritz.parser.node.Node
import spritz.warning.Warning

/**
 * @author surge
 * @since 26/02/2023
 */
class ParseResult {

    var node: Node? = null
    var error: Error? = null

    var advancement = 0
    var reverse = 0
    var lastRegisteredAdvanceCount = 0

    val warnings = mutableListOf<Warning>()

    fun register(result: ParseResult): Node? {
        this.lastRegisteredAdvanceCount = result.advancement
        this.advancement += result.advancement
        this.warnings.addAll(result.warnings)

        if (result.error != null) {
            this.error = result.error
        }

        return result.node
    }

    fun registerAdvancement() {
        this.advancement++
        this.lastRegisteredAdvanceCount = 1
    }

    fun tryRegister(result: ParseResult): Any? {
        if (result.error != null) {
            this.reverse = result.reverse
            return null
        }

        return this.register(result)
    }

    fun success(node: Node): ParseResult {
        this.node = node
        return this
    }

    fun warn(warning: Warning): ParseResult {
        this.warnings.add(warning)
        return this
    }

    fun failure(error: Error): ParseResult {
        if (this.error == null || this.lastRegisteredAdvanceCount == 0) {
            this.error = error
        }

        return this
    }

}