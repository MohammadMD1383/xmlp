package ir.mmd.ktdev.xmlp.parser

import ir.mmd.ktdev.xmlp.text.Position
import ir.mmd.ktdev.xmlp.text.TextRange
import kotlin.properties.Delegates.notNull

class XmlTag(
	val name: String,
	val isSingle: Boolean,
	val attributes: Map<String, String?>,
	val children: List<XmlNode>,
	override val range: TextRange
) : XmlNode {
	override fun toString(): String {
		val attrs = if (attributes.isNotEmpty())
			" " + attributes.entries.joinToString(" ") { "${it.key}=\"${it.value}\"" }
		else ""
		
		return if (isSingle)
			"<$name$attrs/>"
		else
			"<$name$attrs>${children.joinToString("") { it.toString() }}</$name>"
	}
}

class XmlTagFragment {
	lateinit var name: String
	var isSingle by notNull<Boolean>()
	val attributes = mutableMapOf<String, String?>()
	val children = mutableListOf<XmlNode>()
	lateinit var range: TextRange
	
	fun extendRange(position: Position) {
		range = range.copy(end = position)
	}
	
	val xmlTag get() = XmlTag(name, isSingle, attributes, children, range)
}
