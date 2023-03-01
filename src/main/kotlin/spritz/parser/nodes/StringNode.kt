package spritz.parser.nodes

import spritz.lexer.position.Position
import spritz.lexer.token.Token
import spritz.parser.node.Node

/**
 * @author surge
 * @since 01/03/2023
 */
class StringNode(val value: Token<*>, start: Position, end: Position) : Node(start, end) {

    override fun toString() = "(String: $value)"

}