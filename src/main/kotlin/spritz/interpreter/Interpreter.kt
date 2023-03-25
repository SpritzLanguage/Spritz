package spritz.interpreter

import spritz.api.Coercion
import spritz.error.Error
import spritz.error.interpreting.IllegalOperationError
import spritz.error.interpreting.NodeIntepreterNotFoundError
import spritz.error.interpreting.TypeMismatchError
import spritz.interpreter.context.Context
import spritz.lexer.token.TokenType.*
import spritz.parser.node.Node
import spritz.parser.nodes.*
import spritz.parser.nodes.condition.ConditionNode
import spritz.util.ANONYMOUS
import spritz.util.RequiredArgument
import spritz.value.NullValue
import spritz.value.PrimitiveValue
import spritz.value.Value
import spritz.value.bool.BooleanValue
import spritz.value.`class`.DefinedClassValue
import spritz.value.dictionary.DictionaryValue
import spritz.value.list.ListValue
import spritz.value.number.*
import spritz.value.string.StringValue
import spritz.value.table.Table
import spritz.value.table.TableAccessor
import spritz.value.task.DefinedTaskValue
import spritz.value.task.JvmTaskValue

/**
 * @author surge
 * @since 01/03/2023
 */
class Interpreter {

    private val visitors = mapOf(
        // operations
        BinaryOperationNode::class.java to { node: Node, parentContext: Context, childContext: Context -> binaryOperation(node as BinaryOperationNode, parentContext, childContext) },
        UnaryOperationNode::class.java to { node: Node, parentContext: Context, childContext: Context -> unaryOperation(node as UnaryOperationNode, parentContext, childContext) },

        // primitives
        NumberNode::class.java to { node: Node, parentContext: Context, childContext: Context -> number(node as NumberNode, parentContext, childContext) },
        ListNode::class.java to { node: Node, parentContext: Context, childContext: Context -> list(node as ListNode, parentContext, childContext) },
        DictionaryNode::class.java to { node: Node, parentContext: Context, childContext: Context -> dictionary(node as DictionaryNode, parentContext, childContext) },
        StringNode::class.java to { node: Node, parentContext: Context, childContext: Context -> string(node as StringNode, parentContext, childContext) },

        // values
        AssignmentNode::class.java to { node: Node, parentContext: Context, childContext: Context -> assignment(node as AssignmentNode, parentContext, childContext) },
        AccessNode::class.java to { node: Node, parentContext: Context, childContext: Context -> access(node as AccessNode, parentContext, childContext) },
        TaskDefineNode::class.java to { node: Node, parentContext: Context, childContext: Context -> defineTask(node as TaskDefineNode, parentContext, childContext) },
        ClassDefineNode::class.java to { node: Node, parentContext: Context, childContext: Context -> defineClass(node as ClassDefineNode, parentContext, childContext) },
        TaskCallNode::class.java to { node: Node, parentContext: Context, childContext: Context -> callTask(node as TaskCallNode, parentContext, childContext) },

        // branch control
        ConditionNode::class.java to { node: Node, parentContext: Context, childContext: Context -> condition(node as ConditionNode, parentContext, childContext) },
        ForNode::class.java to { node: Node, parentContext: Context, childContext: Context -> `for`(node as ForNode, parentContext, childContext) },
        WhileNode::class.java to { node: Node, parentContext: Context, childContext: Context -> `while`(node as WhileNode, parentContext, childContext) },
        ReturnNode::class.java to { node: Node, parentContext: Context, childContext: Context -> callReturn(node as ReturnNode, parentContext, childContext) },
        ContinueNode::class.java to { node: Node, parentContext: Context, childContext: Context -> callContinue(node as ContinueNode, parentContext, childContext) },
        BreakNode::class.java to { node: Node, parentContext: Context, childContext: Context -> callBreak(node as BreakNode, parentContext, childContext) },

        // try catch
        TryNode::class.java to { node: Node, parentContext: Context, childContext: Context -> `try`(node as TryNode, parentContext, childContext) },
        CatchNode::class.java to { node: Node, parentContext: Context, childContext: Context -> catch(node as CatchNode, parentContext, childContext) },
    )

