package ir.mmd.ktdev.xmlp.text

data class TextRange(
	val start: Position,
	val end: Position
)

inline operator fun Position.rangeTo(other: Position) = TextRange(this, other)
inline val Position.asSingleRange get() = TextRange(this, this)
