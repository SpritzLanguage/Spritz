package spritz.parser.nodes

import spritz.lexer.position.Position
import spritz.lexer.token.Token
import spritz.parser.node.Node

/**
 * @author surge
 * @since 26/02/2023
 */
class AssignmentNode(val name: Token<*>, val value: Node, start: Position, end: Position) : Node(start, end) {

    override fun toString() = "(Assignment: ${name.value!!}, $value)"

}