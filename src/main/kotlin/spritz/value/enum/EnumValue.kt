package spritz.value.`enum`

import spritz.value.Value
import spritz.value.table.TableAccessor

/**
 * @author surge
 * @since 26/03/2023
 */
class EnumValue(identifier: String, members: LinkedHashMap<String, Value>) : Value("enum", identifier = identifier) {

    init {
        members.forEach { (name, value) ->
            TableAccessor(this.table)
                .identifier(name)
                .immutable(true)
                .set(value, declaration = true)
        }
    }

    override fun asJvmValue() = this

}