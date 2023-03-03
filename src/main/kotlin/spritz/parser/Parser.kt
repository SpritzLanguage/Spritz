package spritz.parser

import spritz.error.parsing.ParsingError
import spritz.lexer.token.Token
import spritz.lexer.token.TokenType
import spritz.lexer.token.TokenType.*
import spritz.parser.node.Node
import spritz.parser.nodes.*
import spritz.util.*

/**
 * @author surge
 * @since 26/02/2023
 */
class Parser(val tokens: List<Token<*>>) {

    private var index = -1
    private lateinit var currentToken: Token<*>

    init {
        advance()
    }

    fun parse(): ParseResult {
        val result = this.statements()

        if (result.error != null && this.currentToken.type != END_OF_FILE) {
            return result.failure(ParsingError(
                "Expected an operator!",
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

            return result.success(AssignmentNode(name, expression!!, modifier, immutable, true, start, this.currentToken.end))
        }

        val node = result.register(this.binaryOperation({ this.comparisonExpression() }, hashMapOf(
            AND to "&&",
            OR to "||"
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

        if (this.currentToken.type == NEGATE) {
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
            ARROW_LEFT to null,
            LESS_THAN_OR_EQUAL_TO to null,
            ARROW_RIGHT to null,
            GREATER_THAN_OR_EQUAL_TO to null
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
            MINUS to null
        ))
    }

    private fun term(): ParseResult {
        return this.binaryOperation({ factor() }, hashMapOf(
            MULTIPLY to null,
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
        return this.binaryOperation({ this.call() }, hashMapOf(
            MODULO to null
        ), { this.factor() })
    }

    private fun call(): ParseResult {
        val result = ParseResult()

        val atom = result.register(this.atom())

        if (result.error != null) {
            return result
        }

        if (this.currentToken.type == OPEN_PARENTHESES) {
            advanceRegister(result)

            val argumentNodes = arrayListOf<CallArgument>()

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

                argumentNodes.add(CallArgument(node as Node))

                while (this.currentToken.type == COMMA) {
                    advanceRegister(result)

                    val node = result.register(this.expression())

                    if (result.error != null) {
                        return result
                    }

                    argumentNodes.add(CallArgument(node as Node))
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

            return result.success(TaskCallNode(atom as Node, argumentNodes, atom.start, this.currentToken.end))
        }

        return result.success(atom as Node)
    }

    private fun atom(): ParseResult {
        val result = ParseResult()

        val token = this.currentToken

        if (token.type in arrayOf(INT, FLOAT)) {
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

        if (token.type == STRING) {
            advanceRegister(result)

            return result.success(StringNode(token, token.start, token.end))
        }

        if (token.type == IDENTIFIER) {
            advanceRegister(result)

            if (modifier(this.currentToken.type)) {
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

                return result.success(AssignmentNode(token, expression, modifier, immutable = false, declaration = false, token.start, expression.end))
            }

            return result.success(AccessNode(token))
        }

        if (type(token.value.toString())) {
            advanceRegister(result)
            return result.success(AccessNode(token))
        }

        if (token.matches("task")) {
            val task = result.register(this.task())

            if (result.error != null) {
                return result
            }

            return result.success(task as Node)
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

            if (this.currentToken.type != IDENTIFIER && !type(this.currentToken.value as String)) {
                return result.failure(ParsingError(
                    "Expected identifier",
                    this.currentToken.start,
                    this.currentToken.end
                ))
            }

            returnType = result.register(this.atom())

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

        if (this.currentToken.type != IDENTIFIER) {
            return result.failure(ParsingError(
                "Expected identifier",
                this.currentToken.start,
                this.currentToken.end
            ))
        }

        val name = this.currentToken.value as String

        advanceRegister(result)

        val arguments = mutableListOf<Argument>()

        if (this.currentToken.type == OPEN_PARENTHESES) {
            advanceRegister(result)

            while (this.currentToken.type == IDENTIFIER) {
                val argumentName = this.currentToken
                advanceRegister(result)

                var argumentType: Node? = null

                if (this.currentToken.type == COLON) {
                    advanceRegister(result)

                    if (this.currentToken.type != IDENTIFIER && !type(this.currentToken.value as String)) {
                        return result.failure(ParsingError(
                            "Expected identifier",
                            this.currentToken.start,
                            this.currentToken.end
                        ))
                    }

                    argumentType = result.register(this.atom())

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

            val bodyStart = this.currentToken.start

            val body = result.register(this.expression())

            if (result.error != null) {
                return result
            }

            return result.success(TaskDefineNode(
                name,
                returnType,
                arguments,
                ListNode(listOf(body!!), bodyStart, this.currentToken.end),
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
            returnType,
            arguments,
            body as ListNode,
            start,
            this.currentToken.end
        ))
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