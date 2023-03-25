package spritz.parser

import spritz.api.Config
import spritz.error.parsing.ParsingError
import spritz.lexer.token.Token
import spritz.lexer.token.TokenType
import spritz.lexer.token.TokenType.*
import spritz.parser.node.Node
import spritz.parser.nodes.*
import spritz.parser.nodes.condition.Case
import spritz.parser.nodes.condition.ConditionNode
import spritz.util.*
import spritz.warning.Warning

/**
 * @author surge
 * @since 26/02/2023
 */
class Parser(val config: Config, val tokens: List<Token<*>>) {

    private var index = -1
    private lateinit var currentToken: Token<*>

    init {
        advance()
    }

    fun parse(): ParseResult {
        val result = this.statements()

        if (result.error != null && this.currentToken.type != END_OF_FILE) {
            return result.failure(ParsingError(
                "Token '$currentToken' cannot appear here!",
                this.currentToken.start,
                this.currentToken.end
            ))
        }

        return result
    }

    private fun statements(): ParseResult {
        val result = ParseResult()
        val statements = mutableListOf<Node>()
        val start = this.currentToken.start.clone()

        var statement = this.statement()
        var statementResult = result.tryRegister(statement)

        if (statement.error != null && this.currentToken.type != END_OF_FILE && this.currentToken.type != CLOSE_BRACE) {
            return statement
        }

        if (statementResult != null) {
            statements.add(statementResult as Node)
        }

        while (true) {
            statement = this.statement()
            statementResult = result.tryRegister(statement)

            if (statement.error != null && this.currentToken.type != END_OF_FILE && this.currentToken.type != CLOSE_BRACE) {
                return statement
            }

            if (statementResult == null) {
                this.reverse(result.reverse)
                break
            }

            statements.add(statementResult as Node)
        }

        return result.success(ListNode(
            statements,
            start,
            this.currentToken.end.clone()
        ))
    }

    private fun statement(): ParseResult {
        val result = ParseResult()
        val start = this.currentToken.start.clone()

        if (this.currentToken.matches("return")) {
            advanceRegister(result)

            val expression = result.tryRegister(this.expression())

            if (expression == null) {
                this.reverse(result.reverse)
            }

            return result.success(ReturnNode(expression as Node?, start, this.currentToken.end.clone()))
        }

        if (this.currentToken.matches("continue")) {
            result.registerAdvancement()
            this.advance()
            return result.success(ContinueNode(start, this.currentToken.end.clone()))
        }

        if (this.currentToken.matches("break")) {
            result.registerAdvancement()
            this.advance()
            return result.success(BreakNode(start, this.currentToken.end.clone()))
        }

        val expression = result.register(this.expression())

        if (result.error != null) {
            return result.failure(ParsingError(
                "Expected expression",
                start,
                this.currentToken.end
            ))
        }

        return result.success(expression as Node)
    }

    private fun expression(): ParseResult {
        val result = ParseResult()

        val start = this.currentToken.start.clone()

        if (this.currentToken.matches("mut") || this.currentToken.matches("const")) {
            val immutable = this.currentToken.matches("const")

            advanceRegister(result)

            if (this.currentToken.type != IDENTIFIER) {
                return result.failure(
                    ParsingError(
                        "Expected identifier",
                        this.currentToken.start,
                        this.currentToken.end
                    )
                )
            }

            val name = this.currentToken as Token<String>

            advanceRegister(result)

            if (this.currentToken.type != ASSIGNMENT) {
                return result.failure(
                    ParsingError(
                        "Expected '='",
                        this.currentToken.start,
                        this.currentToken.end
                    )
                )
            }

            val modifier = this.currentToken

            advanceRegister(result)

            val expression = result.register(this.expression())

            if (result.error != null) {
                return result
            }

            return result.success(AssignmentNode(name, expression!!, modifier, immutable, declaration = true, forced = false, start, this.currentToken.end))
        }

        val node = result.register(this.binaryOperation({ this.comparisonExpression() }, hashMapOf(
            AND to null,
            OR to null
        )))

        if (result.error != null) {
            return result.failure(ParsingError(
                "Expected a binary operation",
                start,
                this.currentToken.end
            ))
        }

        return result.success(node as Node)
    }

