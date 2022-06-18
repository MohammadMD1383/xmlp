package ir.mmd.ktdev.xmlp.lexer

import ir.mmd.ktdev.xmlp.text.Position

class LexerException(
	override val message: String,
	val position: Position
) : Exception()

inline infix fun String.at(position: Position) = LexerException(this, position)
