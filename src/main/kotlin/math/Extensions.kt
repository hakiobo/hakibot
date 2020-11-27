package math

import java.math.BigDecimal

fun tokenize(expression: String, data: Map<String, Number> = emptyMap()): Token {
    return Tokenizer.parse(expression, data)
}

fun evaluate(expression: String, data: Map<String, Number> = emptyMap()): BigDecimal {
    return tokenize(expression, data).value.stripTrailingZeros().toPlainString().toBigDecimal()
}

fun String.isUnsignedNumber(): Boolean {
    if (isEmpty() || count('.') > 1) {
        return false
    }

    return all { char -> char.isDigit() || char == '.' }
}

fun String.count(char: Char): Int {
    return count { it == char }
}

fun Number.toBigDecimal(): BigDecimal {
    return when (this) {
        is Int -> BigDecimal(this)
        is Long -> BigDecimal(this)
        is Byte, is Short -> BigDecimal(toInt())
        is Float, is Double -> BigDecimal(toString())
        else -> throw IllegalStateException()
    }
}