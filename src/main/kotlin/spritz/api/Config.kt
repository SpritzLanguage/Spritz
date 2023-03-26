package spritz.api

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
    val natives: Boolean = true
)