    private fun comparisonExpression(): ParseResult {
        val result = ParseResult()

        if (unary(this.currentToken.type)) {
            val operator = this.currentToken

            advanceRegister(result)

            val node = result.register(this.comparisonExpression()) as Node

            if (result.error != null) {
                return result
            }

            return result.success(UnaryOperationNode(operator, node))
        }

        val node = result.register(this.binaryOperation({ this.arithmeticExpression() }, hashMapOf(
            EQUALITY to null,
            INEQUALITY to null,
            ROUGH_EQUALITY to null,
            ROUGH_INEQUALITY to null,
            ARROW_LEFT to null,
            LESS_THAN_OR_EQUAL_TO to null,
            ARROW_RIGHT to null,
            GREATER_THAN_OR_EQUAL_TO to null,
            KEYWORD to "is"
        )))

        if (result.error != null) {
            return result.failure(ParsingError(
                "Expected a value or expression",
                this.currentToken.start,
                this.currentToken.end
            ))
        }

        return result.success(node as Node)
    }

    private fun arithmeticExpression(): ParseResult {
        return this.binaryOperation({ term() }, hashMapOf(
            PLUS to null,
            MINUS to null,

            BIN_SHIFT_LEFT to null,
            BIN_SHIFT_RIGHT to null,
            BIN_UNSIGNED_SHIFT_RIGHT to null,
            BIN_OR to null,
            BIN_AND to null,
            BIN_XOR to null,
            BIN_COMPLEMENT to null
        ))
    }

    private fun term(): ParseResult {
        return this.binaryOperation({ factor() }, hashMapOf(
            ASTERISK to null,
            DIVIDE to null
        ))
    }

    private fun factor(): ParseResult {
        val result = ParseResult()
        val token = this.currentToken

        if (token.type in arrayOf(PLUS, MINUS)) {
            advanceRegister(result)

            val factor = result.register(this.factor())

            if (result.error != null) {
                return result
            }

            return result.success(UnaryOperationNode(token, factor as Node))
        }

        return this.modulo()
    }

    private fun modulo(): ParseResult {
        return this.binaryOperation({ call() }, hashMapOf(
            MODULO to null
        ), { this.factor() })
    }

    private fun call(child: Boolean = false, type: Boolean = false): ParseResult {
        val result = ParseResult()

        val atom = result.register(this.atom(child))
        var node = atom

        if (result.error != null) {
            return result
        }

        if (this.currentToken.type == OPEN_PARENTHESES) {
            if (type) {
                return result.failure(ParsingError(
                    "Unexpected call",
                    this.currentToken.start,
                    this.currentToken.end
                ))
            }

            advanceRegister(result)

            val argumentNodes = arrayListOf<Node>()

            if (this.currentToken.type == CLOSE_PARENTHESES) {
                result.registerAdvancement()
                this.advance()
            } else {
                val node = result.register(this.expression())

                if (result.error != null) {
                    return result.failure(ParsingError(
                        "Expected argument",
                        this.currentToken.start,
                        this.currentToken.end
                    ))
                }

                argumentNodes.add(node as Node)

                while (this.currentToken.type == COMMA) {
                    advanceRegister(result)

                    val node = result.register(this.expression())

                    if (result.error != null) {
                        return result
                    }

                    argumentNodes.add(node as Node)
                }

                if (this.currentToken.type != CLOSE_PARENTHESES) {
                    return result.failure(ParsingError(
                        "Expected ',' or ')'",
                        this.currentToken.start,
                        this.currentToken.end
                    ))
                }

                advanceRegister(result)
            }

            node = TaskCallNode(atom as Node, argumentNodes, atom.start, this.currentToken.end)
        }

        var child: Node? = node

        while (this.currentToken.type == ACCESSOR) {
            result.registerAdvancement()
            this.advance()

            val sub = result.register(this.call(true))

            if (result.error != null) {
                return result
            }

            child!!.child = sub as Node
            child = sub
        }

        return result.success(node as Node)
    }

