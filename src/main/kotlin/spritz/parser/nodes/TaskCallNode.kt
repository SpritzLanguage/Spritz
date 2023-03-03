package spritz.parser.nodes

import spritz.lexer.position.Position
import spritz.parser.node.Node
import spritz.util.CallArgument

/**
 * @author surge
 * @since 03/03/2023
 */
class TaskCallNode(val target: Node, val arguments: List<CallArgument>, start: Position, end: Position) : Node(start, end) {

    override fun toString() = "(Task Call: $target, $arguments)"

}