package spritz.util

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

val KEYWORDS = hashMapOf(
    // declaration keywords
    "task" to "task",
    "mutable" to "mut",
    "constant" to "const",

    // branch control statements
    "return" to "return",
    "continue" to "continue",
    "break" to "break"
)

val TYPES = hashMapOf(
    // inbuilt types
    "int" to "int",
    "float" to "float",
    "string" to "string",
    "boolean" to "bool",
    "list" to "list",
    "dictionary" to "dict",
    "byte" to "byte"
)

fun keyword(input: String) = KEYWORDS.containsValue(input) || TYPES.containsValue(input)
fun type(input: String) = TYPES.containsValue(input)