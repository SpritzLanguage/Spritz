package spritz.parser.nodes

import spritz.lexer.position.Position
import spritz.lexer.token.Token
import spritz.parser.node.Node

/**
 * @author surge
 * @since 07/03/2023
 */
class WhileNode(val expression: Node, val body: Node, start: Position, end: Position) : Node(start, end) {

    override fun toString() = "(while, $expression, $body)"

}