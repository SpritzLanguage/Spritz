package spritz.parser.nodes

import spritz.lexer.token.Token
import spritz.parser.node.Node
import spritz.value.Value

/**
 * @author surge
 * @since 01/03/2023
 */
class AccessNode(val identifier: Token<*>) : Node(identifier.start, identifier.end) {

    var predicate: (Value) -> Boolean = { true }

    fun setPredicate(predicate: (Value) -> Boolean): AccessNode {
        this.predicate = predicate
        return this
    }

    override fun toString() = "(Access: $identifier)::($child)"

}