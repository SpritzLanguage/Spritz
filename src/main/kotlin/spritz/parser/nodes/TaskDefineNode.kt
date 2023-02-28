package spritz.parser.nodes

import spritz.lexer.position.Position
import spritz.parser.node.Node
import spritz.util.Argument

/**
 * @author surge
 * @since 27/02/2023
 */
class TaskDefineNode(val name: String, val returnType: String?, val arguments: List<Argument>, val body: ListNode, start: Position, end: Position) : Node(start, end) {

    override fun toString() = "(Task Define: $name($arguments) -> $returnType\n    $body\n)"

}