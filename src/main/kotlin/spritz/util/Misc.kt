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
    "type" to "type"
)

fun keyword(input: String) = KEYWORDS.containsValue(input)