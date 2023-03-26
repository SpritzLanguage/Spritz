package spritz.api.result

import spritz.error.Error

/**
 * Represents a failure to execute a function.
 */
class Failure(error: Error) : Result(null, error)