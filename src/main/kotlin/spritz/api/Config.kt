package spritz.api

import spritz.error.Error
import spritz.warning.Warning

/**
 * @author surge
 * @since 11/03/2023
 */
data class Config(
    /**
     * Whether to allow forced assignations or not
     */
    val forcedAssignations: Boolean = true,

    /**
     * Whether to load the standard library or not
     */
    val loadDefaults: Boolean = true,

    /**
     * Whether to allow native linking or not
     */
    val natives: Boolean = true,

    /**
     * Log debug messages
     */
    val debug: Boolean = false,

    /**
     * How to handle warnings
     */
    val warningStream: (Warning) -> Unit = {
        println(it)
    },

    /**
     * How to handle errors
     */
    val errorStream: (Error) -> Unit = {
        println(it)
    }
)