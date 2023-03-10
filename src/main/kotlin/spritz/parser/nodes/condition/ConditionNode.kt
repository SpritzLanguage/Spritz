package spritz.parser.nodes.condition

import spritz.parser.node.Node

/**
 * @author surge
 * @since 10/03/2023
 */
class ConditionNode(val cases: List<Case>) : Node(cases.first().node.start, cases.last().node.end) {

    override fun toString() = "(Condition: $cases)"

}