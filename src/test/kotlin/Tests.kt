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

    val parsingTestLexer = spritz.lex("parsing.sz", File("parsing.sz").readText(Charset.defaultCharset()))

    if (parsingTestLexer.second != null) {
        println(parsingTestLexer.second)
        return
    }

    val parsingTest = spritz.parse(parsingTestLexer.first)

    if (parsingTest.error != null) {
        println(parsingTest.error)
        return
    } else {
        println(parsingTest.node)
    }

    println("\n\n---------- INTERPRETING ----------")

    val interpretingTestLexer = spritz.lex("interpreting.sz", File("interpreting.sz").readText(Charset.defaultCharset()))

    if (interpretingTestLexer.second != null) {
        println(interpretingTestLexer.second)
        return
    }

    val interpretingTestParser = spritz.parse(interpretingTestLexer.first)

    if (interpretingTestParser.error != null) {
        println(interpretingTestParser.error)
        return
    }

    val interpretingTest = spritz.interpret(interpretingTestParser.node!!)

    if (interpretingTest.error != null) {
        println(interpretingTest.error)
        return
    } else {
        println(interpretingTest.value!!)
    }

}