package spritz.parser.nodes

import spritz.api.Config
import spritz.lexer.position.Position
import spritz.lexer.token.Token
import spritz.parser.node.Node

/**
 * @author surge
 * @since 28/02/2023
 */
class ExternalNode(val path: Token<*>, val identifier: Token<*>, val config: Config, start: Position, end: Position) : Node(start, end) {

    override fun toString() = "(External: $path, $identifier, $config)"

}