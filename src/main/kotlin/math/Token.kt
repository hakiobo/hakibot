package math

import java.math.BigDecimal

sealed class Token {
    abstract val value: BigDecimal

    data class Number(override val value: BigDecimal) : Token() {
        constructor(value: kotlin.Number) : this(value.toBigDecimal())
    }

    sealed class Unary : Token() {
        abstract val token: Token

        data class Plus(override val token: Token) : Unary() {
            override val value: BigDecimal
                get() = token.value.plus()
        }

        data class Minus(override val token: Token) : Unary() {
            override val value: BigDecimal
                get() = token.value.negate()
        }
    }

    data class Operator(
        val leftToken: Token,
        val rightToken: Token,
        val operation: Function2<BigDecimal>
    ) : Token() {
        override val value: BigDecimal
            get() = operation(leftToken.value, rightToken.value)
    }

    data class Variable(val name: String, override val value: BigDecimal) : Token() {
        constructor(name: String, value: kotlin.Number) : this(name, value.toBigDecimal())
    }

    sealed class Function : Token() {
        abstract val name: String

        data class OneParam(
            override val name: String,
            val token: Token,
            val function1: Function1<BigDecimal>
        ) : Function() {
            override val value: BigDecimal
                get() = function1(token.value)
        }

        data class TwoParams(
            override val name: String,
            val firstToken: Token,
            val secondToken: Token,
            val function2: Function2<BigDecimal>
        ) : Function() {
            override val value: BigDecimal
                get() = function2(firstToken.value, secondToken.value)
        }
    }
}