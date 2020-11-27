package math

import kotlin.math.*

object DefaultFunctions {
    val parentheses: Function1<Double> = { a -> a }
    val sine: Function1<Double> = { a -> sin(Math.toRadians(a)) }
    val cosine: Function1<Double> = { a -> cos(Math.toRadians(a)) }

    val rnd: Function2<Double> = { a, b -> round(a * 10.0.pow(max(0.0, b))) / 10.0.pow(max(0.0, b)) }
    val exponentiation: Function2<Double> = { a, b -> a.pow(b.toInt()) }
}