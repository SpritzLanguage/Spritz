package spritz.parser.nodes

import spritz.lexer.position.Position
import spritz.lexer.token.Token
import spritz.parser.node.Node
import spritz.util.Argument

/**
 * @author surge
 * @since 04/03/2023
 */
class ClassDefineNode(val name: Token<*>, val constructor: List<Argument>, val body: Node?, start: Position, end: Position) : Node(start, end) {

    override fun toString() = "(Class Define: $name, $constructor, $body)"

}