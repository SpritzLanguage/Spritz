package spritz

import spritz.error.Error
import spritz.value.Value
import spritz.warning.Warning

/**
 * Returned after an evaluation of a script has been performed.
 * Contains the [value] which can be supplemented at the end of a script,
 * as well as any [warnings] that have been produced, or an [error] that
 * has occurred during lexing, parsing, or interpreting.
 *
 * @author surge
 * @since 19/03/2023
 */
data class EvaluationResult(val value: Value?, val warnings: List<Warning>, val error: Error?)