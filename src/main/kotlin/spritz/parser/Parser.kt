package spritz.parser

import spritz.error.parsing.ParsingError
import spritz.lexer.token.Token
import spritz.lexer.token.TokenType
import spritz.lexer.token.TokenType.*
import spritz.parser.node.Node
import spritz.parser.nodes.*

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

            it.success(node as Node)
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
                LESS_THAN to null,
                LESS_THAN_OR_EQUAL_TO to null,
                GREATER_THAN to null,
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

            println("Atom: $atom")

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

            return@submit it.failure(ParsingError(
                "Expected int, float, '+', '-' or '('",
                token.start,
                token.end
            ))
        }
    }

    private fun binaryOperation(function: () -> ParseResult, operators: HashMap<TokenType, String?>, functionB: () -> ParseResult = function): ParseResult {
        return ParseResult().submit {
            var left = it.register(function())

            if (it.error != null) {
                return@submit it
            }

            while (operators.any { operator -> operator.key == this.currentToken.type && (operator.value == this.currentToken.value || operator.value == null) }) {
                val operator = this.currentToken

                advanceRegister(it)

                val right = it.register(functionB())

                if (it.error != null) {
                    return@submit it
                }

                left = BinaryOperationNode(left as Node, operator, right as Node)
            }

            return@submit it.success(left as Node)
        }
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