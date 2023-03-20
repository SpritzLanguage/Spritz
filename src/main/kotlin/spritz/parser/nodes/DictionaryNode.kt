package spritz.parser.nodes

import spritz.lexer.position.Position
import spritz.lexer.token.Token
import spritz.parser.node.Node

/**
 * @author surge
 * @since 26/02/2023
 */
class DictionaryNode(val elements: HashMap<String, Node>, start: Position, end: Position) : Node(start, end) {

    override fun toString(): String {
        var result = "(DictionaryNode: ["

        elements.forEach {
            result += "\n$it"
        }

        if (elements.isNotEmpty()) {
            result += '\n'
        }

        result += "])"
        return result
    }

}