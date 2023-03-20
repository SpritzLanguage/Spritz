package spritz

import spritz.error.Error
import spritz.value.Value
import spritz.warning.Warning

/**
 * @author surge
 * @since 19/03/2023
 */
data class EvaluationResult(val value: Value?, val warnings: List<Warning>, val error: Error?)