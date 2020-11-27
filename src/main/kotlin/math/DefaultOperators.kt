package math

import kotlin.math.pow

object DefaultOperators {

    val addition: Function2<Double> = { a, b -> a + b }
    val subtraction: Function2<Double> = { a, b -> a - b }
    val multiplication: Function2<Double> = { a, b -> a * b }
    val division: Function2<Double> = { a, b -> a / b}
    val exponentiation: Function2<Double> = { a, b -> a.pow(b) }
    val modulo: Function2<Double> = { a, b -> a % b }
}