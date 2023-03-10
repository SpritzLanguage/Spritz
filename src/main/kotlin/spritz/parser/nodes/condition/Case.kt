package spritz.parser.nodes.condition

import spritz.parser.node.Node

/**
 * @author surge
 * @since 10/03/2023
 */
open class Case(val condition: Node?, val node: Node, val `else`: Boolean = false) {

    override fun toString() = "(Case: $node)"

}