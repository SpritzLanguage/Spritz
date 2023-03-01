package spritz.parser.nodes

import spritz.lexer.position.Position
import spritz.parser.node.Node

/**
 * @author surge
 * @since 28/02/2023
 */
class BreakNode(start: Position, end: Position) : Node(start, end) {

    override fun toString() = "(break)"

}