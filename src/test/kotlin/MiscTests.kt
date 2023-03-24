import spritz.SpritzEnvironment
import spritz.builtin.Global
import spritz.builtin.Standard
import spritz.value.table.TableFinder
import spritz.value.task.DefinedTaskValue
import java.io.File
import java.nio.charset.Charset

/**
 * @author surge
 * @since 16/03/2023
 */
object MiscTests {

    @JvmStatic
    fun main(args: Array<String>) {
        val spritzEnvironment = SpritzEnvironment()
            .setWarningHandler {
                println(it)
            }
            .setErrorHandler {
                println(it)
            }

        spritzEnvironment.evaluate("testing", File("testing.sz").readText(Charset.defaultCharset()))

        /* val main = spritzEnvironment.get("main") as DefinedTaskValue
        val result = main.execute(arrayListOf())

        if (result.error != null) {
            println(result.error)
        } */

        val main = TableFinder(spritzEnvironment.global)
            .identifier("main")
            .filter { it is DefinedTaskValue && it.arguments.isEmpty() }
            .find().value?.execute(arrayListOf())?.also {
                if (it.error != null) {
                    println(it.error)
                }
            } ?: run {
                println("`main` function not found")
            }
    }

}