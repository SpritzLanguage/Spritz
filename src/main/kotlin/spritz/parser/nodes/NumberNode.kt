package spritz.parser.nodes

import spritz.lexer.token.Token
import spritz.parser.node.Node

/**
 * @author surge
 * @since 26/02/2023
 */
class NumberNode(val token: Token<*>) : Node(token.start, token.end) {

    override fun toString() = "(Number: $token)"

}