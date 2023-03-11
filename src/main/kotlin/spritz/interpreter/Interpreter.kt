package spritz.interpreter

import spritz.Spritz
import spritz.error.Error
import spritz.error.interpreting.ExternalNotFoundError
import spritz.error.interpreting.IllegalOperationError
import spritz.error.interpreting.NodeIntepreterNotFoundError
import spritz.error.interpreting.TypeMismatchError
import spritz.interpreter.context.Context
import spritz.lexer.token.TokenType.*
import spritz.parser.node.Node
import spritz.parser.nodes.*
import spritz.parser.nodes.condition.ConditionNode
import spritz.util.RequiredArgument
import spritz.util.type
import spritz.value.NullValue
import spritz.value.PrimitiveReferenceValue
import spritz.value.Value
import spritz.value.bool.BoolValue
import spritz.value.container.DefinedContainerValue
import spritz.value.container.InstanceValue
import spritz.value.list.ListValue
import spritz.value.number.ByteValue
import spritz.value.number.FloatValue
import spritz.value.number.IntValue
import spritz.value.string.StringValue
import spritz.value.symbols.Symbol
import spritz.value.symbols.SymbolData
import spritz.value.symbols.Table
import spritz.value.task.DefinedTaskValue
import java.io.File
import java.nio.charset.Charset

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
        StringNode::class.java to { node: Node, parentContext: Context, childContext: Context -> string(node as StringNode, parentContext, childContext) },

        // values
        AssignmentNode::class.java to { node: Node, parentContext: Context, childContext: Context -> assignment(node as AssignmentNode, parentContext, childContext) },
        AccessNode::class.java to { node: Node, parentContext: Context, childContext: Context -> access(node as AccessNode, parentContext, childContext) },
        TaskDefineNode::class.java to { node: Node, parentContext: Context, childContext: Context -> defineTask(node as TaskDefineNode, parentContext, childContext) },
        ContainerDefineNode::class.java to { node: Node, parentContext: Context, childContext: Context -> defineContainer(node as ContainerDefineNode, parentContext, childContext) },
        TaskCallNode::class.java to { node: Node, parentContext: Context, childContext: Context -> callTask(node as TaskCallNode, parentContext, childContext) },

        // branch control
        ConditionNode::class.java to { node: Node, parentContext: Context, childContext: Context -> condition(node as ConditionNode, parentContext, childContext) },
        ForNode::class.java to { node: Node, parentContext: Context, childContext: Context -> `for`(node as ForNode, parentContext, childContext) },
        WhileNode::class.java to { node: Node, parentContext: Context, childContext: Context -> `while`(node as WhileNode, parentContext, childContext) },
        ReturnNode::class.java to { node: Node, parentContext: Context, childContext: Context -> callReturn(node as ReturnNode, parentContext, childContext) },

        // other
        ExternalNode::class.java to { node: Node, parentContext: Context, childContext: Context -> external(node as ExternalNode, parentContext, childContext) }
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
                Value.delegateToIllegal(left, right, node.operator)
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
        ), context, node.declaration, forced = node.forced)

        if (set.error != null) {
            return result.failure(set.error)
        }

        return result.success(set.value!!)
    }

    private fun access(node: AccessNode, context: Context, childContext: Context): RuntimeResult {
        val result = RuntimeResult()

        var reference: Value = if (type(node.identifier.value as String)) {
            PrimitiveReferenceValue(node.identifier.value)
        } else {
            val get = childContext.table.get(node.identifier.value, node.identifier.start, node.identifier.end, childContext)

            if (get.error != null) {
                return result.failure(get.error)
            }

            get.value!!
        }

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

        val set = context.table.set(Symbol(name, task, SymbolData(immutable = true, node.start, node.end)), context, declaration = true)

        if (set.error != null) {
            return result.failure(set.error)
        }

        return result.success(task)
    }

    private fun defineContainer(node: ContainerDefineNode, context: Context, childContext: Context): RuntimeResult {
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

        val container = DefinedContainerValue(name, constructor, node.body).positioned(node.start, node.end).givenContext(context)

        val set = context.table.set(Symbol(name, container, SymbolData(immutable = true, node.start, node.end)), context, declaration = true)

        if (set.error != null) {
            return result.failure(set.error)
        }

        return result.success(container)
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

        val target = result.register(this.visit(node.target, childContext))

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

        node.child?.let {
            val childContext = Context(executed!!.type)

            childContext.table = executed!!.table

            val child = result.register(this.visit(it, childContext))

            if (result.shouldReturn()) {
                return result
            }

            executed = child!!
        }

        executed = executed!!.clone().positioned(node.start, node.end).givenContext(context)

        return result.success(executed)
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
            scope.table.set(Symbol(node.identifier.value.toString(), i, SymbolData(immutable = true, node.identifier.start, node.identifier.end)), context, declaration = true, forced = true)

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

            if (condition !is BoolValue) {
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

    private fun condition(node: ConditionNode, context: Context, childContext: Context): RuntimeResult {
        val result = RuntimeResult()

        node.cases.forEach { case ->
            if (!case.`else`) {
                val condition = result.register(this.visit(case.condition!!, context))

                if (result.shouldReturn()) {
                    return result
                }

                if (condition !is BoolValue) {
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

    private fun external(node: ExternalNode, context: Context, childContext: Context): RuntimeResult {
        val result = RuntimeResult()

        val file = File("${node.path.value}.sz")

        if (file.exists()) {
            val spritz = Spritz(node.config)

            val lex = spritz.lex(file.nameWithoutExtension, file.readText(Charset.defaultCharset()))

            if (lex.second != null) {
                return result.failure(lex.second!!)
            }

            val parse = spritz.parse(lex.first)

            if (parse.error != null) {
                return result.failure(parse.error!!)
            }

            val instance = DefinedContainerValue(node.identifier.value.toString(), arrayListOf(), parse.node)
                .positioned(node.start, node.end)
                .givenContext(context)
                .execute(arrayListOf())

            if (instance.error != null) {
                return result.failure(instance.error!!)
            }

            instance.value!!.positioned(node.start, node.end).givenContext(context)

            val set = context.table.set(Symbol(node.identifier.value.toString(), instance.value!!, SymbolData(immutable = true, node.start, node.end)), context, declaration = true)

            if (set.error != null) {
                return result.failure(set.error)
            }

            return result.success(instance.value)
        } else {
            return result.failure(ExternalNotFoundError(
                "'${node.path}' was not found!",
                node.path.start,
                node.path.end,
                context
            ))
        }
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