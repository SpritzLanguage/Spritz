package spritz.util

import spritz.api.annotations.Identifier
import spritz.lexer.token.TokenType
import spritz.lexer.token.TokenType.*
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * @author surge
 * @since 25/02/2023
 */
operator fun String.times(amount: Int): String {
    var result = ""

    for (i in 0..amount) {
        result += this
    }

    return result
}

fun Field.coercedName(): String = this.getAnnotation(Identifier::class.java)?.identifier ?: this.name
fun Method.coercedName(): String = this.getAnnotation(Identifier::class.java)?.identifier ?: this.name

val KEYWORDS = hashMapOf(
    // declaration keywords
    "task" to "task",
    "container" to "container",
    "mutable" to "mut",
    "constant" to "const",
    "external" to "external",
    "as" to "as",

    // loops
    "for" to "for",
    "while" to "while",

    // branch control statements
    "conditional" to "if",
    "conditional_default" to "else",
    "return" to "return",
    "continue" to "continue",
    "break" to "break"
)

val TYPES = hashMapOf(
    // inbuilt types
    "int" to "int",
    "float" to "float",
    "string" to "string",
    "boolean" to "boolean",
    "list" to "list",
    "dictionary" to "dictionary",
    "byte" to "byte"
)

fun keyword(input: String) = KEYWORDS.containsValue(input) || TYPES.containsValue(input)
fun type(input: String) = TYPES.containsValue(input)

fun unary(type: TokenType) = type == NEGATE || type == BIN_COMPLEMENT
fun modifier(type: TokenType) = type == ASSIGNMENT || type == INCREMENT || type == DEINCREMENT || type == INCREASE_BY || type == DECREASE_BY || type == MULTIPLY_BY || type == DIVIDE_BY