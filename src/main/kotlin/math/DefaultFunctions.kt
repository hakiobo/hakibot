package math

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object DefaultFunctions {
    var mathContext = MathContext(10, RoundingMode.HALF_EVEN)

    val parentheses: Function1<BigDecimal> = { a -> a }
    val absolute: Function1<BigDecimal> = { a -> a.abs() }
    val squareRoot: Function1<BigDecimal> = { a -> sqrt(a.toDouble()).toBigDecimal(mathContext) }
    val cubeRoot: Function1<BigDecimal> = { a -> Math.cbrt(a.toDouble()).toBigDecimal(mathContext) }
    val ceil: Function1<BigDecimal> = { a -> a.setScale(0, RoundingMode.CEILING) }
    val floor: Function1<BigDecimal> = { a -> a.setScale(0, RoundingMode.FLOOR) }
    val sine: Function1<BigDecimal> = { a -> sin(Math.toRadians(a.toDouble())).toBigDecimal(mathContext) }
    val cosine: Function1<BigDecimal> = { a -> cos(Math.toRadians(a.toDouble())).toBigDecimal(mathContext) }

    val round: Function2<BigDecimal> = { a, b -> a.setScale(b.toInt(), RoundingMode.HALF_EVEN) }
    val exponentiation: Function2<BigDecimal> = { a, b -> a.pow(b.toInt()) }
    val min: Function2<BigDecimal> = { a, b -> minOf(a, b) }
    val max: Function2<BigDecimal> = { a, b -> maxOf(a, b) }
}