    /**
     * Visits the method that is relevant to the given [node]. The context surrounding
     * variable accessing should be provided by [parentContext], and any accessing should
     * only be provided with symbols from [childContext]
     */
    fun visit(node: Node, parentContext: Context, childContext: Context = parentContext): RuntimeResult {
        val result = visitors[node::class.java]?.invoke(node, parentContext, childContext) ?: return RuntimeResult().failure(NodeIntepreterNotFoundError(
            "Interpreter method not found for ${node::class.java.simpleName}!",
            node.start,
            node.end,
            parentContext
        ))

        if (result.error != null) {
            return result.failure(result.error!!)
        }

        return result
    }

    private fun binaryOperation(node: BinaryOperationNode, context: Context, childContext: Context): RuntimeResult {
        val result = RuntimeResult()

        val left = result.register(this.visit(node.left, context))

        if (result.shouldReturn()) {
            return result
        }

        if (node.operator.type == OR && left is BooleanValue && left.value) {
            return result.success(BooleanValue(true).positioned(node.start, node.end).givenContext(context))
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
            ASTERISK -> left.multiply(right, node.operator)
            DIVIDE -> left.divide(right, node.operator)
            MODULO -> left.modulo(right, node.operator)

            BIN_SHIFT_LEFT -> left.binShl(right, node.operator)
            BIN_SHIFT_RIGHT -> left.binShr(right, node.operator)
            BIN_UNSIGNED_SHIFT_RIGHT -> left.binUShr(right, node.operator)
            BIN_OR -> left.binOr(right, node.operator)
            BIN_AND -> left.binAnd(right, node.operator)
            BIN_XOR -> left.binXor(right, node.operator)

            EQUALITY -> left.equality(right, node.operator)
            INEQUALITY -> left.inequality(right, node.operator)
            ROUGH_EQUALITY -> left.roughEquality(right, node.operator)
            ROUGH_INEQUALITY -> left.roughInequality(right, node.operator)
            ARROW_LEFT -> left.lessThan(right, node.operator)
            ARROW_RIGHT -> left.greaterThan(right, node.operator)
            LESS_THAN_OR_EQUAL_TO -> left.lessThanOrEqualTo(right, node.operator)
            GREATER_THAN_OR_EQUAL_TO -> left.greaterThanOrEqualTo(right, node.operator)

            else -> {
                if (node.operator.matches("is")) {
                    Pair(BooleanValue(PrimitiveValue.check(left, right) || left.type == right.type), null)
                } else {
                    Value.delegateToIllegal(left, right, node.operator)
                }
            }
        }

        return if (operated.second != null) {
            result.failure(operated.second!!)
        } else {
            result.success(operated.first!!.positioned(node.start, node.end).givenContext(context))
        }
    }

