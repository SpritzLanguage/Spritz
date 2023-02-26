package spritz.parser.node

import spritz.lexer.position.Position

/**
 * @author surge
 * @since 26/02/2023
 */
abstract class Node(val start: Position, val end: Position) {

    abstract override fun toString(): String

}