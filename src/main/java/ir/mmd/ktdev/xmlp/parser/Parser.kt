package ir.mmd.ktdev.xmlp.parser

import ir.mmd.ktdev.xmlp.lexer.Token
import ir.mmd.ktdev.xmlp.lexer.Token.Type.*
import ir.mmd.ktdev.xmlp.text.Position
import ir.mmd.ktdev.xmlp.text.TextRange
import ir.mmd.ktdev.xmlp.text.asSingleRange
import ir.mmd.ktdev.xmlp.text.rangeTo

class Parser(private val tokens: List<Token>) {
	private var index = -1
	private var current = null as Token?
	private inline val peek get() = tokens.getOrNull(index + 1)
	private inline val next get() = tokens.getOrNull(++index).also { current = it }
	
	fun parse(): XmlDocument {
		val rootFragment = XmlTagFragment()
		
		val rootStart = next
			?: throw "Empty xml document: No root tag" at Position(1, 0).asSingleRange
		if (rootStart.type != START_TAG_START)
			throw "Xml document should start with a root tag: '<' expected" at rootStart.range
		
		val rootName = next
			?: throw "Tag name expected" at rootStart.range.end.asSingleRange
		rootFragment.name = rootName.value
		rootFragment.range = rootStart.range.start..rootName.range.end
		
		tagAttributes(rootFragment)
		ignore(WHITE_SPACE)
		singleEndOrEnd(rootFragment)
		
		ignore(WHITE_SPACE, COMMENT)
		if (next != null)
			throw "unexpected token after root tag: '${current!!.value}'" at current!!.range
		
		return XmlDocument(rootFragment.xmlTag)
	}
	
	private fun singleEndOrEnd(fragment: XmlTagFragment) {
		val tagEnd = next
			?: throw "Expected '/>' or '>' but reached EOF" at fragment.range.end.asSingleRange
		
		when (tagEnd.type) {
			SINGLE_TAG_END -> {
				fragment.extendRange(tagEnd.range.end)
				fragment.isSingle = true
				return
			}
			
			TAG_END -> {
				xmlTagChildren(fragment)
				
				val endTagStart = next
					?: throw "expected closing tag '</${fragment.name}>' but reached EOF" at fragment.range.end.asSingleRange
				if (endTagStart.type != END_TAG_START)
					throw "expected '</' but found '${endTagStart.value}'" at endTagStart.range
				
				val endTagName = next
					?: throw "expected '${fragment.name}' but reached EOF" at endTagStart.range.end.asSingleRange
				if (endTagName.type != IDENTIFIER || endTagName.value != fragment.name)
					throw "expected '${fragment.name}' but found '${endTagName.value}'" at endTagName.range
				
				val endTagEnd = next
					?: throw "expected '>' but reached EOF" at endTagName.range.end.asSingleRange
				if (endTagEnd.type != TAG_END)
					throw "expected '>' but found '${endTagEnd.value}'" at endTagEnd.range
				
				fragment.extendRange(endTagEnd.range.end)
				fragment.isSingle = false
				return
			}
			
			else -> throw "Expected '/>' or '>' but found '${tagEnd.value}'" at tagEnd.range
		}
	}
	
	private fun xmlTagChildren(fragment: XmlTagFragment) {
		while (true) {
			val content = skipTo(START_TAG_START, END_TAG_START)
			val text = content.joinToString("") { it.value }
			val peeked = peek ?: return
			
			if (text.isNotBlank()) fragment.children += XmlText(
				text,
				content.first().range.start..content.last().range.end
			)
			
			if (peeked.type == START_TAG_START)
				fragment.children += xmlTag(peeked.range.start.asSingleRange) else break
		}
	}
	
	private fun xmlTag(startRange: TextRange): XmlTag {
		val fragment = XmlTagFragment()
		
		val startTagStart = next
			?: throw "expected '<' but reached EOF" at startRange
		if (startTagStart.type != START_TAG_START)
			throw "expected '<' but found '${startTagStart.value}'" at startTagStart.range
		fragment.range = startTagStart.range
		
		val name = next
			?: throw "expected tag name but reached EOF" at startTagStart.range.end.asSingleRange
		if (name.type != IDENTIFIER)
			throw "expected an identifier but found '${name.value}'" at name.range
		fragment.name = name.value
		
		tagAttributes(fragment)
		ignore(WHITE_SPACE)
		singleEndOrEnd(fragment)
		
		return fragment.xmlTag
	}
	
	private fun tagAttributes(fragment: XmlTagFragment) {
		while (true) {
			ignore(WHITE_SPACE)
			
			val peeked = peek
			if (peeked == null || peeked.type != IDENTIFIER) break
			val key = next!!
			
			ignore(WHITE_SPACE)
			
			val equal = next
				?: throw "expected '=' after attribute key" at key.range.end.asSingleRange
			if (equal.type != EQUAL_SIGN)
				throw "expected '=' after attribute key but found '${equal.value}'" at equal.range
			
			ignore(WHITE_SPACE)
			
			val value = next
				?: throw "expected attribute value but reached EOF" at peeked.range.end.asSingleRange
			if (value.type != STRING)
				throw "expected \"...\" but found '${value.value}'" at value.range
			
			fragment.attributes[key.value] = value.value.substring(1, value.value.lastIndex)
		}
	}
	
	private fun ignore(vararg types: Token.Type): Token? {
		while (peek?.type in types) next
		return current
	}
	
	private fun skipTo(vararg types: Token.Type) = mutableListOf<Token>().apply {
		while (true) {
			val peeked = peek
			if (peeked == null || peeked.type in types) break
			add(next!!)
		}
	}
}
