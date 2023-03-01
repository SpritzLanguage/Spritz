package spritz.parser.nodes

import spritz.lexer.token.Token
import spritz.parser.node.Node

/**
 * @author surge
 * @since 01/03/2023
 */
class AccessNode(val name: Token<*>) : Node(name.start, name.end) {

    override fun toString() = "(Access: $name)"

}