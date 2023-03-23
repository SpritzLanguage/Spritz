package spritz.value.container

import spritz.interpreter.Interpreter
import spritz.interpreter.RuntimeResult
import spritz.interpreter.context.Context
import spritz.lexer.position.Position
import spritz.parser.node.Node
import spritz.util.RequiredArgument
import spritz.value.Value
import spritz.value.table.Symbol
import spritz.value.table.Table
import spritz.value.task.TaskValue

/**
 * @author surge
 * @since 04/03/2023
 */
class DefinedContainerValue(identifier: String, val constructor: List<RequiredArgument>, val body: Node?) : TaskValue(identifier = identifier, identifier) {

    override fun asJvmValue() = this

    override fun execute(passed: List<Value>, start: Position, end: Position, context: Context): RuntimeResult {
        val result = RuntimeResult()
        val interpreter = Interpreter()

        val instanceContext = Context(identifier)

        instanceContext.table = Table(context.getOrigin().table)

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

        instance.table.set(Symbol("this", instance, this.start, this.end).setImmutability(true), context, true)

        return result.success(instance.positioned(start, end).givenContext(instanceContext))
    }

    override fun toString() = super.toString().ifEmpty { "($identifier)" }

}