package spritz.value.symbols.result

import spritz.error.Error
import spritz.value.Value

/**
 * @author surge
 * @since 03/03/2023
 */
data class GetResult(val value: Value?, val error: Error?)