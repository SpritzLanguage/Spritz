package spritz.api

import kotlin.system.exitProcess

/**
 * @author surge
 * @since 02/04/2023
 */
object StandardOverride {

    var input: () -> String = {
        readlnOrNull() ?: ""
    }

    var output: (Any) -> Unit = {
        print(it)
    }

    var outputln: (Any) -> Unit = {
        println(it)
    }

    var exit: (Int) -> Unit = {
        exitProcess(it)
    }

}