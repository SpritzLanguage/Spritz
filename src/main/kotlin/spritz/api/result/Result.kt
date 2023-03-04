package spritz.api.result

import spritz.error.Error
import spritz.value.Value

open class Result(val value: Value?, val error: Error?)