    private fun atom(child: Boolean = false): ParseResult {
        val result = ParseResult()

        val token = this.currentToken

        if (token.type in arrayOf(INT, FLOAT, BYTE)) {
            advanceRegister(result)
            return result.success(NumberNode(token))
        }

        if (token.type == OPEN_PARENTHESES) {
            advanceRegister(result)

            val expression = result.register(this.expression())

            if (result.error != null) {
                return result
            }

            return if (this.currentToken.type == CLOSE_PARENTHESES) {
                advanceRegister(result)

                result.success(expression!!)
            } else {
                result.failure(ParsingError(
                    "Expected ')'",
                    this.currentToken.start,
                    this.currentToken.end
                ))
            }
        }

        if (token.type == OPEN_SQUARE) {
            advanceRegister(result)

            val elements = mutableListOf<Node>()

            if (this.currentToken.type == CLOSE_SQUARE) {
                advanceRegister(result)
            } else {
                var node = result.register(this.expression())

                if (result.error != null) {
                    return result
                }

                elements.add(node as Node)

                while (this.currentToken.type == COMMA) {
                    advanceRegister(result)

                    node = result.register(this.expression())

                    if (result.error != null) {
                        return result
                    }

                    elements.add(node as Node)
                }

                if (this.currentToken.type != CLOSE_SQUARE) {
                    return result.failure(ParsingError(
                        "Expected ']'",
                        this.currentToken.start,
                        this.currentToken.end
                    ))
                }

                advanceRegister(result)
            }

            return result.success(ListNode(
                elements,
                token.start,
                this.currentToken.end
            ))
        }

        if (token.type == OPEN_BRACE) {
            advanceRegister(result)

            val elements = hashMapOf<String, Node>()

            if (this.currentToken.type == CLOSE_BRACE) {
                advanceRegister(result)
            } else {
                if (this.currentToken.type != STRING) {
                    return result.failure(ParsingError(
                        "Expected string",
                        this.currentToken.start,
                        this.currentToken.end
                    ))
                }

                val key = this.currentToken.value.toString()

                advanceRegister(result)

                if (this.currentToken.type != COLON) {
                    return result.failure(ParsingError(
                        "Expected ':'",
                        this.currentToken.start,
                        this.currentToken.end
                    ))
                }

                advanceRegister(result)

                val value = result.register(this.expression())

                if (result.error != null) {
                    return result
                }

                elements[key] = value!!

                while (this.currentToken.type == COMMA) {
                    advanceRegister(result)

                    if (this.currentToken.type != STRING) {
                        return result.failure(ParsingError(
                            "Expected string",
                            this.currentToken.start,
                            this.currentToken.end
                        ))
                    }

                    val key = this.currentToken.value.toString()

                    advanceRegister(result)

                    if (this.currentToken.type != COLON) {
                        return result.failure(ParsingError(
                            "Expected ':'",
                            this.currentToken.start,
                            this.currentToken.end
                        ))
                    }

                    advanceRegister(result)

                    val value = result.register(this.expression())

                    if (result.error != null) {
                        return result
                    }

                    elements[key!!] = value!!
                }

                if (this.currentToken.type != CLOSE_BRACE) {
                    return result.failure(ParsingError(
                        "Expected '}'",
                        this.currentToken.start,
                        this.currentToken.end
                    ))
                }

                advanceRegister(result)
            }

            return result.success(DictionaryNode(
                elements,
                token.start,
                this.currentToken.end
            ))
        }

        if (token.type == STRING) {
            advanceRegister(result)

            return result.success(StringNode(token, token.start, token.end))
        }

        if (token.type == IDENTIFIER || token.type == KEYWORD && child) {
            advanceRegister(result)

            fun assignment(forced: Boolean): ParseResult {
                val modifier = this.currentToken

                advanceRegister(result)

                var expression: Node = NumberNode(Token(INT, 1, modifier.start, modifier.end))

                if (modifier.type != INCREMENT && modifier.type != DEINCREMENT) {
                    val expr = result.register(this.expression())

                    if (result.error != null) {
                        return result
                    }

                    expression = expr!!
                }

                return result.success(AssignmentNode(token, expression, modifier, immutable = false, declaration = false, forced = forced, token.start, expression.end))
            }

            if (modifier(this.currentToken.type)) {
                return assignment(false)
            } else if (this.currentToken.type == ASTERISK) {
                if (!this.config.forcedAssignations) {
                    return result.failure(ParsingError(
                        "Forced assignations are disabled!",
                        this.currentToken.start,
                        this.currentToken.end
                    ))
                }

                advanceRegister(result)

                if (!modifier(this.currentToken.type)) {
                    return result.failure(ParsingError(
                        "Expected modifier",
                        this.currentToken.start,
                        this.currentToken.end
                    ))
                }

                return assignment(true)
            }

            return result.success(AccessNode(token))
        }

        if (token.matches("if")) {
            val condition = result.register(this.conditional())

            if (result.error != null) {
                return result
            }

            return result.success(condition as Node)
        }

        if (token.matches("task")) {
            val task = result.register(this.task())

            if (result.error != null) {
                return result
            }

            return result.success(task as Node)
        }

        if (token.matches("class")) {
            val `class` = result.register(this.`class`())

            if (result.error != null) {
                return result
            }

            return result.success(`class` as Node)
        }

        if (token.matches("for")) {
            val `for` = result.register(this.`for`())

            if (result.error != null) {
                return result
            }

            return result.success(`for` as Node)
        }

        if (token.matches("while")) {
            val `while` = result.register(this.`while`())

            if (result.error != null) {
                return result
            }

            return result.success(`while` as Node)
        }

        if (token.matches("try")) {
            val `try` = result.register(this.`try`())

            if (result.error != null) {
                return result
            }

            return result.success(`try` as Node)
        }

        return result.failure(ParsingError(
            "Expected int, float, '+', '-' or '('",
            token.start,
            token.end
        ))
    }

