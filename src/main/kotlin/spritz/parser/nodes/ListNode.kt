package spritz.parser.nodes

import spritz.lexer.position.Position
import spritz.parser.node.Node

/**
 * @author surge
 * @since 26/02/2023
 */
class ListNode(val elements: List<Node>, start: Position, end: Position) : Node(start, end) {

    override fun toString() = "(ListNode: \n   ${elements}\n)"

}