package math

import java.math.BigDecimal

sealed class Token {
    abstract val value: Double

    data class Number(override val value: Double) : Token()

    sealed class Unary : Token() {
        abstract val token: Token

        data class Plus(override val token: Token) : Unary() {
            override val value: Double
                get() = token.value
        }

        data class Minus(override val token: Token) : Unary() {
            override val value: Double
                get() = -token.value
        }
    }

    data class Operator(
        val leftToken: Token,
        val rightToken: Token,
        val operation: Function2<Double>
    ) : Token() {
        override val value: Double
            get() = operation(leftToken.value, rightToken.value)
    }

    data class Variable(val name: String, override val value: Double) : Token() {
        constructor(name: String, value: kotlin.Number) : this(name, value.toDouble())
    }

    sealed class Function : Token() {
        abstract val name: String

        data class OneParam(
            override val name: String,
            val token: Token,
            val function1: Function1<Double>
        ) : Function() {
            override val value: Double
                get() = function1(token.value)
        }

        data class TwoParams(
            override val name: String,
            val firstToken: Token,
            val secondToken: Token,
            val function2: Function2<Double>
        ) : Function() {
            override val value: Double
                get() = function2(firstToken.value, secondToken.value)
        }
    }
}