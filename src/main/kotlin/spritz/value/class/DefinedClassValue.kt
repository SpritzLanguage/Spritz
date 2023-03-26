package spritz.value.`class`

import spritz.SpritzEnvironment
import spritz.builtin.companions.ClassCompanion
import spritz.interpreter.Interpreter
import spritz.interpreter.RuntimeResult
import spritz.interpreter.context.Context
import spritz.lexer.position.Position
import spritz.parser.node.Node
import spritz.util.RequiredArgument
import spritz.value.Value
import spritz.value.table.Table
import spritz.value.table.TableAccessor

/**
 * @author surge
 * @since 04/03/2023
 */
class DefinedClassValue(identifier: String, val constructor: List<RequiredArgument>, val body: Node?) : ClassValue(identifier = identifier, identifier) {

    init {
        SpritzEnvironment.putIntoTable(ClassCompanion(this), this.table, Context("companion"))
    }

    override fun asJvmValue() = this

    override fun execute(passed: List<Value>, start: Position, end: Position, context: Context): RuntimeResult {
        val result = RuntimeResult()
        val interpreter = Interpreter()

        // generate new context
        val instanceContext = generateExecuteContext()

        // check and populate arguments
        result.register(this.checkAndPopulate(constructor, passed, start, end, instanceContext))

        if (result.shouldReturn()) {
            return result
        }

        // table that holds any tasks and variables inside the instance.
        val table = Table()

        if (body != null) {
            result.register(interpreter.visit(this.body, instanceContext))

            if (result.error != null) {
                return result
            }
        }

        // add symbols to table
        table.symbols.addAll(instanceContext.table.symbols)

        // generate instance
        val instance = DefinedInstanceValue(this, table)

        TableAccessor(instance.table)
            .identifier("this")
            .immutable(true)
            .set(instance, forced = true, data = Table.Data(this.start, this.end, context))

        return result.success(instance.position(start, end).givenContext(instanceContext))
    }

    override fun toString() = super.toString().ifEmpty { "($identifier)" }

}