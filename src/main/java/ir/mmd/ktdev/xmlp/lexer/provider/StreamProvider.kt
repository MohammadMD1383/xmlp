package ir.mmd.ktdev.xmlp.lexer.provider

interface StreamProvider {
	val peek: Char?
	val next: Char?
}