    private fun task(): ParseResult {
        val result = ParseResult()

        val start = this.currentToken.start

        advanceRegister(result)

        var returnType: Node? = null

        if (this.currentToken.type == ARROW_LEFT) {
            advanceRegister(result)

            returnType = result.register(this.call(child = true, type = true))

            if (result.error != null) {
                return result
            }

            if (this.currentToken.type != ARROW_RIGHT) {
                return result.failure(ParsingError(
                    "Expected '>'",
                    this.currentToken.start,
                    this.currentToken.end
                ))
            }

            advanceRegister(result)
        }

        var name = ANONYMOUS

        if (this.currentToken.type == IDENTIFIER) {
            name = this.currentToken.value.toString()
            advanceRegister(result)
        }

        if (name.any { it.isUpperCase() }) {
            result.warn(Warning("Name should be in snake_case (upper case char detected)", this.currentToken.start.clone()))
        }

        val arguments = mutableListOf<Argument>()

        if (this.currentToken.type == OPEN_PARENTHESES) {
            advanceRegister(result)

            while (this.currentToken.type == IDENTIFIER) {
                val argumentName = this.currentToken
                advanceRegister(result)

                var argumentType: Node? = null

                if (this.currentToken.type == COLON) {
                    advanceRegister(result)

                    argumentType = result.register(this.call(child = true, type = true))

                    if (result.error != null) {
                        return result
                    }
                }

                if (this.currentToken.type == COMMA) {
                    advanceRegister(result)
                }

                arguments.add(Argument(argumentName, argumentType))
            }

            if (this.currentToken.type != CLOSE_PARENTHESES) {
                return result.failure(ParsingError(
                    "Expected ')'",
                    this.currentToken.start,
                    this.currentToken.end
                ))
            }

            advanceRegister(result)
        }

        if (this.currentToken.type == ASSIGNMENT) {
            advanceRegister(result)

            val body = result.register(this.expression())

            if (result.error != null) {
                return result
            }

            return result.success(TaskDefineNode(
                name,
                true,
                returnType,
                arguments,
                body!!,
                start,
                this.currentToken.end
            ))
        }

        if (this.currentToken.type != OPEN_BRACE) {
            return result.failure(ParsingError(
                "Expected '{' or '='",
                this.currentToken.start,
                this.currentToken.end
            ))
        }

        advanceRegister(result)

        val body = result.register(this.statements())

        if (result.error != null) {
            return result
        }

        body as Node

        if (this.currentToken.type != CLOSE_BRACE) {
            return result.failure(ParsingError(
                "Expected '}'",
                this.currentToken.start,
                this.currentToken.end,
            ))
        }

        advanceRegister(result)

        return result.success(TaskDefineNode(
            name,
            false,
            returnType,
            arguments,
            body,
            start,
            this.currentToken.end
        ))
    }

