package spritz.parser

import spritz.error.parsing.ParsingError
import spritz.lexer.token.Token
import spritz.lexer.token.TokenType
import spritz.lexer.token.TokenType.*
import spritz.parser.node.Node
import spritz.parser.nodes.*
import spritz.util.Argument
import spritz.util.modifier
import spritz.util.type

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
        return ParseResult().submit {
            val start = this.currentToken.start.clone()

            if (this.currentToken.matches("mut") || this.currentToken.matches("const")) {
                val const = this.currentToken.matches("const")

                advanceRegister(it)

                if (this.currentToken.type != IDENTIFIER) {
                    return@submit it.failure(
                        ParsingError(
                            "Expected identifier",
                            this.currentToken.start,
                            this.currentToken.end
                        )
                    )
                }

                val name = this.currentToken as Token<String>

                advanceRegister(it)

                if (this.currentToken.type != ASSIGNMENT) {
                    return@submit it.failure(
                        ParsingError(
                            "Expected '='",
                            this.currentToken.start,
                            this.currentToken.end
                        )
                    )
                }

                advanceRegister(it)

                val expression = it.register(this.expression())

                if (it.error != null) {
                    return@submit it
                }

                return@submit it.success(AssignmentNode(name, expression!!, start, this.currentToken.end))
            }

            val node = it.register(this.binaryOperation({ this.comparisonExpression() }, hashMapOf(
                AND to "&&",
                OR to "||"
            )))

            if (it.error != null) {
                return@submit it.failure(ParsingError(
                    "Expected a binary operation",
                    start,
                    this.currentToken.end
                ))
            }

            return@submit it.success(node as Node)
        }
    }

    private fun comparisonExpression(): ParseResult {
        return ParseResult().submit {
            if (this.currentToken.type == NEGATE) {
                val operator = this.currentToken

                advanceRegister(it)

                val node = it.register(this.comparisonExpression()) as Node

                if (it.error != null) {
                    return@submit it
                }

                return@submit it.success(UnaryOperationNode(operator, node))
            }

            val node = it.register(this.binaryOperation({ this.arithmeticExpression() }, hashMapOf(
                EQUALITY to null,
                INEQUALITY to null,
                ARROW_LEFT to null,
                LESS_THAN_OR_EQUAL_TO to null,
                ARROW_RIGHT to null,
                GREATER_THAN_OR_EQUAL_TO to null
            )))

            if (it.error != null) {
                return@submit it.failure(ParsingError(
                    "Expected a value or expression",
                    this.currentToken.start,
                    this.currentToken.end
                ))
            }

            it.success(node as Node)
        }
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
        return ParseResult().submit {
            val atom = it.register(this.atom())

            if (it.error != null) {
                return@submit it
            }

            it.success(atom as Node)
        }
    }

    private fun atom(): ParseResult {
        return ParseResult().submit {
            val token = this.currentToken

            if (token.type in arrayOf(INT, FLOAT)) {
                advanceRegister(it)
                return@submit it.success(NumberNode(token))
            }

            if (token.type == OPEN_PARENTHESES) {
                advanceRegister(it)

                val expression = it.register(this.expression())

                if (it.error != null) {
                    return@submit it
                }

                return@submit if (this.currentToken.type == CLOSE_PARENTHESES) {
                    advanceRegister(it)

                    it.success(expression!!)
                } else {
                    it.failure(ParsingError(
                        "Expected ')'",
                        this.currentToken.start,
                        this.currentToken.end
                    ))
                }
            }

            if (token.type == STRING) {
                advanceRegister(it)

                return@submit it.success(StringNode(token, token.start, token.end))
            }

            if (token.type == IDENTIFIER) {
                advanceRegister(it)

                if (modifier(this.currentToken.type)) {
                    val modifier = this.currentToken

                    advanceRegister(it)

                    var expression: Node = NumberNode(Token(INT, 1, modifier.start, modifier.end))

                    if (modifier.type != INCREMENT && modifier.type != DEINCREMENT) {
                        val expr = it.register(this.expression())

                        if (it.error != null) {
                            return@submit it
                        }

                        expression = expr!!
                    }

                    return@submit it.success(AssignmentNode(token, expression, token.start, expression.end))
                }

                return@submit it.success(AccessNode(token))
            }

            if (token.matches("task")) {
                val task = it.register(this.task())

                if (it.error != null) {
                    return@submit it
                }

                return@submit it.success(task as Node)
            }

            return@submit it.failure(ParsingError(
                "Expected int, float, '+', '-' or '('",
                token.start,
                token.end
            ))
        }
    }

    private fun task(): ParseResult {
        return ParseResult().submit {
            val start = this.currentToken.start

            advanceRegister(it)

            var returnType: String? = null

            if (this.currentToken.type == ARROW_LEFT) {
                advanceRegister(it)

                if (this.currentToken.type != IDENTIFIER && !type(this.currentToken.value as String)) {
                    return@submit it.failure(ParsingError(
                        "Expected identifier",
                        this.currentToken.start,
                        this.currentToken.end
                    ))
                }

                returnType = this.currentToken.value as String

                advanceRegister(it)

                if (this.currentToken.type != ARROW_RIGHT) {
                    return@submit it.failure(ParsingError(
                        "Expected '>'",
                        this.currentToken.start,
                        this.currentToken.end
                    ))
                }

                advanceRegister(it)
            }

            if (this.currentToken.type != IDENTIFIER) {
                return@submit it.failure(ParsingError(
                    "Expected identifier",
                    this.currentToken.start,
                    this.currentToken.end
                ))
            }

            val name = this.currentToken.value as String

            advanceRegister(it)

            val arguments = mutableListOf<Argument>()

            if (this.currentToken.type == OPEN_PARENTHESES) {
                advanceRegister(it)

                while (this.currentToken.type == IDENTIFIER) {
                    val argumentName = this.currentToken.value as String
                    advanceRegister(it)

                    var argumentType: String? = null

                    if (this.currentToken.type == COLON) {
                        advanceRegister(it)

                        if (this.currentToken.type != IDENTIFIER && !type(this.currentToken.value as String)) {
                            return@submit it.failure(ParsingError(
                                "Expected identifier",
                                this.currentToken.start,
                                this.currentToken.end
                            ))
                        }

                        argumentType = this.currentToken.value as String

                        advanceRegister(it)
                    }

                    if (this.currentToken.type == COMMA) {
                        advanceRegister(it)
                    }

                    arguments.add(Argument(argumentName, argumentType))
                }

                if (this.currentToken.type != CLOSE_PARENTHESES) {
                    return@submit it.failure(ParsingError(
                        "Expected ')'",
                        this.currentToken.start,
                        this.currentToken.end
                    ))
                }

                advanceRegister(it)
            }

            if (this.currentToken.type == ASSIGNMENT) {
                /**
                 * TODO: Single line tasks
                 * Similarly to Kotlin:
                 * `fun a() = 2`
                 * but with the following syntax:
                 * `task<int> a = 2`
                 * `task<int> a() = 2`
                 * `task a = 2`
                 *
                 * basically any variation of the above ^^^
                 */

                advanceRegister(it)

                val bodyStart = this.currentToken.start

                val body = it.register(this.expression())

                if (it.error != null) {
                    return@submit it
                }

                return@submit it.success(TaskDefineNode(
                    name,
                    returnType,
                    arguments,
                    ListNode(listOf(body!!), bodyStart, this.currentToken.end),
                    start,
                    this.currentToken.end
                ))
            }

            if (this.currentToken.type != OPEN_BRACE) {
                return@submit it.failure(ParsingError(
                    "Expected '{' or '='",
                    this.currentToken.start,
                    this.currentToken.end
                ))
            }

            advanceRegister(it)

            val body = it.register(this.statements())

            if (it.error != null) {
                return@submit it
            }

            body as Node

            if (this.currentToken.type != CLOSE_BRACE) {
                return@submit it.failure(ParsingError(
                    "Expected '}'",
                    this.currentToken.start,
                    this.currentToken.end,
                ))
            }

            advanceRegister(it)

            it.success(TaskDefineNode(
                name,
                returnType,
                arguments,
                body as ListNode,
                start,
                this.currentToken.end
            ))
        }
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