import jvmlink.ClassTesting
import jvmlink.ClassWithEnum
import spritz.SpritzEnvironment
import spritz.api.Config
import spritz.value.table.TableAccessor
import spritz.value.task.DefinedTaskValue
import java.io.File

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

        spritzEnvironment.evaluate(File("examples/for_loop.sz"))

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