package spritz.value.table.result

import spritz.error.Error
import spritz.value.Value

/**
 * Holds a [value] and an [error] of a find or set operation.
 *
 * @author surge
 * @since 18/03/2023
 */
data class Result(val value: Value?, val error: Error?)