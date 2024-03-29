package spritz.parser.nodes

import spritz.lexer.position.Position
import spritz.lexer.token.Token
import spritz.parser.node.Node
import spritz.util.Argument

/**
 * @author surge
 * @since 04/03/2023
 */
class EnumDefineNode(val name: Token<*>, val constructor: List<Argument>, val members: LinkedHashMap<Token<*>, List<Node>>, val body: Node?, start: Position, end: Position) : Node(start, end) {

    override fun toString() = "(Enum Define: $name, $constructor, $members, $body)"

}