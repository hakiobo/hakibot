package math

fun tokenize(expression: String, data: Map<String, Number> = emptyMap()): Token {
    return Tokenizer.parse(expression, data)
}

fun evaluate(expression: String, data: Map<String, Number> = emptyMap()): Double {
    return tokenize(expression, data).value
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
