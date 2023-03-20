package spritz.builtin.companions

import spritz.api.annotations.Excluded
import spritz.value.NullValue
import spritz.value.Value
import spritz.value.dictionary.DictionaryValue
import spritz.value.string.StringValue

/**
 * @author surge
 * @since 20/03/2023
 */
class DictionaryCompanion(@Excluded val dictionary: DictionaryValue) {

    fun get(key: StringValue): Value {
        return dictionary.elements[key.value] ?: NullValue()
    }

    fun remove(key: StringValue): Value {
        return dictionary.elements.remove(key.value) ?: NullValue()
    }

    fun length(): Int {
        return dictionary.elements.size
    }

}