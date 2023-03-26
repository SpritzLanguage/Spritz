package spritz.builtin.companions

import spritz.api.annotations.Excluded
import spritz.value.Value

/**
 * This class is intended to serve as the backbone for every value, an includes any methods that may be needed for every value.
 *
 * @author surge
 * @since 26/03/2023
 */
open class Companion(@Excluded @JvmField val value: Value)