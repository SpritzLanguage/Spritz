package spritz.util

import spritz.api.annotations.Identifier
import spritz.api.result.Failure
import spritz.api.result.Success
import spritz.lexer.token.TokenType
import spritz.lexer.token.TokenType.*
import spritz.value.Value
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

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

fun Class<*>.getAllFields(): List<Field> {
    val fields = mutableListOf<Field>()

    this.declaredFields.forEach {
        it.isAccessible = true

        fields.add(it)
    }

    if (this.superclass != null) {
        fields.addAll(this.superclass.getAllFields())
    }

    return fields
}

fun Class<*>.getAllMethods(): List<Method> {
    val methods = mutableListOf<Method>()

    this.declaredMethods.forEach {
        it.isAccessible = true

        methods.add(it)
    }

    if (this.superclass != null) {
        methods.addAll(this.superclass.getAllMethods())
    }

    return methods
}

fun Value.success(): Success {
    return Success(this)
}

val KEYWORDS = hashMapOf(
    // declaration keywords
    "task" to "task",
    "class" to "class",
    "mutable" to "mut",
    "constant" to "const",
    "as" to "as",
    "is" to "is",

    // loops
    "for" to "for",
    "while" to "while",

    // branch control statements
    "conditional" to "if",
    "conditional_default" to "else",
    "return" to "return",
    "continue" to "continue",
    "break" to "break",

    // try catch
    "try" to "try",
    "catch" to "catch"
)

val NUMBERS = listOf(
    "int",
    "float",
    "number"
)

val ANONYMOUS = "<anonymous>"

fun keyword(input: String) = KEYWORDS.containsValue(input)

fun unary(type: TokenType) = type == NEGATE || type == BIN_COMPLEMENT
fun modifier(type: TokenType) = type == ASSIGNMENT || type == INCREMENT || type == DEINCREMENT || type == INCREASE_BY || type == DECREASE_BY || type == MULTIPLY_BY || type == DIVIDE_BY