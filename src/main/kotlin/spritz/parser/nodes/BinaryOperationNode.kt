package spritz.parser.nodes

import spritz.lexer.position.Position
import spritz.lexer.token.Token
import spritz.parser.node.Node

/**
 * @author surge
 * @since 26/02/2023
 */
class BinaryOperationNode(val left: Node, val operator: Token<*>, val right: Node) : Node(left.start, right.end) {

    override fun toString() = "(Binary Operation: $left, $operator, $right)"

}