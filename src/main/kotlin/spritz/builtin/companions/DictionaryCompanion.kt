package spritz.builtin.companions

import spritz.api.CallData
import spritz.api.annotations.Excluded
import spritz.api.result.Failure
import spritz.api.result.Result
import spritz.api.result.Success
import spritz.error.interpreting.MemberNotFoundError
import spritz.util.success
import spritz.value.NullValue
import spritz.value.Value
import spritz.value.dictionary.DictionaryValue
import spritz.value.string.StringValue

/**
 * @author surge
 * @since 20/03/2023
 */
class DictionaryCompanion(@Excluded val dictionary: DictionaryValue) {

    fun set(key: StringValue, value: Value) {
        this.dictionary.elements[key.value] = value
    }

    fun get(data: CallData, key: StringValue): Result {
        return dictionary.elements[key.value]?.success() ?: Failure(MemberNotFoundError(
            "'${key.value}' was not found",
            data.start,
            data.end,
            data.context
        ))
    }

    fun remove(data: CallData, key: StringValue): Result {
        return dictionary.elements.remove(key.value)?.success() ?: Failure(MemberNotFoundError(
            "'${key.value}' was not found",
            data.start,
            data.end,
            data.context
        ))
    }

    fun length(): Int {
        return dictionary.elements.size
    }

}