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
    val start: Position,
    val end: Position,
    val context: Context,
    val arguments: List<Value>,
    val instance: JvmTaskValue
)
