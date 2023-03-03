package spritz.parser.nodes

import spritz.lexer.position.Position
import spritz.parser.node.Node
import spritz.util.Argument

/**
 * @author surge
 * @since 27/02/2023
 */
class TaskDefineNode(val identifier: String, val returnType: Node?, val arguments: List<Argument>, val body: ListNode, start: Position, end: Position) : Node(start, end) {

    override fun toString() = "(Task Define: $identifier($arguments) -> $returnType\n    $body\n)"

}