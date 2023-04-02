import jvmlink.ClassTesting
import jvmlink.ClassWithEnum
import spritz.SpritzEnvironment
import spritz.api.Config
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
        val spritzEnvironment = SpritzEnvironment(Config(debug = false))
            .putClass("ClsWithEnum", ClassWithEnum::class.java)
            .putInstance("ClassTesting", ClassTesting())

            .setWarningHandler {
                println(it)
            }
            .setErrorHandler {
                println(it)
            }

        spritzEnvironment.evaluate("safe_call.sz", File("examples/safe_call.sz").readText(Charset.defaultCharset()))

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