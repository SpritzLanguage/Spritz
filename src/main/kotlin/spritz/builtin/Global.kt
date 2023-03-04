package spritz.builtin

import spritz.api.annotations.Identifier

/**
 * @author surge
 * @since 04/03/2023
 */
object Global {

    @Identifier("true")
    const val TRUE = true

    @Identifier("false")
    const val FALSE = false

    @Identifier("null")
    @JvmField val NULL = null

}