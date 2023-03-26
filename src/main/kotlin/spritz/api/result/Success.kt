package spritz.api.result

import spritz.value.Value

/**
 * Represents a successful execution of a function.
 */
class Success(value: Value? = null) : Result(value, null)