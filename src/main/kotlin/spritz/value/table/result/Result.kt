package spritz.value.table.result

import spritz.error.Error
import spritz.value.Value

/**
 * @author surge
 * @since 18/03/2023
 */
data class Result(val value: Value?, val error: Error?)