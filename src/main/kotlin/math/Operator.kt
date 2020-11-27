package math

data class Operator(val symbol: Char, val precedence: Int, val operation: Function2<Double>)