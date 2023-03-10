package spritz.value.container

import spritz.interpreter.Interpreter
import spritz.interpreter.RuntimeResult
import spritz.interpreter.context.Context
import spritz.lexer.position.Position
import spritz.parser.node.Node
import spritz.util.RequiredArgument
import spritz.value.Value
import spritz.value.symbols.Symbol
import spritz.value.symbols.SymbolData
import spritz.value.symbols.Table
import spritz.value.task.TaskValue

/**
 * @author surge
 * @since 04/03/2023
 */
class DefinedContainerValue(identifier: String, val constructor: List<RequiredArgument>, val body: Node?) : TaskValue(identifier = identifier, "container") {

    override fun execute(passed: List<Value>, start: Position, end: Position, context: Context): RuntimeResult {
        val result = RuntimeResult()
        val interpreter = Interpreter()

        val instanceContext = Context(identifier)

        instanceContext.table = Table()

        result.register(this.checkAndPopulate(constructor, passed, start, end, instanceContext))

        if (result.shouldReturn()) {
            return result
        }

        val table = Table()

        if (body != null) {
            result.register(interpreter.visit(this.body, instanceContext))

            if (result.error != null) {
                return result
            }
        }

        table.symbols.addAll(instanceContext.table.symbols)
        val instance = InstanceValue(this, table)

        instance.table.set(Symbol("this", instance, SymbolData(immutable = true, this.start, this.end)), context, true)

        return result.success(instance.positioned(start, end).givenContext(instanceContext))
    }

    override fun toString() = "($identifier)"

}