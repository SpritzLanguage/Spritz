package spritz.api.result

import spritz.error.Error
import spritz.value.Value

/**
 * Represents a result of executing a function.
 */
open class Result(val value: Value?, val error: Error?)