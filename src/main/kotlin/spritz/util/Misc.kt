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
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.kotlinProperty

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
fun Class<*>.coercedName(): String = this.getAnnotation(Identifier::class.java)?.identifier ?: this.simpleName
fun Enum<*>.coercedName(): String = this.declaringClass.getField(this.name).getAnnotation(Identifier::class.java)?.identifier ?: this.name

fun Class<*>.getAllFields(): List<Field> {
    val fields = mutableListOf<Field>()

    this.declaredFields.forEach {
        if (it.isSynthetic) {
            return@forEach
        }

        it.isAccessible = true

        fields.add(it)
    }

    if (this.superclass != null && this.superclass != Any::class.java) {
        fields.addAll(this.superclass.getAllFields())
    }

    return fields
}

fun Class<*>.getAllMethods(): List<Method> {
    val methods = mutableListOf<Method>()

    this.declaredMethods.forEach {
        if (it.isSynthetic) {
            return@forEach
        }

        it.isAccessible = true

        methods.add(it)
    }

    if (this.superclass != null && this.superclass != Any::class.java) {
        methods.addAll(this.superclass.getAllMethods())
    }

    return methods
}

inline fun <T> Array<out T>.allIndexed(predicate: (Int, T) -> Boolean): Boolean {
    this.forEachIndexed { index, element ->
        if (!predicate(index, element)) {
            return false
        }
    }

    return true
}

fun Value.success(): Success {
    return Success(this)
}

val KEYWORDS = hashMapOf(
    // declaration keywords
    "task" to "task",
    "lambda" to "lambda",
    "class" to "class",
    "enum" to "enum",
    "mutable" to "mut",
    "constant" to "const",
    "native" to "native",
    "as" to "as",
    "is" to "is",
    "import" to "import",

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