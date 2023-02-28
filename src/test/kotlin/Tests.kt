import spritz.Spritz
import java.io.File
import java.nio.charset.Charset

/**
 * @author surge
 * @since 25/02/2023
 */
fun main() {
    val spritz = Spritz()

    println("---------- LEXING ----------")

    val lexingTest = spritz.lex("lexing.sz", File("lexing.sz").readText(Charset.defaultCharset()))

    if (lexingTest.second != null) {
        println(lexingTest.second)
        return
    } else {
        println(lexingTest.first)
    }

    println("\n\n---------- PARSING ----------")

    val parsingTest = spritz.parse(spritz.lex("parsing.sz", File("parsing.sz").readText(Charset.defaultCharset())).first)

    if (parsingTest.error != null) {
        println(parsingTest.error)
        return
    } else {
        println(parsingTest.node)
    }

}