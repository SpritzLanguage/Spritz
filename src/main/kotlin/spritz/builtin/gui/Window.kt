package spritz.builtin.gui

import spritz.api.annotations.Excluded
import spritz.interpreter.RuntimeResult
import spritz.value.task.DefinedTaskValue

/**
 * @author surge
 * @since 12/03/2023
 */
class Window(@Excluded val render: DefinedTaskValue?, var width: Int, val height: Int) {

    fun render() {
        while (true) {
            val error = render!!.execute(arrayListOf())

            if (error.error != null) {
                println(error.error)
                break
            }
        }
    }

}