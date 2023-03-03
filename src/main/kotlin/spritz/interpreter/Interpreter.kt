package spritz.interpreter

import spritz.error.Error
import spritz.error.interpreting.IllegalOperationError
import spritz.error.interpreting.NodeIntepreterNotFoundError
import spritz.interpreter.context.Context
import spritz.lexer.token.TokenType.*
import spritz.parser.node.Node
import spritz.parser.nodes.*
import spritz.util.Argument
import spritz.util.PassedArgument
import spritz.util.RequiredArgument
import spritz.util.type
import spritz.value.NullValue
import spritz.value.PrimitiveReferenceValue
import spritz.value.Value
import spritz.value.list.ListValue
import spritz.value.number.FloatValue
import spritz.value.number.IntValue
import spritz.value.string.StringValue
import spritz.value.symbols.Symbol
import spritz.value.symbols.SymbolData
import spritz.value.task.DefinedTaskValue

/**
 * @author surge
 * @since 01/03/2023
 */
class Interpreter {

    private val visitors = mapOf(
        // operations
        BinaryOperationNode::class.java to { node: Node, context: Context -> binaryOperation(node as BinaryOperationNode, context) },
        UnaryOperationNode::class.java to { node: Node, context: Context -> unaryOperation(node as UnaryOperationNode, context) },

        // primitives
        NumberNode::class.java to { node: Node, context: Context -> number(node as NumberNode, context) },
        ListNode::class.java to { node: Node, context: Context -> list(node as ListNode, context) },
        StringNode::class.java to { node: Node, context: Context -> string(node as StringNode, context) },

        // values
        AssignmentNode::class.java to { node: Node, context: Context -> assignment(node as AssignmentNode, context) },
        AccessNode::class.java to { node: Node, context: Context -> access(node as AccessNode, context) },
        TaskDefineNode::class.java to { node: Node, context: Context -> defineTask(node as TaskDefineNode, context) },
        TaskCallNode::class.java to { node: Node, context: Context -> callTask(node as TaskCallNode, context) },

        // branch control
        ReturnNode::class.java to { node: Node, context: Context -> callReturn(node as ReturnNode, context) }
    )

    fun visit(node: Node, context: Context): RuntimeResult {
        val result = visitors[node::class.java]?.invoke(node, context) ?: return RuntimeResult().failure(NodeIntepreterNotFoundError(
            "Interpreter method not found for ${node::class.java.simpleName}!",
            node.start,
            node.end,
            context
        ))

        if (result.error != null) {
            return RuntimeResult().failure(result.error!!)
        }

        return result
    }

    private fun binaryOperation(node: BinaryOperationNode, context: Context): RuntimeResult {
        val result = RuntimeResult()

        val left = result.register(this.visit(node.left, context))

        if (result.shouldReturn()) {
            return result
        }

        val right = result.register(this.visit(node.right, context))

        if (result.shouldReturn()) {
            return result
        }

        left!!
        right!!

        val operated: Pair<Value?, Error?> = when (node.operator.type) {
            AND -> left.and(right, node.operator)
            OR -> left.or(right, node.operator)

            PLUS -> left.plus(right, node.operator)
            MINUS -> left.minus(right, node.operator)
            MULTIPLY -> left.multiply(right, node.operator)
            DIVIDE -> left.divide(right, node.operator)
            MODULO -> left.modulo(right, node.operator)

            EQUALITY -> left.equality(right, node.operator)
            INEQUALITY -> left.inequality(right, node.operator)
            ARROW_LEFT -> left.lessThan(right, node.operator)
            ARROW_RIGHT -> left.greaterThan(right, node.operator)
            LESS_THAN_OR_EQUAL_TO -> left.lessThanOrEqualTo(right, node.operator)
            GREATER_THAN_OR_EQUAL_TO -> left.greaterThanOrEqualTo(right, node.operator)

            else -> {
                Value.delegateToIllegal(left, right, node.operator)
            }
        }

        return if (operated.second != null) {
            result.failure(operated.second!!)
        } else {
            result.success(operated.first!!.positioned(node.start, node.end))
        }
    }

