package spritz.interpreter

import spritz.error.Error
import spritz.error.interpreting.NodeIntepreterNotFoundError
import spritz.interpreter.context.Context
import spritz.lexer.token.TokenType.*
import spritz.parser.node.Node
import spritz.parser.nodes.BinaryOperationNode
import spritz.parser.nodes.ListNode
import spritz.parser.nodes.NumberNode
import spritz.value.Value
import spritz.value.list.ListValue
import spritz.value.number.FloatValue
import spritz.value.number.IntValue

/**
 * @author surge
 * @since 01/03/2023
 */
class Interpreter {

    private val visitors = mapOf(
        BinaryOperationNode::class.java to { node: Node, context: Context -> binaryOperation(node as BinaryOperationNode, context) },
        NumberNode::class.java to { node: Node, context: Context -> number(node as NumberNode, context) },
        ListNode::class.java to { node: Node, context: Context -> list(node as ListNode, context) }
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

    fun binaryOperation(node: BinaryOperationNode, context: Context): RuntimeResult {
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

}