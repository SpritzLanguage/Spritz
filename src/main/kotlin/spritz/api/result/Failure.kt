package spritz.api.result

import spritz.error.Error

class Failure(error: Error) : Result(null, error)