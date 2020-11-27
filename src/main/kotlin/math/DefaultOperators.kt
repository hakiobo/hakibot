package math


import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

object DefaultOperators {
    var mathContext = MathContext(10, RoundingMode.HALF_EVEN)

    val addition: Function2<BigDecimal> = { a, b -> a + b }
    val subtraction: Function2<BigDecimal> = { a, b -> a - b }
    val multiplication: Function2<BigDecimal> = { a, b -> a * b }
    val division: Function2<BigDecimal> = { a, b -> a.divide(b, mathContext) }
    val exponentiation: Function2<BigDecimal> = { a, b -> a.pow(b.toInt()) }
    val modulo: Function2<BigDecimal> = { a, b -> a % b }
}