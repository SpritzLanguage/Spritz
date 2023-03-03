package spritz.value.symbols

import spritz.value.Value

/**
 * @author surge
 * @since 02/03/2023
 */
data class Symbol(val identifier: String, var value: Value, val data: SymbolData)