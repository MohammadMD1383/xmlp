package ir.mmd.ktdev.xmlp.lexer.provider

class StringStreamProvider(private val string: String) : StreamProvider {
	private var index = -1
	
	override val peek: Char?
		get() = string.getOrNull(index + 1)
	
	override val next: Char?
		get() = string.getOrNull(++index)
}

inline val String.streamProvider get() = StringStreamProvider(this)