    private fun `class`(): ParseResult {
        val result = ParseResult()
        val start = this.currentToken.start

        advanceRegister(result)

        if (this.currentToken.type != IDENTIFIER) {
            return result.failure(ParsingError(
                "Expected identifier",
                this.currentToken.start,
                this.currentToken.end
            ))
        }

        val name = this.currentToken

        advanceRegister(result)

        val constructor = mutableListOf<Argument>()

        if (this.currentToken.type == OPEN_PARENTHESES) {
            advanceRegister(result)

            while (this.currentToken.type == IDENTIFIER) {
                val argumentName = this.currentToken
                advanceRegister(result)

                var argumentType: Node? = null

                if (this.currentToken.type == COLON) {
                    advanceRegister(result)

                    argumentType = result.register(this.call(child = true, type = true))

                    if (result.error != null) {
                        return result
                    }
                }

                if (this.currentToken.type == COMMA) {
                    advanceRegister(result)
                }

                constructor.add(Argument(argumentName, argumentType))
            }

            if (this.currentToken.type != CLOSE_PARENTHESES) {
                return result.failure(ParsingError(
                    "Expected ')'",
                    this.currentToken.start,
                    this.currentToken.end
                ))
            }

            advanceRegister(result)
        }

        var body: Node? = null

        if (this.currentToken.type == OPEN_BRACE) {
            advanceRegister(result)

            body = result.register(this.statements())

            if (result.error != null) {
                return result
            }

            if (this.currentToken.type != CLOSE_BRACE) {
                return result.failure(ParsingError(
                    "Expected '}'",
                    this.currentToken.start,
                    this.currentToken.end
                ))
            }

            advanceRegister(result)
        }

        return result.success(ClassDefineNode(name, constructor, body, start, this.currentToken.end))
    }

    private fun `for`(): ParseResult {
        val result = ParseResult()
        val start = this.currentToken.start

        advanceRegister(result)

        if (this.currentToken.type != OPEN_PARENTHESES) {
            return result.failure(ParsingError(
                "Expected '('",
                this.currentToken.start,
                this.currentToken.end
            ))
        }

        advanceRegister(result)

        if (this.currentToken.type != IDENTIFIER) {
            return result.failure(ParsingError(
                "Expected identifier",
                this.currentToken.start,
                this.currentToken.end
            ))
        }

        val identifier = this.currentToken

        advanceRegister(result)

        if (this.currentToken.type != COLON) {
            return result.failure(ParsingError(
                "Expected ':'",
                this.currentToken.start,
                this.currentToken.end
            ))
        }

        advanceRegister(result)

        val expression = result.register(this.expression())

        if (result.error != null) {
            return result
        }

        if (this.currentToken.type != CLOSE_PARENTHESES) {
            return result.failure(ParsingError(
                "Expected ')'",
                this.currentToken.start,
                this.currentToken.end
            ))
        }

        advanceRegister(result)

        if (this.currentToken.type == ARROW) {
            advanceRegister(result)

            val body = result.register(this.expression())

            if (result.error != null) {
                return result
            }

            return result.success(ForNode(
                identifier,
                expression!!,
                body!!,
                start,
                this.currentToken.end
            ))
        }

        if (this.currentToken.type != OPEN_BRACE) {
            return result.failure(ParsingError(
                "Expected '{' or '='",
                this.currentToken.start,
                this.currentToken.end
            ))
        }

        advanceRegister(result)

        val body = result.register(this.statements())

        if (result.error != null) {
            return result
        }

        body as Node

        if (this.currentToken.type != CLOSE_BRACE) {
            return result.failure(ParsingError(
                "Expected '}'",
                this.currentToken.start,
                this.currentToken.end,
            ))
        }

        advanceRegister(result)

        return result.success(ForNode(
            identifier,
            expression!!,
            body,
            start,
            this.currentToken.end
        ))
    }

    private fun `while`(): ParseResult {
        val result = ParseResult()
        val start = this.currentToken.start

        advanceRegister(result)

        if (this.currentToken.type != OPEN_PARENTHESES) {
            return result.failure(ParsingError(
                "Expected '('",
                this.currentToken.start,
                this.currentToken.end
            ))
        }

        advanceRegister(result)

        val expression = result.register(this.expression())

        if (result.error != null) {
            return result
        }

        if (this.currentToken.type != CLOSE_PARENTHESES) {
            return result.failure(ParsingError(
                "Expected ')'",
                this.currentToken.start,
                this.currentToken.end
            ))
        }

        advanceRegister(result)

        if (this.currentToken.type == ARROW) {
            advanceRegister(result)

            val body = result.register(this.expression())

            if (result.error != null) {
                return result
            }

            return result.success(WhileNode(
                expression!!,
                body!!,
                start,
                this.currentToken.end
            ))
        }

        if (this.currentToken.type != OPEN_BRACE) {
            return result.failure(ParsingError(
                "Expected '{' or '='",
                this.currentToken.start,
                this.currentToken.end
            ))
        }

        advanceRegister(result)

        val body = result.register(this.statements())

        if (result.error != null) {
            return result
        }

        body as Node

        if (this.currentToken.type != CLOSE_BRACE) {
            return result.failure(ParsingError(
                "Expected '}'",
                this.currentToken.start,
                this.currentToken.end,
            ))
        }

        advanceRegister(result)

        return result.success(WhileNode(
            expression!!,
            body,
            start,
            this.currentToken.end
        ))
    }

