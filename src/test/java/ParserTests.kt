import ir.mmd.ktdev.xmlp.lexer.Token
import ir.mmd.ktdev.xmlp.parser.Parser
import ir.mmd.ktdev.xmlp.parser.ParserException
import ir.mmd.ktdev.xmlp.parser.XmlTag
import ir.mmd.ktdev.xmlp.parser.XmlText
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

fun List<Token>.parse() = Parser(this).parse()
fun String.parse() = lex().parse()
fun Exception.print() = println(message)

class ParserTests {
	@Test
	fun singleRootTag() {
		println("<some/>".parse())
	}
	
	@Test
	fun rootTag() {
		println("<some></some>".parse())
	}
	
	@Test
	fun incompleteTagError() {
		assertThrows<ParserException> {
			"<hello>".parse()
		}.print()
	}
	
	@Test
	fun rootTagWithNoValueAttr() {
		assertThrows<ParserException> {
			"<root isOk/>".parse()
		}.print()
	}
	
	@Test
	fun rootTagWithAttributes() {
		val document = "<root value=\"true\"></root>".parse()
		println(document)
		
		assertEquals(
			document.root.attributes["value"],
			"true"
		)
	}
	
	@Test
	fun rootTagWithTextOnly() {
		println("<root>Some Text</root>".parse())
	}
	
	@Test
	fun rootTagWithSpacedAttr() {
		println("<root key  =   \"value\"></root>".parse())
	}
	
	@Test
	fun duplicateRootTag() {
		assertThrows<ParserException> {
			"""
				<root>
					some Text
				</root>
				<another />
			""".trimIndent().parse()
		}.print()
	}
	
	@Test
	fun sameTagLabels() {
		assertThrows<ParserException> {
			"<root></another>".parse()
		}.print()
	}
	
	@Test
	fun childrenParsing() {
		val result = """
			<root>
				<child key="value"/>
			</root>
		""".trimIndent().parse().root
		
		assertIs<XmlTag>(result.children[0])
	}
	
	@Test
	fun deepChildrenParsing() {
		val result = """
			<root>
				<child key="value">
					<l2>text</l2>
				</child>
			</root>
		""".trimIndent().parse().root
		
		assertIs<XmlTag>(result.children[0])
		assertIs<XmlTag>((result.children[0] as XmlTag).children[0])
		assertIs<XmlText>(((result.children[0] as XmlTag).children[0] as XmlTag).children[0])
	}
	
	@Test
	fun complexDeepParsing() {
		val root = """
			<root>
				<child1>
					text1
					<child2 attr='value'/>
					<child2 attr='value'/>
					text2
					<child2 attr='value'/>
				</child1>
			</root>
		""".trimIndent().parse().root
		
		assertIs<XmlTag>(root.children.first())
		
		val first = root.children.first() as XmlTag
		assertIs<XmlText>(first.children[0])
		assertIs<XmlTag>(first.children[1])
		assertIs<XmlTag>(first.children[2])
		assertIs<XmlText>(first.children[3])
		assertIs<XmlTag>(first.children[4])
		
		assertEquals(
			"value",
			(first.children[1] as XmlTag).attributes["attr"]
		)
	}
	
	@Test
	fun correctAttrValueTrimming() {
		val root1 = "<root attr='\"value\"'/>".parse().root
		val root2 = "<root attr=\"'value'\"/>".parse().root
		val root3 = "<root attr=\"'''value'\"/>".parse().root
		
		assertEquals("\"value\"", root1.attributes["attr"])
		assertEquals("'value'", root2.attributes["attr"])
		assertEquals("'''value'", root3.attributes["attr"])
	}
}
