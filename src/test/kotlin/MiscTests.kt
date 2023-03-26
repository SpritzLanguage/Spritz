import spritz.SpritzEnvironment
import spritz.value.table.TableAccessor
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

        spritzEnvironment.evaluate("native_resolution.sz", File("demos/native_resolution.sz").readText(Charset.defaultCharset()))

        val main = TableAccessor(spritzEnvironment.global)
            .identifier("main")
            .predicate { it is DefinedTaskValue && it.arguments.isEmpty() }
            .find()
            .value
            ?.execute(arrayListOf())?.also {
                if (it.error != null) {
                    println(it.error)
                }
            } ?: run {
                println("`main` function not found")
            }
    }

}