    private fun `try`(): ParseResult {
        val result = ParseResult()
        val start = this.currentToken.start

        advanceRegister(result)

        if (this.currentToken.type == ARROW) {
            advanceRegister(result)

            val body = result.register(this.expression())

            if (result.error != null) {
                return result
            }

            var catchNode: Node? = null

            if (this.currentToken.matches("catch")) {
                catchNode = result.register(this.catch())

                if (result.error != null) {
                    return result
                }
            }

            return result.success(TryNode(
                body!!,
                catchNode as CatchNode?,
                start,
                this.currentToken.end
            ))
        }

        if (this.currentToken.type != OPEN_BRACE) {
            return result.failure(ParsingError(
                "Expected '{' or '='",
                this.currentToken.start,
                this.currentToken.end
            ))
        }

        advanceRegister(result)

        val body = result.register(this.statements())

        if (result.error != null) {
            return result
        }

        body as Node

        if (this.currentToken.type != CLOSE_BRACE) {
            return result.failure(ParsingError(
                "Expected '}'",
                this.currentToken.start,
                this.currentToken.end,
            ))
        }

        advanceRegister(result)

        var catchNode: Node? = null

        if (this.currentToken.matches("catch")) {
            catchNode = result.register(this.catch())

            if (result.error != null) {
                return result
            }
        }

        return result.success(TryNode(
            body,
            catchNode as CatchNode?,
            start,
            this.currentToken.end
        ))
    }

    private fun `catch`(): ParseResult {
        val result = ParseResult()
        val start = this.currentToken.start

        advanceRegister(result)

        if (this.currentToken.type != IDENTIFIER) {
            return result.failure(ParsingError(
                "Expected identifier",
                this.currentToken.start,
                this.currentToken.end
            ))
        }

        val exception = this.currentToken.value.toString()

        advanceRegister(result)

        if (this.currentToken.type == ARROW) {
            advanceRegister(result)

            val body = result.register(this.expression())

            if (result.error != null) {
                return result
            }

            return result.success(CatchNode(
                exception,
                body!!,
                start,
                this.currentToken.end
            ))
        }

        if (this.currentToken.type != OPEN_BRACE) {
            return result.failure(ParsingError(
                "Expected '{' or '='",
                this.currentToken.start,
                this.currentToken.end
            ))
        }

        advanceRegister(result)

        val body = result.register(this.statements())

        if (result.error != null) {
            return result
        }

        body as Node

        if (this.currentToken.type != CLOSE_BRACE) {
            return result.failure(ParsingError(
                "Expected '}'",
                this.currentToken.start,
                this.currentToken.end,
            ))
        }

        advanceRegister(result)

        return result.success(CatchNode(
            exception,
            body,
            start,
            this.currentToken.end
        ))
    }

    private fun conditional(): ParseResult {
        val result = ParseResult()

        val cases = result.register(cases("if"))

        if (result.error != null) {
            return result
        }

        return result.success(cases!!)
    }