    private fun unaryOperation(node: UnaryOperationNode, context: Context, childContext: Context): RuntimeResult {
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

            BIN_COMPLEMENT -> {
                val transformed = value.binComplement(node.operator)

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

    private fun number(node: NumberNode, context: Context, childContext: Context): RuntimeResult {
        val result = RuntimeResult()

        var reference = when (node.token.type) {
            BYTE -> ByteValue(node.token.value.toString().toByte())
            INT -> IntValue(node.token.value.toString().toInt())
            else -> FloatValue(node.token.value.toString().toFloat())
        }.positioned(node.start, node.end).givenContext(context)

        val child = result.register(child(node, reference, context))

        if (result.shouldReturn()) {
            return result
        }

        reference = child!!

        return result.success(reference)
    }

    private fun list(node: ListNode, context: Context, childContext: Context): RuntimeResult {
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

    private fun dictionary(node: DictionaryNode, context: Context, childContext: Context): RuntimeResult {
        val result = RuntimeResult()
        val elements = hashMapOf<String, Value>()

        node.elements.forEach { (key, value) ->
            val value = result.register(this.visit(value, context))

            if (result.shouldReturn()) {
                return result
            }

            elements[key] = value!!
        }

        return result.success(DictionaryValue(elements).positioned(node.start, node.end).givenContext(context))
    }

    private fun string(node: StringNode, context: Context, childContext: Context): RuntimeResult {
        val result = RuntimeResult()

        var reference = StringValue(node.value.value.toString()).positioned(node.start, node.end).givenContext(context)

        val child = result.register(child(node, reference, context))

        if (result.shouldReturn()) {
            return result
        }

        reference = child!!

        return result.success(reference)
    }

    private fun assignment(node: AssignmentNode, context: Context, childContext: Context): RuntimeResult {
        val result = RuntimeResult()

        val name = node.name.value as String
        val value = result.register(this.visit(node.value, context))

        if (result.shouldReturn()) {
            return result
        }

        value!!

        val set = TableAccessor(childContext.table)
            .identifier(name)
            .set(when (node.modifier.type) {
                    ASSIGNMENT -> value

                    INCREMENT -> {
                        val get = TableAccessor(context.table)
                            .identifier(name)
                            .find(node.name.start, node.name.end, context)

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
                        val get = TableAccessor(context.table)
                            .identifier(name)
                            .find(node.name.start, node.name.end, context)

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
                        val get = TableAccessor(context.table)
                            .identifier(name)
                            .find(node.name.start, node.name.end, context)

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
                        val get = TableAccessor(context.table)
                            .identifier(name)
                            .find(node.name.start, node.name.end, context)

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
                        val get = TableAccessor(context.table)
                            .identifier(name)
                            .find(node.name.start, node.name.end, context)

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
                        val get = TableAccessor(context.table)
                            .identifier(name)
                            .find(node.name.start, node.name.end, context)

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
                        val get = TableAccessor(context.table)
                            .identifier(name)
                            .find(node.name.start, node.name.end, context)

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
                }, declaration = node.declaration, forced = node.forced, Table.Data(node.start, node.end, childContext))

        if (set.error != null) {
            return result.failure(set.error)
        }

        return result.success(set.value!!)
    }

    private fun access(node: AccessNode, context: Context, childContext: Context): RuntimeResult {
        val result = RuntimeResult()

        var reference = TableAccessor(childContext.table)
            .identifier(node.identifier.value.toString())
            .predicate { node.predicate(it as Value) }
            .top(context != childContext)
            .find(node.identifier.start, node.identifier.end, childContext).also {
                if (it.error != null) {
                    return result.failure(it.error)
                }
            }.value!!

        // although this is still executed when a CallNode is invoked, it will be overridden by the returned value
        val child = result.register(child(node, reference, context))

        if (result.shouldReturn()) {
            return result
        }

        reference = child!!

        return result.success(reference)
    }

    private fun defineTask(node: TaskDefineNode, context: Context, childContext: Context): RuntimeResult {
        val result = RuntimeResult()

        val name = node.identifier
        val arguments = arrayListOf<RequiredArgument>()

        node.arguments.forEach {
            if (it.type != null) {
                val resolved = result.register(this.visit(it.type, context))

                if (result.shouldReturn()) {
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

        val task = DefinedTaskValue(name, arguments, node.body, node.expression, returnType)
            .positioned(node.start, node.end)
            .givenContext(context) as DefinedTaskValue

        val set = TableAccessor(context.table)
            .identifier(name)
            .immutable(true)
            .predicate { it is DefinedTaskValue && it.arguments.size == arguments.size }
            .set(task, declaration = true, forced = name == ANONYMOUS, data = Table.Data(node.start, node.end, context))

        if (set.error != null) {
            return result.failure(set.error)
        }

        return result.success(task)
    }

    private fun defineClass(node: ClassDefineNode, context: Context, childContext: Context): RuntimeResult {
        val result = RuntimeResult()

        val name = node.name.value as String

        val constructor = arrayListOf<RequiredArgument>()

        node.constructor.forEach {
            if (it.type != null) {
                val resolved = result.register(this.visit(it.type, context))

                if (result.error != null) {
                    return result
                }

                constructor.add(RequiredArgument(it.name, resolved))
            } else {
                constructor.add(RequiredArgument(it.name, null))
            }
        }

        val `class` = DefinedClassValue(name, constructor, node.body).positioned(node.start, node.end).givenContext(context)

        val set = TableAccessor(context.table)
            .identifier(name)
            .immutable(true)
            .set(`class`, declaration = true, data = Table.Data(node.start, node.end, context))

        if (set.error != null) {
            return result.failure(set.error)
        }

        return result.success(`class`)
    }

    private fun callTask(node: TaskCallNode, context: Context, childContext: Context): RuntimeResult {
        val result = RuntimeResult()

        val passedArguments = mutableListOf<Value>()

        node.arguments.forEach {
            val value = result.register(this.visit(it, context))

            if (result.error != null) {
                return result
            }

            passedArguments.add(value!!)
        }

        val target = result.register(this.visit((node.target as AccessNode).setPredicate {
            if (it is DefinedTaskValue) {
                it.arguments.size == passedArguments.size
            } else if (it is JvmTaskValue) {
                it.arguments.size == passedArguments.size
            } else {
                true
            }
        }, childContext))

        if (result.shouldReturn()) {
            return result
        }

        target!!.clone().positioned(node.start, node.end).givenContext(context)

        var returned = result.register(target.execute(passedArguments, node.start, node.end, context))

        if (result.shouldReturn()) {
            return result
        }

        if (returned == null) {
            returned = NullValue()
        }

        val child = result.register(child(node, returned, context))

        if (result.shouldReturn()) {
            return result
        }

        returned = child!!

        returned = returned.clone().positioned(node.start, node.end).givenContext(context)

        return result.success(returned)
    }

    private fun callReturn(node: ReturnNode, context: Context, childContext: Context): RuntimeResult {
        val result = RuntimeResult()

        val value: Value = if (node.value != null) {
            val local = result.register(this.visit(node.value, context))

            if (result.shouldReturn()) {
                return result
            }

            local!!.positioned(node.start, node.end).givenContext(context)
        } else {
            NullValue().positioned(node.start, node.end).givenContext(context)
        }

        return result.successReturn(value)
    }

    private fun callContinue(node: ContinueNode, context: Context, childContext: Context): RuntimeResult {
        return RuntimeResult().successContinue()
    }

    private fun callBreak(node: BreakNode, context: Context, childContext: Context): RuntimeResult {
        return RuntimeResult().successBreak()
    }

    private fun `for`(node: ForNode, context: Context, childContext: Context): RuntimeResult {
        val result = RuntimeResult()

        val list = result.register(this.visit(node.expression, context))

        // handle error before type mismatch
        if (result.shouldReturn()) {
            return result
        }

        if (list !is ListValue) {
            return result.failure(TypeMismatchError(
                "Expected 'list' not ${list?.type ?: "<JVM NULL>"}",
                node.expression.start,
                node.expression.end,
                context
            ))
        }

        val scope = Context("scope", parent = context).givenTable(Table(context.table))

        val elements = mutableListOf<Value>()

        for (i in list.elements) {
            TableAccessor(scope.table)
                .identifier(node.identifier.value.toString())
                .immutable(true)
                .set(i, declaration = true, forced = true, data = Table.Data(node.identifier.start, node.identifier.end, context))

            val body = result.register(this.visit(node.body, scope))

            if (result.shouldReturn() && !result.shouldContinue && !result.shouldBreak) {
                return result
            }

            if (result.shouldContinue) {
                continue
            }

            if (result.shouldBreak) {
                break
            }

            elements.add(body!!)
        }

        return result.success(ListValue(elements).positioned(node.start, node.end).givenContext(scope))
    }

    private fun `while`(node: WhileNode, context: Context, childContext: Context): RuntimeResult {
        val result = RuntimeResult()

        val scope = Context("scope", parent = context).givenTable(Table(context.table))

        val elements = mutableListOf<Value>()

        while (true) {
            val scope = Context("scope", context)
            scope.table = Table(parent = context.table)

            val condition = result.register(this.visit(node.expression, context))

            // handle error before type mismatch
            if (result.shouldReturn()) {
                return result
            }

            if (condition !is BooleanValue) {
                return result.failure(TypeMismatchError(
                    "Expected 'bool' not ${condition?.type ?: "<JVM NULL>"}",
                    node.expression.start,
                    node.expression.end,
                    context
                ))
            }

            if (!condition.value) {
                break
            }

            val value = result.register(this.visit(node.body, scope))

            if (result.shouldReturn() && !result.shouldContinue && !result.shouldBreak) {
                return result
            }

            if (result.shouldContinue) {
                continue
            }

            if (result.shouldBreak) {
                break
            }

            elements.add(value!!)
        }

        return result.success(ListValue(elements).positioned(node.start, node.end).givenContext(scope))
    }

    private fun `try`(node: TryNode, context: Context, childContext: Context): RuntimeResult {
        val result = RuntimeResult()

        val scope = Context("scope", parent = context).givenTable(Table(context.table))

        val scopeResult = RuntimeResult()

        val value: Value? = scopeResult.register(this.visit(node.body, scope))

        if (scopeResult.error != null) {
            if (node.catch != null) {
                val scope = Context("scope", parent = context).givenTable(Table(context.table))

                TableAccessor(scope.table)
                    .identifier(node.catch.exception)
                    .immutable(true)
                    .set(Coercion.IntoSpritz.coerce(scopeResult.error))

                val catched = scopeResult.register(this.visit(node.catch, scope))

                if (scopeResult.error != null) {
                    return result.failure(scopeResult.error!!)
                }

                return result.success(catched)
            }

            return result.success(NullValue())
        }

        return result.success(value!!)
    }

    private fun `catch`(node: CatchNode, context: Context, childContext: Context): RuntimeResult {
        val result = RuntimeResult()

        val value = result.register(this.visit(node.body, context))

        if (result.error != null) {
            return result
        }

        return result.success(value)
    }

    private fun condition(node: ConditionNode, context: Context, childContext: Context): RuntimeResult {
        val result = RuntimeResult()

        node.cases.forEach { case ->
            if (!case.`else`) {
                val condition = result.register(this.visit(case.condition!!, context))

                if (result.shouldReturn()) {
                    return result
                }

                if (condition !is BooleanValue) {
                    return result.failure(
                        TypeMismatchError(
                            "Expected 'bool' not ${condition?.type ?: "<JVM NULL>"}",
                            case.node.start,
                            case.node.end,
                            context
                        )
                    )
                }

                if (!condition.value) {
                    return@forEach
                }
            }

            val scope = Context("scope", context)
            scope.table = Table(context.table)

            val interpreted = result.register(this.visit(case.node, scope))

            if (result.shouldReturn()) {
                return result
            }

            return result.success(interpreted!!)
        }

        return result.success(NullValue())
    }

    private fun child(node: Node, reference: Value, context: Context): RuntimeResult {
        val result = RuntimeResult()

        if (node.child != null) {
            val childReferenceContext = Context(reference.identifier)
            childReferenceContext.table = reference.table

            val child = result.register(this.visit(node.child!!, context, childReferenceContext))

            if (result.shouldReturn()) {
                return result
            }

            return result.success(child!!)
        }

        return result.success(reference)
    }

}