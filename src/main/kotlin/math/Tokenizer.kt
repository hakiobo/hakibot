package math

import kotlin.math.*

//modified from original code

object Tokenizer {
    private val operators = mutableMapOf<Char, Operator>()
    private val oneParamFunctions = mutableMapOf<String, Function1<Double>>()
    private val twoParamFunctions = mutableMapOf<String, Function2<Double>>()

    init {
        registerOperator('+', 1, DefaultOperators.addition)
        registerOperator('-', 1, DefaultOperators.subtraction)
        registerOperator('*', 2, DefaultOperators.multiplication)
        registerOperator('/', 2, DefaultOperators.division)
        registerOperator('^', 3, DefaultOperators.exponentiation)
        registerOperator('%', 2, DefaultOperators.modulo)

        registerFunction("", DefaultFunctions.parentheses)
        registerFunction("abs", ::abs)
        registerFunction("sqrt", ::sqrt)
        registerFunction("cbrt", Math::cbrt)
        registerFunction("ceil", ::ceil)
        registerFunction("floor", ::floor)
        registerFunction("sin", DefaultFunctions.sine)
        registerFunction("cos", DefaultFunctions.cosine)
        registerFunction("sinr", ::sin)
        registerFunction("cosr", ::cos)

        registerFunction("round", DefaultFunctions.rnd)
        registerFunction("pow", DefaultFunctions.exponentiation)
        registerFunction("min", ::min)
        registerFunction("max", ::max)
    }

    fun registerOperator(symbol: Char, precedence: Int, operation: Function2<Double>) {
        operators[symbol] = Operator(symbol, precedence, operation)
    }

    fun registerFunction(name: String, function1: Function1<Double>) {
        oneParamFunctions[name] = function1
    }

    fun registerFunction(name: String, function2: Function2<Double>) {
        twoParamFunctions[name] = function2
    }

    fun parse(expression: String, data: Map<String, Number> = emptyMap()): Token {
        if (expression.startsWith('+')) {
            return Token.Unary.Plus(parse(expression.substring(1), data))
        }

        if (expression.startsWith('-')) {
            return parse("0$expression", data)
        }

        if (expression.isUnsignedNumber()) {
            return Token.Number(expression.toDouble())
        }

        if (expression in data) {
            return Token.Variable(expression, data.getValue(expression))
        }

        operators.values.sortedBy(Operator::precedence).forEach { operator ->
            val parts = expression.splitBySymbolOutsideParentheses(operator.symbol)
            if (parts.size > 1) {
                return Token.Operator(
                    leftToken = parse(parts.dropLast(1).joinToString(operator.symbol.toString()), data),
                    rightToken = parse(parts.last(), data),
                    operation = operator.operation
                )
            }
        }

        val name = expression.substringBefore('(')
        val content = expression.substringAfter('(').substringBeforeLast(')')
        val parts = content.splitBySymbolOutsideParentheses(',')
        when (name) {
            in oneParamFunctions -> return Token.Function.OneParam(
                name = name,
                token = parse(parts[0], data),
                function1 = oneParamFunctions.getValue(name)
            )
            in twoParamFunctions -> return Token.Function.TwoParams(
                name = name,
                firstToken = parse(parts[0], data),
                secondToken = parse(parts[1], data),
                function2 = twoParamFunctions.getValue(name)
            )
        }

        throw IllegalStateException()
    }

    private fun String.splitBySymbolOutsideParentheses(symbol: Char): List<String> {
        var openingCounter = 0
        var closingCounter = 0
        var buffer = ""
        val result = mutableListOf<String>()
        forEach { char ->
            when (char) {
                '(' -> openingCounter++
                ')' -> closingCounter++
            }
            if (char == symbol && openingCounter == closingCounter) {
                result += buffer
                buffer = ""
            } else {
                buffer += char
            }
        }
        result += buffer
        return result
    }
}