package math

import java.math.BigDecimal

data class Operator(val symbol: Char, val precedence: Int, val operation: Function2<BigDecimal>)