import spritz.Spritz
import spritz.value.bool.BoolValue
import spritz.value.list.ListValue
import spritz.value.task.DefinedTaskValue
import java.io.File
import java.nio.charset.Charset

/**
 * @author surge
 * @since 25/02/2023
 */
fun main() {
    val spritz = Spritz()
        .loadStandard()

    // individual_tests/importing/main.sz

    val test = File("individual_tests/importing/main.sz")

    if (!test.exists()) {
        println("Test not found")
        return
    }

    val result = spritz.evaluate(test.nameWithoutExtension, test.readText(Charset.defaultCharset()))

    if (result != null) {
        println(result)
        return
    }

    val main = (spritz.globalTable.get("main").value as DefinedTaskValue).execute(arrayListOf())

    if (main.error != null) {
        println(main.error)
    }
}

fun baseTests(spritz: Spritz) {
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

    interpretingTestParser.warnings.forEach {
        println(it)
    }

    val interpretingTest = spritz.interpret(interpretingTestParser.node!!)

    if (interpretingTest.first.error != null) {
        println(interpretingTest.first.error)
        return
    }

    val main = interpretingTest.second.get("main").value as DefinedTaskValue

    val result = main.execute(arrayListOf())

    if (result.error != null) {
        println(result.error)
        return
    }
}