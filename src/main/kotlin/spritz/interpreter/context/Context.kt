package spritz.interpreter.context

import spritz.lexer.position.Position
import spritz.value.table.Table

/**
 * @author surge
 * @since 01/03/2023
 */
data class Context(val name: String, val parent: Context? = null, val parentEntryPosition: Position? = null) {

    lateinit var table: Table

    fun givenTable(table: Table): Context {
        this.table = table
        return this
    }

    fun getOrigin(): Context {
        if (this.parent == null) {
            return this
        }

        return this.parent.getOrigin()
    }

}