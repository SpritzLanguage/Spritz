package spritz.api

import spritz.interpreter.context.Context
import spritz.lexer.position.Position
import spritz.value.Value
import spritz.value.task.JvmTaskValue


/**
 * @author surge
 * @since 04/03/2023
 */
data class CallData(
    /**
     * The start of where this task was called from.
     */
    val start: Position,

    /**
     * The end of where this task was called from.
     */
    val end: Position,

    /**
     * The context of where this task was called from.
     */
    val context: Context,

    /**
     * The passed arguments.
     */
    val arguments: List<Value>,

    /**
     * An instance of the JVM task.
     */
    val instance: JvmTaskValue
)
