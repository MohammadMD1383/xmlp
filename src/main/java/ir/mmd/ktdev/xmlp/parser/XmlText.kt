package ir.mmd.ktdev.xmlp.parser

import ir.mmd.ktdev.xmlp.text.TextRange

class XmlText(
	val text: String,
	override val range: TextRange
) : XmlNode {
	override fun toString() = text
}
