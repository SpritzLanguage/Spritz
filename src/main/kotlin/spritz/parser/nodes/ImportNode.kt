package spritz.parser.nodes

import spritz.lexer.position.Position
import spritz.lexer.token.Token
import spritz.parser.node.Node

/**
 * @author surge
 * @since 26/03/2023
 */
class ImportNode(val name: Token<*>, val identifier: Token<*>, start: Position, end: Position) : Node(start, end) {

    override fun toString() = "(Import: $name, $identifier)"

}