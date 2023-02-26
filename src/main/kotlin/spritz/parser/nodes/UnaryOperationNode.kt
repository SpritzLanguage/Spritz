package spritz.parser.nodes

import spritz.lexer.token.Token
import spritz.parser.node.Node

/**
 * @author surge
 * @since 26/02/2023
 */
class UnaryOperationNode(val operator: Token<*>, val value: Node) : Node(operator.start, value.end) {

    override fun toString() = "(Unary Operation: $operator, $value)"

}