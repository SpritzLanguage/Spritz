package spritz.util

import spritz.lexer.token.Token
import spritz.parser.node.Node

/**
 * @author surge
 * @since 27/02/2023
 */
data class Argument(val name: Token<*>, val type: Node?)