    private fun unaryOperation(node: UnaryOperationNode, context: Context): RuntimeResult {
        val result = RuntimeResult()

        val value = result.register(this.visit(node.value, context))

        if (result.shouldReturn()) {
            return result
        }

        value!!

        when (node.operator.type) {
            MINUS -> {
                val transformed = value.multiply(IntValue(-1), node.operator)

                if (transformed.second != null) {
                    return result.failure(transformed.second!!)
                }

                return result.success(transformed.first!!.positioned(node.start, node.end))
            }

            NEGATE -> {
                val transformed = value.negated(node.operator)

                if (transformed.second != null) {
                    return result.failure(transformed.second!!)
                }

                return result.success(transformed.first!!.positioned(node.start, node.end))
            }

            else -> {
                return result.failure(IllegalOperationError(
                    "'${node.operator.type}' on '$value'",
                    node.operator.start,
                    value.end,
                    context
                ))
            }
        }
    }

    private fun number(node: NumberNode, context: Context): RuntimeResult {
        return RuntimeResult().success(
            (if (node.token.type == INT) {
                IntValue(node.token.value.toString().toInt())
            } else {
                FloatValue(node.token.value.toString().toFloat())
            }).positioned(node.start, node.end)
                .givenContext(context)
        )
    }

    private fun list(node: ListNode, context: Context): RuntimeResult {
        val result = RuntimeResult()
        val elements = arrayListOf<Value>()

        node.elements.forEach { node ->
            val value = result.register(this.visit(node, context))

            if (result.shouldReturn()) {
                return result
            }

            elements.add(value!!)
        }

        return result.success(ListValue(elements).positioned(node.start, node.end).givenContext(context))
    }

    private fun string(node: StringNode, context: Context): RuntimeResult {
        return RuntimeResult().success(StringValue(node.value.value.toString()).positioned(node.start, node.end).givenContext(context))
    }

    private fun assignment(node: AssignmentNode, context: Context): RuntimeResult {
        val result = RuntimeResult()

        val name = node.name.value as String
        val value = result.register(this.visit(node.value, context))

        if (result.shouldReturn()) {
            return result
        }

        value!!

        val set = context.table.set(Symbol(
            name,

            when (node.modifier.type) {
                ASSIGNMENT -> value

                INCREMENT -> {
                    val get = context.table.get(name, node.name.start, node.name.end, context)

                    if (get.error != null) {
                        return result.failure(get.error)
                    }

                    val modified = get.value!!.plus(IntValue(1), node.modifier)

                    if (modified.second != null) {
                        return result.failure(modified.second!!)
                    }

                    modified.first!!
                }

                DEINCREMENT -> {
                    val get = context.table.get(name, node.name.start, node.name.end, context)

                    if (get.error != null) {
                        return result.failure(get.error)
                    }

                    val modified = get.value!!.minus(IntValue(1), node.modifier)

                    if (modified.second != null) {
                        return result.failure(modified.second!!)
                    }

                    modified.first!!
                }

                INCREASE_BY -> {
                    val get = context.table.get(name, node.name.start, node.name.end, context)

                    if (get.error != null) {
                        return result.failure(get.error)
                    }

                    val modified = get.value!!.plus(value, node.modifier)

                    if (modified.second != null) {
                        return result.failure(modified.second!!)
                    }

                    modified.first!!
                }

                DECREASE_BY -> {
                    val get = context.table.get(name, node.name.start, node.name.end, context)

                    if (get.error != null) {
                        return result.failure(get.error)
                    }

                    val modified = get.value!!.minus(value, node.modifier)

                    if (modified.second != null) {
                        return result.failure(modified.second!!)
                    }

                    modified.first!!
                }

                MULTIPLY_BY -> {
                    val get = context.table.get(name, node.name.start, node.name.end, context)

                    if (get.error != null) {
                        return result.failure(get.error)
                    }

                    val modified = get.value!!.multiply(value, node.modifier)

                    if (modified.second != null) {
                        return result.failure(modified.second!!)
                    }

                    modified.first!!
                }

                DIVIDE_BY -> {
                    val get = context.table.get(name, node.name.start, node.name.end, context)

                    if (get.error != null) {
                        return result.failure(get.error)
                    }

                    val modified = get.value!!.divide(value, node.modifier)

                    if (modified.second != null) {
                        return result.failure(modified.second!!)
                    }

                    modified.first!!
                }

                MODULO_BY -> {
                    val get = context.table.get(name, node.name.start, node.name.end, context)

                    if (get.error != null) {
                        return result.failure(get.error)
                    }

                    val modified = get.value!!.modulo(value, node.modifier)

                    if (modified.second != null) {
                        return result.failure(modified.second!!)
                    }

                    modified.first!!
                }

                else -> {
                    return result.failure(IllegalOperationError(
                        "Invalid operation: '${node.modifier}'",
                        node.modifier.start,
                        node.modifier.end,
                        context
                    ))
                }
            },

            SymbolData(node.immutable, node.name.start, value.end)
        ), context, node.declaration)

        if (set.error != null) {
            return result.failure(set.error)
        }

        return result.success(set.value!!)
    }

