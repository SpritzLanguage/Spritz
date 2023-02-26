package spritz.lexer.position

import spritz.interfaces.Cloneable

/**
 * @author surge
 * @since 25/02/2023
 */
data class Position(var name: String, var contents: String, var index: Int, var line: Int, var column: Int) : Cloneable {

    fun advance(character: Char? = null): Position {
        this.index++
        this.column++

        if (character == '\n') {
            this.line++
            this.column = 0
        }

        return this
    }

    override fun clone(): Position {
        return Position(name, contents, index, line, column)
    }

}
