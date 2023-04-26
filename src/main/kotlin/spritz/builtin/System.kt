package spritz.builtin

import spritz.api.annotations.Identifier
import java.lang.System

/**
 * @author surge
 * @since 26/04/2023
 */
object System {

    @Identifier("current_time_millis")
    fun currentTimeMillis(): Long = System.currentTimeMillis()

}