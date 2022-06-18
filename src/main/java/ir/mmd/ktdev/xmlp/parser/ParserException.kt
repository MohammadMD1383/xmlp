package ir.mmd.ktdev.xmlp.parser

import ir.mmd.ktdev.xmlp.text.TextRange

class ParserException(
	override val message: String,
	val range: TextRange
) : Exception()

inline infix fun String.at(range: TextRange) = ParserException(this, range)