    private fun cases(keyword: String): ParseResult {
        val result = ParseResult()
        val cases = mutableListOf<Case>()

        if (!this.currentToken.matches(keyword)) {
            return result.failure(ParsingError(
                "Expected '$keyword'",
                this.currentToken.start,
                this.currentToken.end
            ))
        }

        advanceRegister(result)

        if (this.currentToken.type != OPEN_PARENTHESES) {
            return result.failure(ParsingError(
                "Expected '('",
                this.currentToken.start,
                this.currentToken.end
            ))
        }

        advanceRegister(result)

        val condition = result.register(this.expression())

        if (result.error != null) {
            return result
        }
        
        if (this.currentToken.type != CLOSE_PARENTHESES) {
            return result.failure(ParsingError(
                "Expected ')'",
                this.currentToken.start,
                this.currentToken.end
            ))
        }
        
        advanceRegister(result)

        if (this.currentToken.type == OPEN_BRACE) {
            advanceRegister(result)

            val statements = result.register(this.statements())

            if (result.error != null) {
                return result
            }

            cases.add(Case(condition, statements!!))

            if (this.currentToken.type != CLOSE_BRACE) {
                return result.failure(ParsingError(
                    "Expected '}'",
                    this.currentToken.start,
                    this.currentToken.end
                ))
            }

            advanceRegister(result)
        } else {
            val statement = result.register(this.statement())

            if (result.error != null) {
                return result
            }

            cases.add(Case(condition, statement!!))
        }

        // else if and else
        while (this.currentToken.matches("else")) {
            advanceRegister(result)

            // else if
            if (this.currentToken.matches("if")) {
                advanceRegister(result)

                if (this.currentToken.type != OPEN_PARENTHESES) {
                    return result.failure(ParsingError(
                        "Expected '('",
                        this.currentToken.start,
                        this.currentToken.end
                    ))
                }

                advanceRegister(result)

                val condition = result.register(this.expression())

                if (result.error != null) {
                    return result
                }

                if (this.currentToken.type != CLOSE_PARENTHESES) {
                    return result.failure(ParsingError(
                        "Expected ')'",
                        this.currentToken.start,
                        this.currentToken.end
                    ))
                }

                advanceRegister(result)

                if (this.currentToken.type == OPEN_BRACE) {
                    advanceRegister(result)

                    val statements = result.register(this.statements())

                    if (result.error != null) {
                        return result
                    }

                    cases.add(Case(condition, statements!!))

                    if (this.currentToken.type != CLOSE_BRACE) {
                        return result.failure(ParsingError(
                            "Expected '}'",
                            this.currentToken.start,
                            this.currentToken.end
                        ))
                    }

                    advanceRegister(result)
                } else {
                    val statement = result.register(this.statement())

                    if (result.error != null) {
                        return result
                    }

                    cases.add(Case(condition, statement!!))
                }
            }

            // else
            else {
                if (this.currentToken.type == OPEN_BRACE) {
                    advanceRegister(result)

                    val statements = result.register(this.statements())

                    if (result.error != null) {
                        return result
                    }

                    cases.add(Case(condition, statements!!, true))

                    if (this.currentToken.type != CLOSE_BRACE) {
                        return result.failure(ParsingError(
                            "Expected '}'",
                            this.currentToken.start,
                            this.currentToken.end
                        ))
                    }

                    advanceRegister(result)
                } else {
                    val statement = result.register(this.statement())

                    if (result.error != null) {
                        return result
                    }

                    cases.add(Case(condition, statement!!, true))
                }

                break
            }
        }

        return result.success(ConditionNode(cases))
    }

    private fun binaryOperation(function: () -> ParseResult, operators: HashMap<TokenType, String?>, functionB: () -> ParseResult = function): ParseResult {
        val result = ParseResult()

        var left = result.register(function())

        if (result.error != null) {
            return result
        }

        while (operators.any { operator -> operator.key == this.currentToken.type && (operator.value == this.currentToken.value || operator.value == null) }) {
            val operator = this.currentToken

            advanceRegister(result)

            val right = result.register(functionB())

            if (result.error != null) {
                return result
            }

            if ((operator.type == DIVIDE || operator.type == DIVIDE_BY) && right is NumberNode && right.token.value == "0") {
                result.warn(Warning(
                    "Division by 0",
                    right.start
                ))
            }

            left = BinaryOperationNode(left as Node, operator, right as Node)
        }

        return result.success(left as Node)
    }

    private fun advance(): Token<*> {
        this.index++
        this.updateCurrent()
        return this.currentToken
    }

    private fun reverse(amount: Int = 1): Token<*> {
        this.index -= amount
        this.updateCurrent()
        return this.currentToken
    }

    private fun updateCurrent() {
        if (this.index >= 0 && this.index < tokens.size) {
            this.currentToken = this.tokens[this.index]
        }
    }

    private fun advanceRegister(result: ParseResult) {
        result.registerAdvancement()
        this.advance()
    }

}