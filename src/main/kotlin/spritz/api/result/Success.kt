package spritz.api.result

import spritz.value.Value

class Success(value: Value? = null) : Result(value, null)