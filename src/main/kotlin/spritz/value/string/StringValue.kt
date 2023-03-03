package spritz.value.string

import spritz.error.Error
import spritz.lexer.token.Token
import spritz.value.PrimitiveReferenceValue
import spritz.value.Value

/**
 * @author surge
 * @since 03/03/2023
 */
class StringValue(val value: String) : Value("string") {

    override fun plus(other: Value, operator: Token<*>): Pair<Value?, Error?> {
        return Pair(StringValue(value + other.toString()), null)
    }

    override fun matchesType(type: Value) = super.matchesType(type) || type is PrimitiveReferenceValue && type.type == "string"
    override fun toString() = value

}