    private fun access(node: AccessNode, context: Context): RuntimeResult {
        val result = RuntimeResult()

        if (type(node.identifier.value as String)) {
            return result.success(PrimitiveReferenceValue(node.identifier.value))
        } else {
            val get = context.table.get(node.identifier.value, node.identifier.start, node.identifier.end, context)

            if (get.error != null) {
                return result.failure(get.error)
            }

            return result.success(get.value)
        }
    }

    private fun defineTask(node: TaskDefineNode, context: Context): RuntimeResult {
        val result = RuntimeResult()

        val name = node.identifier
        val arguments = arrayListOf<RequiredArgument>()

        node.arguments.forEach {
            if (it.type != null) {
                val resolved = result.register(this.visit(it.type, context))

                if (result.error != null) {
                    return result
                }

                arguments.add(RequiredArgument(it.name, resolved))
            } else {
                arguments.add(RequiredArgument(it.name, null))
            }
        }

        var returnType: Value? = null

        if (node.returnType != null) {
            returnType = result.register(this.visit(node.returnType, context))

            if (result.shouldReturn()) {
                return result
            }
        }

        val task = DefinedTaskValue(name, arguments, node.body, false, returnType)
            .positioned(node.start, node.end)
            .givenContext(context)

        val set = context.table.set(Symbol(name, task, SymbolData(immutable = true, node.start, node.end)), context, declaration = true)

        if (set.error != null) {
            return result.failure(set.error)
        }

        return result.success(task)
    }

    private fun callTask(node: TaskCallNode, context: Context): RuntimeResult {
        val result = RuntimeResult()

        val passedArguments = mutableListOf<PassedArgument>()

        node.arguments.forEach {
            val value = result.register(this.visit(it.node, context))

            if (result.error != null) {
                return result
            }

            passedArguments.add(PassedArgument(value!!))
        }

        val target = result.register(this.visit(node.target, context))

        if (result.shouldReturn()) {
            return result
        }

        target!!.clone().positioned(node.start, node.end).givenContext(context)

        var executed = result.register(target.execute(passedArguments, node.start, node.end, context))

        if (result.shouldReturn()) {
            return result
        }

        if (executed == null) {
            executed = NullValue()
        }

        executed = executed.clone().positioned(node.start, node.end).givenContext(context)

        return result.success(executed)
    }

    private fun callReturn(node: ReturnNode, context: Context): RuntimeResult {
        val result = RuntimeResult()

        val value: Value = if (node.value != null) {
            val local = result.register(this.visit(node.value, context))

            if (result.shouldReturn()) {
                return result
            }

            local as Value
        } else {
            NullValue()
        }

        return result.successReturn(value)
    }

}