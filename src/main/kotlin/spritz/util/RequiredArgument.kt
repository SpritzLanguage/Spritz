package spritz.util

import spritz.lexer.token.Token
import spritz.value.Value

/**
 * @author surge
 * @since 03/03/2023
 */
data class RequiredArgument(val name: Token<*>, val type: Value?)