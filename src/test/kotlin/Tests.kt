import spritz.Spritz
import java.io.File
import java.nio.charset.Charset

/**
 * @author surge
 * @since 25/02/2023
 */
fun main() {
    val spritz = Spritz()

    val lexingTest = spritz.lex("lexing.sz", File("lexing.sz").readText(Charset.defaultCharset()))

    if (lexingTest.second != null) {
        println(lexingTest.second)
        return
    } else {
        println(lexingTest.first)
    }
}